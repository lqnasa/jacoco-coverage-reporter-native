package com.coder.jacoco.coverage.reporter.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import lombok.Data;

@Data
public class CoverageDescVO {

    @ExcelProperty(value = "覆盖合规说明")
    private String coverageDesc;

    @NumberFormat("#,#00.00# %")
    @ExcelProperty(value = "覆盖合规值")
    private Double coverageValue;

}
