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
public class JacocoExecConfig {


    /**
     * 模块名称，可以是子模块
     */
    private String moduleName;

    /**
     * 模块路径
     */
    private List<String> modulePathList;

    /**
     * 采集地址
     */
    private String address;

    /**
     * 采集端口
     */
    private Integer port;
}
