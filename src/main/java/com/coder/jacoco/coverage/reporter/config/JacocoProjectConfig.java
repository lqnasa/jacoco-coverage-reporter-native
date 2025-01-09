package com.coder.jacoco.coverage.reporter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@ConfigurationProperties(prefix = "jacoco.report.config")
public class JacocoProjectConfig {

    JacocoCoverageQualifiedConfig coverageQualified = new JacocoCoverageQualifiedConfig();

    List<JacocoReportConfig> gitProjectList;


}
