package com.coder.jacoco.coverage.reporter.service;

import org.jacoco.core.analysis.IBundleCoverage;

import java.io.File;
import java.util.List;

public interface JacocoExecService {

    /**
     * 远程dump jacoco exec
     *
     * @param jacocoExecDestFile 保存文件
     * @param address            jacoco远程地址
     * @param port               jacoco远程访问端口
     */
    void dumpExec(String jacocoExecDestFile, String address, Integer port);

    /**
     * 合并 jacoco exec
     *
     * @param execFiles   jacoco exec list
     * @param newFileName 新的文件名称
     */
    void mergeExec(List<String> execFiles, String newFileName);

    /**
     * 生成报告
     *
     * @param jacocoExecFileList exec列表
     * @param classFileList      class类路径
     * @param sourceFileList     src路径
     * @param htmlPath           保存报告路径
     */
    IBundleCoverage reportExec(List<File> jacocoExecFileList, List<File> classFileList, List<File> sourceFileList, File htmlPath);

}
