package com.coder.jacoco.coverage.reporter.enums;

import lombok.Getter;

/**
 * Description: Function Description
 * Copyright: Copyright (c)
 * Company:  Co., Ltd.
 * Create Time: 2021/7/30 11:37
 *
 * @author coderLee23
 */
@Getter
public enum JobStatusEnum {

    /**
     * 任务状态
     */
    INITIAL("初始数据"),
    INITIAL_FAIL("初始数据失败"),
    WAITING("待执行"),

    CLONING("下载代码中"),
    CLONE_DONE("下载代码成功"),
    CLONE_FAIL("下载代码失败"),

    COMPILING("编译中"),
    COMPILE_DONE("编译成功"),
    COMPILE_FAIL("编译失败"),

    REPORTGENERATING("生成报告中"),
    GENERATEREPORT_DONE("生成报告成功"),
    GENERATEREPORT_FAIL("生成报告失败"),

    SUCCESS("执行成功"),
    TIMEOUT("超时"),
    REMOVE_FILE_ING("删除源文件中"),
    REMOVE_FILE_DONE("删除源文件成功"),
    REMOVE_FILE_FAIL("删除源文件失败"),

    PULL_EXEC_SING("拉取exec文件中"),
    PULL_EXEC_SUCCESS("拉取exec文件成功"),
    PULL_EXEC_FAILED("拉取exec文件失败"),

    MERGE_EXEC_SUCCESS("mergeExec文件成功"),
    MERGE_EXEC_FAILED("mergeExec文件失败");


    private String statusDesc;

    JobStatusEnum(String statusDesc) {
        this.statusDesc = statusDesc;
    }

}