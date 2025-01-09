package com.coder.jacoco.coverage.reporter.runner;

import cn.hutool.system.OsInfo;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.DataFormatData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.coder.jacoco.coverage.reporter.annotation.CoverageDescAnnotation;
import com.coder.jacoco.coverage.reporter.config.JacocoCoverageQualifiedConfig;
import com.coder.jacoco.coverage.reporter.config.JacocoExecConfig;
import com.coder.jacoco.coverage.reporter.config.JacocoProjectConfig;
import com.coder.jacoco.coverage.reporter.config.JacocoReportConfig;
import com.coder.jacoco.coverage.reporter.constants.Constants;
import com.coder.jacoco.coverage.reporter.enums.JobStatusEnum;
import com.coder.jacoco.coverage.reporter.excel.CellStyleStrategy;
import com.coder.jacoco.coverage.reporter.excel.ExcelFillCellMergePrevColStrategy;
import com.coder.jacoco.coverage.reporter.excel.ExcelFillCellMergeStrategy;
import com.coder.jacoco.coverage.reporter.exception.ServiceException;
import com.coder.jacoco.coverage.reporter.service.GitLabProjectService;
import com.coder.jacoco.coverage.reporter.service.JacocoExecService;
import com.coder.jacoco.coverage.reporter.util.GitHttpUrlUtils;
import com.coder.jacoco.coverage.reporter.util.ShellUtils;
import com.coder.jacoco.coverage.reporter.util.SoftwareInstallInfoUtils;
import com.coder.jacoco.coverage.reporter.vo.CoverageDescVO;
import com.coder.jacoco.coverage.reporter.vo.CoverageReportVO;
import com.coder.jacoco.coverage.reporter.vo.CoverageTypeDescVO;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component
public class JacocoCoverageReporterRunner implements CommandLineRunner {


    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss");
    @Autowired
    private JacocoProjectConfig jacocoProjectConfig;

    @Autowired
    private JacocoExecService jacocoExecService;

    @Autowired
    private GitLabProjectService gitLabProjectService;

    @Value("${spring.profiles.active}")
    private String active;


    @Override
    public void run(String... args) throws Exception {
        // 异步执行report任务
        List<JacocoReportConfig> gitProjectList = jacocoProjectConfig.getGitProjectList();
        List<CoverageReportVO> coverageReportVOList = new ArrayList<>();
        for (JacocoReportConfig jacocoReportConfig : gitProjectList) {
            try {
                IBundleCoverage iBundleCoverage = doHandler(jacocoReportConfig);
                CoverageReportVO coverageReportVO = getCoverageReportVO(jacocoReportConfig, iBundleCoverage);
                coverageReportVOList.add(coverageReportVO);
            } catch (Exception e) {
                log.error("生成覆盖率报告失败！", e);
            }
        }
        addTotalCoverage(coverageReportVOList);
        doWriteExcel(gitProjectList, coverageReportVOList);
    }

    private void doWriteExcel(List<JacocoReportConfig> gitProjectList, List<CoverageReportVO> coverageReportVOList) {
        ExcelFillCellMergePrevColStrategy excelFillCellMergePrevCol = new ExcelFillCellMergePrevColStrategy();
        excelFillCellMergePrevCol.add(0, 1, 15);
        excelFillCellMergePrevCol.add(1, 1, 15);
        excelFillCellMergePrevCol.add(2, 1, 15);
        excelFillCellMergePrevCol.add(3, 1, 15);
        excelFillCellMergePrevCol.add(4, 1, 15);
        ExcelFillCellMergeStrategy excelFillCellMergeStrategy = new ExcelFillCellMergeStrategy(0, new int[]{0, 1, 2, 3, 4});

        String gitCloneRootDir = gitProjectList.get(0).getGitCloneRootDir();
        String fileName = gitCloneRootDir + File.separator + active + "工程覆盖率报告-" + DATE_TIME_FORMATTER.format(LocalDateTime.now()) + ".xlsx";
        try (ExcelWriter excelWriter = EasyExcelFactory.write(fileName, CoverageReportVO.class).build()) {
            WriteSheet writeSheet = EasyExcelFactory.writerSheet("覆盖率报告").needHead(Boolean.FALSE).build();
            // 写入描述信息
            WriteTable writeCoverageTypeTable =
                    EasyExcelFactory.writerTable(0).needHead(Boolean.FALSE)
                            .registerWriteHandler(excelFillCellMergePrevCol)
                            .registerWriteHandler(excelFillCellMergeStrategy)
                            .registerWriteHandler(new CellStyleStrategy())
                            .build();
            excelWriter.write(getCoverageTypeDescVOList(), writeSheet, writeCoverageTypeTable);

            // 写入覆盖率合规阈值描述
            WriteTable writeCoverageDescTable = EasyExcelFactory.writerTable(1).needHead(Boolean.FALSE).build();
            excelWriter.write(getCoverageDescVOList(), writeSheet, writeCoverageDescTable);

            // 写入覆盖率数据
            WriteTable writeCoverageReportTable =
                    EasyExcelFactory.writerTable(2).needHead(Boolean.TRUE)
                            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                            .build();
            excelWriter.write(coverageReportVOList, writeSheet, writeCoverageReportTable);
        }
    }

