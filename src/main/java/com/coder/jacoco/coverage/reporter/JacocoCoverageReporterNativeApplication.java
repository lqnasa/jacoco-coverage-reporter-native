package com.coder.jacoco.coverage.reporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JacocoCoverageReporterNativeApplication {

    public static void main(String[] args) {
        //在application中的自动关闭方法
        System.exit(SpringApplication.exit(SpringApplication.run(JacocoCoverageReporterNativeApplication.class, args)));
//        SpringApplication.run(JacocoCoverageReporterNativeApplication.class, args);
    }

}
