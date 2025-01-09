package com.coder.jacoco.coverage.reporter.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description: Function Description
 * Copyright: Copyright (c)
 * Company:  Co., Ltd.
 * Create Time: 2022/8/10 18:27
 *
 * @author coderLee23
 */
@Log4j2
public class SoftwareInstallInfoUtils {

    private static final List<String> HKEY_LIST = Arrays.asList("HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths", "HKLM\\SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\App Paths", "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\App Paths");

    private static final Pattern READ_KEY_PATTERN = Pattern.compile("(?i)[\\s+]?\\w+[\\s+]?\\w+[\\s+](.+)$");

    /**
     * 获取电脑安装的软件信息
     *
     * @return List<SoftwareInfo> 软件信息列表
     */
    public static Map<String, String> getSoftwarePathMap() {
        Map<String, String> softwareAllMap = new HashMap<>();
        for (String hkey : HKEY_LIST) {
            try {
                Map<String, String> softwareMap = getSoftwareMap(hkey);
                softwareAllMap.putAll(softwareMap);
            } catch (IOException e) {
                log.error("忽略异常！", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("忽略异常！", e);
            }
        }
        return softwareAllMap;
    }

    private static Map<String, String> getSoftwareMap(String hkey) throws IOException, InterruptedException {
        Map<String, String> softwareMap = new HashMap<>(10);
        ShellUtils.CommandResult commandResult = ShellUtils.executeShell(new String[]{"cmd", "/c", String.format("reg query \"%s\" /ve /s", hkey)});
        if (commandResult.getResult() != 0) {
            log.warn("请求返回值获取异常！异常错误信息为：{}", JSON.toJSONString(commandResult.getErrorMsg()));
            return softwareMap;
        }

        for (String msg : commandResult.getSuccessMsg()) {
            String softwareMsg = msg.toLowerCase();
            if (softwareMsg.startsWith("hkey_") || !softwareMsg.endsWith(".exe")) {
                continue;
            }

            Matcher matcher = READ_KEY_PATTERN.matcher(msg);
            while (matcher.find()) {
                String softwarePath = matcher.group(1).trim();
                String softwareName = softwarePath.replaceAll(".*?([^\\\\]+)$", "$1");
                softwareMap.putIfAbsent(softwareName.toLowerCase(), softwarePath);
            }
        }
        return softwareMap;
    }

    public static void main(String[] args) {

        Map<String, String> softwarePathMap = getSoftwarePathMap();
        System.out.println(JSON.toJSONString(softwarePathMap, SerializerFeature.PrettyFormat));
    }

}
