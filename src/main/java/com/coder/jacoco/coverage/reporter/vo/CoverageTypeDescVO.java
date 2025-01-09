package com.coder.jacoco.coverage.reporter.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class CoverageTypeDescVO {

    @ExcelProperty(value = "覆盖率类型说明")
    private String coverageType;

    @ExcelProperty(value = "说明内容")
    private String coverageDesc;

}
