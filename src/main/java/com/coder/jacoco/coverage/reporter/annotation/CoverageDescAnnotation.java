package com.coder.jacoco.coverage.reporter.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CoverageDescAnnotation {

    /**
     * 描述
     *
     * @return str
     */
    String value();

}
