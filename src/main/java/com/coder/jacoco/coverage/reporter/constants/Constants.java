package com.coder.jacoco.coverage.reporter.constants;


import java.io.File;

/**
 * Description: Function Description
 * Copyright: Copyright (c)
 * Company:  Co., Ltd.
 * Create Time: 2021/7/30 11:37
 *
 * @author coderLee23
 */
public interface Constants {

    /**
     * user home
     */
    String USER_HOME = "user.home";

    /**
     * code root
     */
    String CODE_ROOT = System.getProperty(USER_HOME) + "/app/super_jacoco/clonecode/";

    /**
     * 日志路径
     */
    String LOG_PATH = System.getProperty(USER_HOME) + "/report/logs/";

    /**
     * report path
     */
    String REPORT_PATH = System.getProperty(USER_HOME) + "/report/";

    /**
     * 覆盖路径
     */
    String COV_PATH = System.getProperty(USER_HOME) + "/cover/";

    /**
     * 编译脚本
     */
    String COMPILE_CMD = "cd %s &&mvn clean compile -Dmaven.test.skip=true %s >> %s";

    /**
     * jacoco exec 后缀
     */
    String JACOCO_EXEC_SUFFIX = "_jacoco.exec";

    /**
     * class file 路径
     */
    String CLASS_FILE_SUFFIX = "\\target\\classes";

    /**
     * SOURCE file 路径
     */
    String SOURCE_FILE_SUFFIX = "\\src\\main\\java";


    String JACOCO_LOG = File.separator + "jacoco.log";

    String HTML_PATH = "html-report";

    String INDEX_HTML = File.separator + "index.html";
}