    private void addTotalCoverage(List<CoverageReportVO> coverageReportVOList) {
        CoverageReportVO totalCoverageReportVO = new CoverageReportVO();
        totalCoverageReportVO.setGitProjectName("总计");
        totalCoverageReportVO.setGitProjectBranch("--");
        // 指令覆盖
        totalCoverageReportVO.setInstructionCoveredCount(coverageReportVOList.stream().mapToInt(CoverageReportVO::getInstructionCoveredCount).sum());
        totalCoverageReportVO.setInstructionTotalCount(coverageReportVOList.stream().mapToInt(CoverageReportVO::getInstructionTotalCount).sum());
        double totalInstructionCoveredRatio =
                totalCoverageReportVO.getInstructionCoveredCount() * 1D / totalCoverageReportVO.getInstructionTotalCount();
        WriteCellData<Double> instructionCoveredRatioCellData = getDoubleWriteCellData(totalInstructionCoveredRatio,
                jacocoProjectConfig.getCoverageQualified().getInstructionCoverageValue());
        totalCoverageReportVO.setInstructionCoveredRatio(instructionCoveredRatioCellData);

        // 分支覆盖
        totalCoverageReportVO.setBranchCoveredCount(coverageReportVOList.stream().mapToInt(CoverageReportVO::getBranchCoveredCount).sum());
        totalCoverageReportVO.setBranchTotalCount(coverageReportVOList.stream().mapToInt(CoverageReportVO::getBranchTotalCount).sum());
        double totalBranchCoveredRatio = totalCoverageReportVO.getBranchCoveredCount() * 1D / totalCoverageReportVO.getBranchTotalCount();
        WriteCellData<Double> totalBranchCoveredRatioCellData = getDoubleWriteCellData(totalBranchCoveredRatio,
                jacocoProjectConfig.getCoverageQualified().getBranchCoverageValue());
        totalCoverageReportVO.setBranchCoveredRatio(totalBranchCoveredRatioCellData);

        //行覆盖
        totalCoverageReportVO.setLineCoveredCount(coverageReportVOList.stream().mapToInt(CoverageReportVO::getLineCoveredCount).sum());
        totalCoverageReportVO.setLineTotalCount(coverageReportVOList.stream().mapToInt(CoverageReportVO::getLineTotalCount).sum());
        double totalLineCoveredRatio = totalCoverageReportVO.getLineCoveredCount() * 1D / totalCoverageReportVO.getLineTotalCount();
        WriteCellData<Double> totalLineCoveredRatioCellData = getDoubleWriteCellData(totalLineCoveredRatio,
                jacocoProjectConfig.getCoverageQualified().getLineCoverageValue());
        totalCoverageReportVO.setLineCoveredRatio(totalLineCoveredRatioCellData);

        //方法覆盖
        totalCoverageReportVO.setMethodCoveredCount(coverageReportVOList.stream().mapToInt(CoverageReportVO::getMethodCoveredCount).sum());
        totalCoverageReportVO.setMethodTotalCount(coverageReportVOList.stream().mapToInt(CoverageReportVO::getMethodTotalCount).sum());
        double totalMethodCoveredRatio = totalCoverageReportVO.getMethodCoveredCount() * 1D / totalCoverageReportVO.getMethodTotalCount();
        WriteCellData<Double> totalMethodCoveredRatioCellData = getDoubleWriteCellData(totalMethodCoveredRatio,
                jacocoProjectConfig.getCoverageQualified().getMethodCoverageValue());
        totalCoverageReportVO.setMethodCoveredRatio(totalMethodCoveredRatioCellData);

        //类覆盖
        totalCoverageReportVO.setClassCoveredCount(coverageReportVOList.stream().mapToInt(CoverageReportVO::getClassCoveredCount).sum());
        totalCoverageReportVO.setClassTotalCount(coverageReportVOList.stream().mapToInt(CoverageReportVO::getClassTotalCount).sum());
        double totalClassCoveredRatio = totalCoverageReportVO.getClassCoveredCount() * 1D / totalCoverageReportVO.getClassTotalCount();
        WriteCellData<Double> totalClassCoveredRatioCellData = getDoubleWriteCellData(totalClassCoveredRatio,
                jacocoProjectConfig.getCoverageQualified().getClassCoverageValue());
        totalCoverageReportVO.setClassCoveredRatio(totalClassCoveredRatioCellData);

        coverageReportVOList.add(totalCoverageReportVO);
    }

