# 工程简介

### jacoco 简单实现代码覆盖率全量采集

#### jacoco-coverage-reporter-native

- doc
    - settings.xml 存放maven配置文件 建议修改 localRepository属性指向你的maven仓库地址，避免重复下载jar包，影响采集效率
    - jacoco-0.8.8.zip 最新的jacoco包，当前该服务需要自己在服务器配置jacoco采集客户端

#### 服务器配置jacoco

```text
jacoco 配置项  
-javaagent:/data/web/rcl/light/jacocoagent.jar=includes=*,output=tcpserver,port=19078,address=0.0.0.0,append=true 
```

#### 切换yaml配置
```text
application.properties

# 切换 yaml配置
spring.profiles.active=dev

application-{active}.yaml

```

#### application.yaml 配置说明

```text
jacoco:
  report:
    config:
    # 支持采集不同git project
      gitProjectList:
        - httpUrlToRepo: http://gitlab.net/rcl/project/web/cloud2.0.git
          #projectId: 1134
          branch: master （填写服务器部署的分支）
          gitCloneRootDir: D:\\jacoco  （填写下载编译代码到你的本地路径）
          personalAccessToken: xxxxx  （gitlab个人授权码） 查看personalAccessToken 获取说明
          mvnSettingPath: D:\\jacoco\\settings-rcl-2.xml  指定你本地mvn库，轻云的mvn库和CDCmvn库不一样，需要注意（需要本地安装maven支持）
          jacocoExecConfigList:
             # 支持子模块微服务部署场景
            - { address: 172.28.100.211,moduleName: cloud-user,modulePathList: [ "/cloud2.0/cloud-user/cloud-user-server" ],port: 19073 }
            - { address: 172.28.100.211,moduleName: cloud-auth,modulePathList: [ "/cloud2.0/cloud-auth/cloud-auth-server" ],port: 19074 }
            - { address: 172.28.100.211,moduleName: cloud-terminal,modulePathList: [ "/cloud2.0/cloud-terminal/cloud-terminal-server" ],port: 19075 }
            - { address: 172.28.100.211,moduleName: cloud-behavior,modulePathList: [ "/cloud2.0/cloud-behavior/cloud-behavior-server" ],port: 19076 }
            - { address: 172.28.100.211,moduleName: cloud-socket,modulePathList: [ "/cloud2.0/cloud-socket/cloud-socket-server" ],port: 19077 }
            - { address: 172.28.100.211,moduleName: cloud-portal,modulePathList: [ "/cloud2.0/cloud-portal" ],port: 19078 }



```

#### jacocoExecConfigList 说明

```text
    address: 采集服务器IP 
    port：jacoco采集端口
    moduleName：给采集的jacoco.exec命名，避免重复 {moduleName}_jacoco.exec 
    modulePath:微服务部署的子模块路径，主要用于配置编译后识别工程的 classes 和 代码源文件路径
      modulePath 生成report 报告需要
      java -jar jacococli.jar report ./jacoco-demo.exec --classfiles /Users/oukotoshuu/IdeaProjects/demo/target/classes --sourcefiles /Users/oukotoshuu/IdeaProjects/demo/src/main/java --html html_report
      设置 --classfiles 和 --sourcefiles 的作用
```

#### personalAccessToken 获取说明

```text
使用你的账号登陆到gitlab
访问 http://gitlab.rcd.ruijie.net/profile/personal_access_tokens 该路径

创建一个个人授权token
Add a personal access token
    -- name 随意填写名称
    -- Expires at  采用默认，永久生效
    -- Scopes  
        api
        read_user
        read_repository
        write_repository 可选勾选，其他建议都勾选
        
    点击创建 Create personal access token
    将获取 Your New Personal Access Token 
    复制该token 如：p7Fz6gt3Apy3jBoYdWFV 
    填写到yaml配置项中
    
```

#### 开放端口

```text
查看firewall的状态
firewall-cmd --state
 
开放端口
firewall-cmd --permanent --add-port=18199/tcp
firewall-cmd --permanent --add-port=8080-8085/tcp
 
查看防火墙的开放的端口
firewall-cmd --permanent --list-ports
 
重启防火墙(修改配置后要重启防火墙)
firewall-cmd --reload
 
最后可以输入相应的ip地址查看tomcat是否启动

```

#### 实现方案

```text
参考 org.jacoco.cli-0.8.8.jar
commonds
    ClassInfo 在提供的位置打印有关Java类文件的信息
    Dump 以“tcpserver”输出模式运行的JaCoCo代理请求执行数据
    ExecInfo 以可读格式打印exec文件内容
    Instrument Java类文件和JAR文件的离线检测。
    Report 通过读取exec和Java类文件生成不同格式的报告
    Version jacoco版本
    Merge 将多个exec文件合并到一个新文件中。

当前工程实现逻辑
1、拉取git项目
    - 采用org.eclipse.jgit包，拉取远程git代码
2、编译git项目
   - 本地需要安装apache maven库
   - 会执行  mvn -s %s clean compile -Dmaven.test.skip=true 
   - 编译的日志文件将自动通过浏览器形式打开，需自行手动刷新 
3、下载远程jacoco.exec
    - 实现逻辑参考 org.jacoco.cli-0.8.8.jar Dump实现
4、生成报告
    - 实现逻辑参考 org.jacoco.cli-0.8.8.jar Report实现
5.打开windows报告路径 
    - 报告生成后将自动通过浏览器形式打开

```

#### 服务启动说明

```text
java -jar jacoco-coverage-reporter-native-0.0.1-SNAPSHOT.jar

有修改application.yaml 指定生效方式 
--spring.config.location=D:\\application.yaml
如下所示（jacoco-coverage-reporter-native-0.0.1-SNAPSHOT.jar 和 application.yaml 同级目录配置如下）
java -jar jacoco-coverage-reporter-native-0.0.1-SNAPSHOT.jar --spring.config.location=application.yaml
```

#### 参考

```text
https://blog.csdn.net/github_35735591/article/details/124230774
https://blog.csdn.net/weixin_45625187/article/details/109198027
https://github.com/lqnasa/jacocoReportTools/blob/master/pom.xml
https://www.cnblogs.com/wang1001/p/14983279.html
jacoco生成覆盖率报告的几种方式
https://blog.csdn.net/kang123488/article/details/85101289
jacoco覆盖率统计
https://blog.csdn.net/qq_14879165/article/details/122987386
https://www.cnblogs.com/liuyitan/p/15716027.html

```