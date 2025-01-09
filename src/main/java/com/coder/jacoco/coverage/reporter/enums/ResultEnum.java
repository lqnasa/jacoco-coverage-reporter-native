package com.coder.jacoco.coverage.reporter.enums;


public enum ResultEnum {

    /**
     * 未知错误
     */
    UNKNOWN_ERROR(-1, "unknown error"),
    /**
     * 成功
     */
    SUCCESS(0, "success"),
    /**
     * 错误
     */
    ERROR(1, "error");

    private int code;
    private String message;

    ResultEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
