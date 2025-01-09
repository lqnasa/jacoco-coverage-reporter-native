package com.coder.jacoco.coverage.reporter.util;

import com.coder.jacoco.coverage.reporter.exception.ServiceException;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

public class GitHttpUrlUtils {


    private GitHttpUrlUtils() {

    }

    /**
     *  获取工程名称
     * @param httpUrlToRepo git http url
     * @return String
     */
    public static String getProjectName(String httpUrlToRepo) {
        try {
            URL urlToRepo = new URL(httpUrlToRepo);
            String path = urlToRepo.getPath();
            String projectName = path.replaceAll(".*?([^/]+)\\.git$", "$1");
            if (!StringUtils.hasText(projectName)) {
                throw new ServiceException(httpUrlToRepo + "地址不合法！");
            }
            return projectName;
        } catch (MalformedURLException e) {
            throw new ServiceException(httpUrlToRepo + "地址不合法！");
        }
    }


}