    private static WriteCellData<Double> getDoubleWriteCellData(double coveredRatio, double coveredQualifiedRatio) {
        WriteCellData<Double> instructionCoveredRatioCellData = new WriteCellData<>();
        instructionCoveredRatioCellData.setType(CellDataTypeEnum.NUMBER);
        instructionCoveredRatioCellData.setNumberValue(BigDecimal.valueOf(coveredRatio));

        WriteFont writeFont = new WriteFont();
        writeFont.setColor(coveredRatio > coveredQualifiedRatio ? IndexedColors.GREEN.getIndex() : IndexedColors.RED.getIndex());
        writeFont.setBold(true);

        DataFormatData dataFormatData = new DataFormatData();
        dataFormatData.setFormat("#,#00.0# %");

        WriteCellStyle writeStyle = new WriteCellStyle();
        writeStyle.setWriteFont(writeFont);
        writeStyle.setDataFormatData(dataFormatData);

        instructionCoveredRatioCellData.setWriteCellStyle(writeStyle);
        return instructionCoveredRatioCellData;
    }

    private CoverageReportVO getCoverageReportVO(JacocoReportConfig jacocoReportConfig, IBundleCoverage iBundleCoverage) {
        CoverageReportVO coverageReportVO = new CoverageReportVO();
        String projectName = GitHttpUrlUtils.getProjectName(jacocoReportConfig.getHttpUrlToRepo());
        coverageReportVO.setGitProjectName(projectName);
        coverageReportVO.setGitProjectBranch(jacocoReportConfig.getBranch());

        // 指令覆盖(Instructions,C0coverage)：计数单元是单个java二进制代码指令，指令覆盖率提供了代码是否被执行的信息，度量完全 独立源码格式。
        ICounter instructionCounter = iBundleCoverage.getInstructionCounter();
        int instructionCoveredCount = instructionCounter.getCoveredCount();
        int instructionTotalCount = instructionCounter.getTotalCount();
        double instructionCoveredRatio = instructionCounter.getCoveredRatio();
        coverageReportVO.setInstructionCoveredCount(instructionCoveredCount);
        coverageReportVO.setInstructionTotalCount(instructionTotalCount);
        WriteCellData<Double> instructionCoveredRatioCellData = getDoubleWriteCellData(instructionCoveredRatio,
                jacocoProjectConfig.getCoverageQualified().getInstructionCoverageValue());
        coverageReportVO.setInstructionCoveredRatio(instructionCoveredRatioCellData);

        //分支覆盖率(Branches,C1coverage)：度量if和switch语句的分支覆盖情况，计算一个方法里面的总分支数，确定执行和不执行的 分支数量。
        ICounter branchCounter = iBundleCoverage.getBranchCounter();
        int branchCoveredCount = branchCounter.getCoveredCount();
        int branchTotalCount = branchCounter.getTotalCount();
        double branchCoveredRatio = branchCounter.getCoveredRatio();
        coverageReportVO.setBranchCoveredCount(branchCoveredCount);
        coverageReportVO.setBranchTotalCount(branchTotalCount);
        WriteCellData<Double> branchCoveredRatioCellData = getDoubleWriteCellData(branchCoveredRatio,
                jacocoProjectConfig.getCoverageQualified().getBranchCoverageValue());
        coverageReportVO.setBranchCoveredRatio(branchCoveredRatioCellData);

        //行覆盖率(Lines)：度量被测程序的每行代码是否被执行，判断标准行中是否至少有一个指令被执行。
        ICounter lineCounter = iBundleCoverage.getLineCounter();
        int lineCoveredCount = lineCounter.getCoveredCount();
        int lineTotalCount = lineCounter.getTotalCount();
        double lineCoveredRatio = lineCounter.getCoveredRatio();
        coverageReportVO.setLineCoveredCount(lineCoveredCount);
        coverageReportVO.setLineTotalCount(lineTotalCount);
        WriteCellData<Double> lineCoveredRatioCellData = getDoubleWriteCellData(lineCoveredRatio,
                jacocoProjectConfig.getCoverageQualified().getLineCoverageValue());
        coverageReportVO.setLineCoveredRatio(lineCoveredRatioCellData);

        //方法覆盖率(non-abstract methods)：度量被测程序的方法执行情况，是否执行取决于方法中是否有至少一个指令被执行。
        ICounter methodCounter = iBundleCoverage.getMethodCounter();
        int methodCoveredCount = methodCounter.getCoveredCount();
        int methodTotalCount = methodCounter.getTotalCount();
        double methodCoveredRatio = methodCounter.getCoveredRatio();
        coverageReportVO.setMethodCoveredCount(methodCoveredCount);
        coverageReportVO.setMethodTotalCount(methodTotalCount);
        WriteCellData<Double> methodCoveredRatioCellData = getDoubleWriteCellData(methodCoveredRatio,
                jacocoProjectConfig.getCoverageQualified().getMethodCoverageValue());
        coverageReportVO.setMethodCoveredRatio(methodCoveredRatioCellData);

        //类覆盖率(classes))：度量计算class类文件是否被执行。
        ICounter classCounter = iBundleCoverage.getClassCounter();
        int classCoveredCount = classCounter.getCoveredCount();
        int classTotalCount = classCounter.getTotalCount();
        double classCoveredRatio = classCounter.getCoveredRatio();
        coverageReportVO.setClassCoveredCount(classCoveredCount);
        coverageReportVO.setClassTotalCount(classTotalCount);
        WriteCellData<Double> classCoveredRatioCellData = getDoubleWriteCellData(classCoveredRatio,
                jacocoProjectConfig.getCoverageQualified().getClassCoverageValue());
        coverageReportVO.setClassCoveredRatio(classCoveredRatioCellData);

        return coverageReportVO;
    }


