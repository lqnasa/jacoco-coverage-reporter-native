package com.coder.jacoco.coverage.reporter.config;

import com.coder.jacoco.coverage.reporter.annotation.CoverageDescAnnotation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class JacocoCoverageQualifiedConfig {

    @CoverageDescAnnotation(value = "java字节码覆盖率合格阈值")
    private Double instructionCoverageValue;
    @CoverageDescAnnotation("分支覆盖率合格阈值")
    private Double branchCoverageValue;
    @CoverageDescAnnotation("lines覆盖率合格阈值")
    private Double lineCoverageValue;
    @CoverageDescAnnotation("methods覆盖率合格阈值")
    private Double methodCoverageValue;
    @CoverageDescAnnotation("classes覆盖率合格阈值")
    private Double classCoverageValue;

    public JacocoCoverageQualifiedConfig() {
        this.instructionCoverageValue = 0.5D;
        this.branchCoverageValue = 0.5D;
        this.lineCoverageValue = 0.5D;
        this.methodCoverageValue = 0.5D;
        this.classCoverageValue = 0.5D;
    }

}
