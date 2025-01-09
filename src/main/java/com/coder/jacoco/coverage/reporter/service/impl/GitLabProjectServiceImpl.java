package com.coder.jacoco.coverage.reporter.service.impl;

import cn.hutool.system.OsInfo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.coder.jacoco.coverage.reporter.constants.Constants;
import com.coder.jacoco.coverage.reporter.enums.JobStatusEnum;
import com.coder.jacoco.coverage.reporter.exception.ServiceException;
import com.coder.jacoco.coverage.reporter.service.GitLabProjectService;
import com.coder.jacoco.coverage.reporter.util.ShellUtils;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;

import javax.annotation.Nullable;
import java.io.File;

@Log4j2
@Service
public class GitLabProjectServiceImpl implements GitLabProjectService {

    /**
     * mvn --offline -s %s clean compile -Dmaven.test.skip=true
     */
    private static final String WIN_COMPILE_CMD = "cd /d %s && mvn -s %s clean compile -Dmaven.test.skip=true >> %s";

    private static final String LINUX_COMPILE_CMD = "cd /d %s && mvn -s %s clean compile -Dmaven.test.skip=true >> %s";


    @Override
    public void cloneProject(String projectHttpUrl, String personalAccessToken, String branch, File repositoryDir) {
        Assert.hasText(projectHttpUrl, "projectHttpUrl must not be null or empty");
        Assert.hasText(personalAccessToken, "personalAccessToken must not be null or empty");
        Assert.hasText(branch, "branch must not be null or empty");
        Assert.notNull(repositoryDir, "repositoryDir must not be null or empty");

        log.info("{}", JobStatusEnum.CLONING.getStatusDesc());
        StopWatch watch = new StopWatch();
        watch.start();
        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", personalAccessToken);
        try {
            Git git =
                    Git.cloneRepository().setURI(projectHttpUrl).setBranch(branch).setCredentialsProvider(credentialsProvider).setDirectory(repositoryDir).setProgressMonitor(new TextProgressMonitor()).call();
            git.close();
        } catch (GitAPIException e) {
            log.info("{}", JobStatusEnum.CLONE_FAIL.getStatusDesc());
            throw new ServiceException(e.getMessage(), e);
        } finally {
            watch.stop();
            log.info("clone {} 耗时：{} 秒", projectHttpUrl, watch.getTotalTimeSeconds());
        }

        log.info("{}", JobStatusEnum.CLONE_DONE.getStatusDesc());
    }


    @Override
    public void compileProject(String repositoryDir, @Nullable String mvnSettingXml) {
        Assert.hasText(repositoryDir, "repositoryDir must not be null or empty");
        log.info("{}", JobStatusEnum.COMPILING.getStatusDesc());
        OsInfo osInfo = new OsInfo();
        String[] execCmd = osInfo.isWindows() ? new String[]{"cmd", "/c", String.format(WIN_COMPILE_CMD, repositoryDir, mvnSettingXml,
                repositoryDir + Constants.JACOCO_LOG)} : new String[]{"/bin/sh", "-c", String.format(LINUX_COMPILE_CMD, repositoryDir,
                mvnSettingXml, repositoryDir + Constants.JACOCO_LOG)};
        try {
            log.info("===================脚本参数==================\n {}", JSON.toJSONString(execCmd, SerializerFeature.PrettyFormat));
            ShellUtils.executeShell(execCmd);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("{}", JobStatusEnum.COMPILE_FAIL.getStatusDesc());
            throw new ServiceException("编译错误！", e);
        }
        log.info("{}", JobStatusEnum.COMPILE_DONE.getStatusDesc());
    }

}