    private IBundleCoverage doHandler(JacocoReportConfig jacocoReportConfig) {

        log.info(JSON.toJSONString(jacocoReportConfig, SerializerFeature.PrettyFormat));
        String repositoryRootDir = jacocoReportConfig.getGitCloneRootDir();
        String personalAccessToken = jacocoReportConfig.getPersonalAccessToken();
        String httpUrlToRepo = jacocoReportConfig.getHttpUrlToRepo();
        String projectName = GitHttpUrlUtils.getProjectName(httpUrlToRepo);

        UUID jacocoReportId = UUID.randomUUID();
        // git clone 保存根路径  repositoryRootDir + jacocoReportId
        File repositoryRootDirFile = new File(repositoryRootDir, jacocoReportId.toString());
        if (repositoryRootDirFile.exists()) {
            FileSystemUtils.deleteRecursively(repositoryRootDirFile);
        }
        repositoryRootDirFile.mkdirs();
        //  git clone 保存路径 repositoryRootDir + jacocoReportId + name
        File repositoryDirFile = new File(repositoryRootDirFile, projectName);

        // 1、拉取git项目
        gitLabProjectService.cloneProject(httpUrlToRepo, personalAccessToken, jacocoReportConfig.getBranch(), repositoryDirFile);

        // 2、编译git项目
        compileProject(jacocoReportConfig, repositoryDirFile);

        // 3、下载远程jacoco.exec
        List<File> jacocoExecFileList = new ArrayList<>();
        List<File> classFileList = new ArrayList<>();
        List<File> sourceFileList = new ArrayList<>();
        dumpExec(jacocoReportConfig, repositoryRootDirFile, jacocoExecFileList, classFileList, sourceFileList);

        // 4、生成报告
        File saveReportPath = new File(repositoryRootDirFile, Constants.HTML_PATH);
        IBundleCoverage iBundleCoverage = jacocoExecService.reportExec(jacocoExecFileList, classFileList, sourceFileList, saveReportPath);
        log.info("生成报告路径 {}", saveReportPath);

        // 5.打开windows报告路径
        openReporterWindowsPath(new File(saveReportPath, Constants.INDEX_HTML));

        return iBundleCoverage;
    }

