package com.coder.jacoco.coverage.reporter.service.impl;

import com.alibaba.fastjson.JSON;
import com.coder.jacoco.coverage.reporter.enums.JobStatusEnum;
import com.coder.jacoco.coverage.reporter.exception.ServiceException;
import com.coder.jacoco.coverage.reporter.service.JacocoExecService;
import lombok.extern.log4j.Log4j2;
import org.jacoco.core.analysis.*;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.ContentTypeDetector;
import org.jacoco.core.tools.ExecDumpClient;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.*;
import org.jacoco.report.html.HTMLFormatter;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Log4j2
@Service
public class JacocoExecServiceImpl implements JacocoExecService {

    @Override
    public void dumpExec(String jacocoExecDestFile, String address, Integer port) {
        Assert.hasText(jacocoExecDestFile, "jacocoExecDestFile must be not null or empty");
        Assert.hasText(address, "address must be not null or empty");
        Assert.notNull(port, "port must be not null or empty");
        final ExecDumpClient client = new ExecDumpClient() {
            @Override
            protected void onConnecting(final InetAddress address, final int port) {
                log.info(" Connecting to {}:{}", address, port);
            }

            @Override
            protected void onConnectionFailure(final IOException exception) {
                log.error("onConnectionFailure {}", exception.getMessage());
            }
        };
        client.setRetryCount(3);

        try {
            ExecFileLoader loader = client.dump(address, port);
            loader.save(new File(jacocoExecDestFile), true);
        } catch (IOException e) {
            throw new ServiceException("文件IO异常", e);
        }


    }

    @Override
    public void mergeExec(List<String> execFiles, String newFileName) {
        Assert.notEmpty(execFiles, "execFiles must not be empty");
        Assert.hasText(newFileName, "newFileName must not be null or empty");
        ExecFileLoader execFileLoader = new ExecFileLoader();
        try {
            for (String execFile : execFiles) {
                execFileLoader.load(new File(execFile));
            }
            execFileLoader.save(new File(newFileName), false);
        } catch (Exception e) {
            log.error(String.format("ExecFiles 合并失败 errorMessege is %s", e.fillInStackTrace()));
        }
    }

    @Override
    public IBundleCoverage reportExec(List<File> jacocoExecFileList, List<File> classFileList, List<File> sourceFileList, File htmlPath) {
        Assert.notEmpty(jacocoExecFileList, "jacocoExecFileList must not be empty");
        Assert.notEmpty(classFileList, "classFileList must not be empty");
        Assert.notEmpty(sourceFileList, "sourceFileList must not be empty");
        Assert.notNull(htmlPath, "htmlPath must not be null");

        log.info("{}", JobStatusEnum.REPORTGENERATING.getStatusDesc());
        try {
            ExecFileLoader loader = loadExecutionData(jacocoExecFileList);
            final IBundleCoverage bundle = analyze(loader.getExecutionDataStore(), classFileList);
            writeReports(bundle, loader, sourceFileList, htmlPath);
            log.info("{}", JobStatusEnum.GENERATEREPORT_DONE.getStatusDesc());
            return bundle;
        } catch (IOException e) {
            log.info("{}", JobStatusEnum.GENERATEREPORT_FAIL.getStatusDesc());
            throw new ServiceException("生成报告失败！", e);
        }
    }

    private ExecFileLoader loadExecutionData(List<File> execFileList) throws IOException {
        log.info("loadExecutionData execFiles {}", JSON.toJSONString(execFileList));
        final ExecFileLoader loader = new ExecFileLoader();
        for (File execFile : execFileList) {
            loader.load(execFile);
        }
        return loader;
    }

    private IBundleCoverage analyze(final ExecutionDataStore data, List<File> classFileList) throws IOException {
        final CoverageBuilder builder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(data, builder);
        for (final File classFile : classFileList) {
            // 处理\target\classes 含有非class文件
//            analyzer.analyzeAll(classFile);
            try (Stream<Path> pathStream = Files.find(classFile.toPath(), Integer.MAX_VALUE, (path, attr) -> attr.isRegularFile())) {
                pathStream.forEach(path -> {
                    File file = path.toFile();
                    try (InputStream input = Files.newInputStream(file.toPath())) {
                        ContentTypeDetector detector = new ContentTypeDetector(input);
                        if (detector.getType() == ContentTypeDetector.CLASSFILE) {
                            analyzer.analyzeClass(detector.getInputStream(), file.getPath());
                        }
                    } catch (Exception e) {
                        log.error("读取class文件" + file.getPath() + "失败", e);
                    }
                });
            }
        }
        for (final IClassCoverage c : builder.getNoMatchClasses()) {
            log.warn("Execution data for class {} does not match", c.getName());
        }
        return builder.getBundle("JaCoCo Coverage Report");
    }

    private void writeReports(final IBundleCoverage bundle, final ExecFileLoader loader, List<File> sourceFileList, File htmlPath) throws IOException {
        log.info("Analyzing classes {}", bundle.getClassCounter().getTotalCount());
        final IReportVisitor visitor = createReportVisitor(htmlPath);
        visitor.visitInfo(loader.getSessionInfoStore().getInfos(), loader.getExecutionDataStore().getContents());
        visitor.visitBundle(bundle, getSourceLocator(sourceFileList));
        visitor.visitEnd();
    }

    private IReportVisitor createReportVisitor(File htmlPath) throws IOException {
        final List<IReportVisitor> visitors = new ArrayList<>();
        final HTMLFormatter formatter = new HTMLFormatter();
        visitors.add(formatter.createVisitor(new FileMultiReportOutput(htmlPath)));
        return new MultiReportVisitor(visitors);
    }

    private ISourceFileLocator getSourceLocator(List<File> sourceFileList) {
        final MultiSourceFileLocator multi = new MultiSourceFileLocator(4);
        for (final File sourceFile : sourceFileList) {
            multi.add(new DirectorySourceFileLocator(sourceFile, "UTF-8", 4));
        }
        return multi;
    }

}
