package com.coder.jacoco.coverage.reporter.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.metadata.data.WriteCellData;
import lombok.Data;

@Data
public class CoverageReportVO {

    @ColumnWidth(30)
    @ExcelProperty(value = "工程名称")
    private String gitProjectName;

    @ExcelProperty(value = "工程分支")
    private String gitProjectBranch;


    @ExcelProperty(value = "Java字节指令覆盖数量")
    private Integer instructionCoveredCount;

    @ExcelProperty(value = "Java字节指令总数量")
    private Integer instructionTotalCount;

    @ExcelProperty(value = "Java字节指令覆盖率")
    private WriteCellData<Double> instructionCoveredRatio;


    @ExcelProperty(value = "分支覆盖数量")
    private Integer branchCoveredCount;

    @ExcelProperty(value = "分支总数量")
    private Integer branchTotalCount;

    @ExcelProperty(value = "分支覆盖率")
    private WriteCellData<Double> branchCoveredRatio;


    @ExcelProperty(value = "Lines覆盖数量")
    private Integer lineCoveredCount;

    @ExcelProperty(value = "Lines总数量")
    private Integer lineTotalCount;

    @ExcelProperty(value = "Lines覆盖率")
    private WriteCellData<Double> lineCoveredRatio;


    @ExcelProperty(value = "Methods覆盖数量")
    private Integer methodCoveredCount;

    @ExcelProperty(value = "Methods总数量")
    private Integer methodTotalCount;

    @ExcelProperty(value = "Methods覆盖率")
    private WriteCellData<Double> methodCoveredRatio;


    @ExcelProperty(value = "Classes覆盖数量")
    private Integer classCoveredCount;

    @ExcelProperty(value = "Classes总数量")
    private Integer classTotalCount;

    @ExcelProperty(value = "Classes覆盖率")
    private WriteCellData<Double> classCoveredRatio;


}