    private void dumpExec(JacocoReportConfig jacocoReportConfig, File repositoryRootDirFile, List<File> jacocoExecFileList,
                          List<File> classFileList, List<File> sourceFileList) {
        log.info("{}", JobStatusEnum.PULL_EXEC_SING.getStatusDesc());
        try {
            for (JacocoExecConfig jacocoExecConfig : jacocoReportConfig.getJacocoExecConfigList()) {
                // exec 保存根路径  repositoryRootDir + moduleName + _jacoco.exec
                File jacocoExecFile = new File(repositoryRootDirFile, jacocoExecConfig.getModuleName() + Constants.JACOCO_EXEC_SUFFIX);

                List<String> modulePathList = jacocoExecConfig.getModulePathList();
                for (String modulePath : modulePathList) {
                    File classFile = new File(repositoryRootDirFile, modulePath + Constants.CLASS_FILE_SUFFIX);
                    File sourceFile = new File(repositoryRootDirFile, modulePath + Constants.SOURCE_FILE_SUFFIX);
                    classFileList.add(classFile);
                    sourceFileList.add(sourceFile);
                }
                jacocoExecService.dumpExec(jacocoExecFile.getPath(), jacocoExecConfig.getAddress(), jacocoExecConfig.getPort());
                jacocoExecFileList.add(jacocoExecFile);
            }
        } catch (Exception e) {
            log.info("{}", JobStatusEnum.PULL_EXEC_FAILED.getStatusDesc());
            throw new ServiceException("dump jacoco exec失败！", e);
        }
        log.info("{}", JobStatusEnum.PULL_EXEC_SUCCESS.getStatusDesc());
    }

