package com.coder.jacoco.coverage.reporter.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JacocoReportConfig {


    /**
     * 通过工程id
     */
    private Long projectId;

    /**
     * 工程下载地址
     */
    private String httpUrlToRepo;

    /**
     * 工程分支
     */
    private String branch;


    private String gitCloneRootDir;

    /**
     * git lab 访问授权码
     */
    private String personalAccessToken;

    /**
     * mvn setting 访问路径
     */
    private String mvnSettingPath;


    List<JacocoExecConfig> jacocoExecConfigList;

}
