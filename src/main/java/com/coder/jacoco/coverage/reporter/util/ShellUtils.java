package com.coder.jacoco.coverage.reporter.util;

import com.alibaba.fastjson.JSON;
import com.coder.jacoco.coverage.reporter.exception.ServiceException;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description: shell 执行工具类
 * Copyright: Copyright (c)
 * Company:  Co., Ltd.
 * Create Time: 2021/8/27 16:32
 *
 * @author coderLee23
 */
@Log4j2
public class ShellUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellUtils.class);

    private ShellUtils() {
        throw new IllegalStateException("ShellUtils Utility class");
    }


    /**
     * 执行shell脚本
     *
     * @param commandArr shell指令集合
     * @return 执行结果
     * @throws InterruptedException ex
     */
    public static CommandResult executeShell(String[] commandArr) throws InterruptedException {
        Assert.notEmpty(commandArr, "commandArr must not be empty");
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(commandArr);
        List<String> successMsg;
        List<String> errorMsg;
        Process process = null;
        int exitCode = -1;
        try {
            process = processBuilder.start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("GBK")))) {
                successMsg = br.lines().collect(Collectors.toList());
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream(), Charset.forName("GBK")))) {
                errorMsg = br.lines().collect(Collectors.toList());
            }
            exitCode = process.waitFor();

        } catch (IOException e) {
            LOGGER.error(String.format("执行shell脚本%s发生io异常！", JSON.toJSONString(commandArr)), e);
            throw new ServiceException("执行脚本" + JSON.toJSONString(commandArr) + "失败！", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        CommandResult commandResult = new CommandResult();
        commandResult.setResult(exitCode);
        commandResult.setSuccessMsg(successMsg);
        commandResult.setErrorMsg(errorMsg);

        return commandResult;
    }


    /**
     * Command执行结果
     */
    @Data
    public static class CommandResult {

        /**
         * 执行结果码
         */
        private int result;

        /**
         * 成功返回结果
         */
        private List<String> successMsg;

        /**
         * 错误返回结果
         */
        private List<String> errorMsg;


    }

}