    private void compileProject(JacocoReportConfig jacocoReportConfig, File repositoryDirFile) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            File file = new File(repositoryDirFile, Constants.JACOCO_LOG);
            while (!file.exists()) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            openReporterWindowsPath(file);
        });
        gitLabProjectService.compileProject(repositoryDirFile.getPath(), jacocoReportConfig.getMvnSettingPath());
    }


    private static void openReporterWindowsPath(File saveReportPath) {
        OsInfo osInfo = new OsInfo();
        if (osInfo.isWindows()) {
            Map<String, String> softwarePathMap = SoftwareInstallInfoUtils.getSoftwarePathMap();

            String chromeSoftwarePath = softwarePathMap.get("chrome.exe");
            if (!StringUtils.hasText(chromeSoftwarePath)) {
                chromeSoftwarePath = softwarePathMap.get("iexplore.exe");
            }

            if (!StringUtils.hasText(chromeSoftwarePath)) {
                log.error("没有可用的浏览器！无法打开" + saveReportPath);
                return;
            }

            log.info("使用 {} 浏览器打开{}", chromeSoftwarePath, saveReportPath);
            String[] openCmd = {"cmd", "/c", String.format(" \"%s\" \"%s\" ", chromeSoftwarePath, saveReportPath)};
            try {
                log.info("执行shell命令：{}", JSON.toJSONString(openCmd));
                ShellUtils.executeShell(openCmd);
            } catch (Exception e) {
                log.error("浏览器打开" + saveReportPath + "失败！", e);
            }
        }
    }

    public List<CoverageDescVO> getCoverageDescVOList() {
        JacocoCoverageQualifiedConfig coverageQualified = jacocoProjectConfig.getCoverageQualified();
        List<CoverageDescVO> coverageDescVOList = new ArrayList<>();

        Field[] declaredFields = coverageQualified.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            CoverageDescAnnotation annotation = AnnotationUtils.findAnnotation(declaredField, CoverageDescAnnotation.class);
            if (Objects.isNull(annotation)) {
                continue;
            }
            declaredField.setAccessible(true);
            try {
                Double aDouble = (Double) declaredField.get(coverageQualified);
                CoverageDescVO coverageDescVO = new CoverageDescVO();
                coverageDescVO.setCoverageValue(aDouble);
                coverageDescVO.setCoverageDesc(annotation.value());
                coverageDescVOList.add(coverageDescVO);
            } catch (IllegalAccessException e) {
                log.error("获取不到值", e);
            }
        }
        return coverageDescVOList;
    }

    public List<CoverageTypeDescVO> getCoverageTypeDescVOList() {
        List<CoverageTypeDescVO> coverageTypeDescVOList = new ArrayList<>();
        CoverageTypeDescVO coverageTypeDescVO = new CoverageTypeDescVO();
        coverageTypeDescVO.setCoverageType("覆盖率指标说明");
        coverageTypeDescVO.setCoverageDesc("指令覆盖(Instructions,C0 coverage)：计数单元是单个java二进制代码指令，指令覆盖率提供了代码是否被执行的信息，度量完全独立源码格式。");
        coverageTypeDescVOList.add(coverageTypeDescVO);

        CoverageTypeDescVO coverageTypeDescVO1 = new CoverageTypeDescVO();
        coverageTypeDescVO1.setCoverageType("覆盖率指标说明");
        coverageTypeDescVO1.setCoverageDesc("分支覆盖率(Branches,C1 coverage)：度量if和switch语句的分支覆盖情况，计算一个方法里面的总分支数，确定执行和不执行的分支数量。");
        coverageTypeDescVOList.add(coverageTypeDescVO1);

        CoverageTypeDescVO coverageTypeDescVO2 = new CoverageTypeDescVO();
        coverageTypeDescVO2.setCoverageType("覆盖率指标说明");
        coverageTypeDescVO2.setCoverageDesc("行覆盖率(Lines)：度量被测程序的每行代码是否被执行，判断标准行中是否至少有一个指令被执行。");
        coverageTypeDescVOList.add(coverageTypeDescVO2);

        CoverageTypeDescVO coverageTypeDescVO3 = new CoverageTypeDescVO();
        coverageTypeDescVO3.setCoverageType("覆盖率指标说明");
        coverageTypeDescVO3.setCoverageDesc("方法覆盖率(non-abstract methods)：度量被测程序的方法执行情况，是否执行取决于方法中是否有至少一个指令被执行。");
        coverageTypeDescVOList.add(coverageTypeDescVO3);

        CoverageTypeDescVO coverageTypeDescVO4 = new CoverageTypeDescVO();
        coverageTypeDescVO4.setCoverageType("覆盖率指标说明");
        coverageTypeDescVO4.setCoverageDesc("类覆盖率(classes)：度量计算class类文件是否被执行。");
        coverageTypeDescVOList.add(coverageTypeDescVO4);
        return coverageTypeDescVOList;
    }


    public static void main(String[] args) {
        List<CoverageReportVO> coverageReportVOList = new ArrayList<>();
        CoverageReportVO coverageReportVO = new CoverageReportVO();
        coverageReportVO.setGitProjectName("xxxx-module");
        coverageReportVO.setGitProjectBranch("xxxx");
        coverageReportVO.setInstructionCoveredCount(64086);
        coverageReportVO.setInstructionTotalCount(279955);
//        WriteCellData<Double> instructionCoveredRatioCellData = getDoubleWriteCellData(64086 * 1D / 279955);
//        coverageReportVO.setInstructionCoveredRatio(instructionCoveredRatioCellData);
        coverageReportVOList.add(coverageReportVO);

        EasyExcelFactory.write("D:\\jacoco\\test.xlsx", CoverageReportVO.class)
//                .head(head())
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
//                .registerWriteHandler(new RatioCellWriteStrategy())
//                .registerWriteHandler(new HeadMergeStrategy())
//                .registerWriteHandler(new CellRowHeightStyleStrategy())
                .sheet("覆盖率报告").doWrite(coverageReportVOList);


    }
}
