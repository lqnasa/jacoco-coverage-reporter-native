package com.coder.jacoco.coverage.reporter.service;

import java.io.File;

public interface GitLabProjectService {

    /**
     * 获取所有 projectVo
     *
     * @param hostUrl             git服务地址
     * @param personalAccessToken 个人访问授权码
     * @param branch              工程分支
     * @param repositoryDir       下载目录
     */
    void cloneProject(String hostUrl, String personalAccessToken, String branch, File repositoryDir);

    /**
     * 编译 project
     *
     * @param repositoryDir 路径
     * @param mvnSettingXml mvn setting.xml
     */
    void compileProject(String repositoryDir, String mvnSettingXml);
}
