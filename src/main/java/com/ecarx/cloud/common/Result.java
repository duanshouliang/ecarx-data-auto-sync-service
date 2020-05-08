package com.ecarx.cloud.common;

public class Result<T> {
    private boolean success;
    private Integer code;
    private T msg;

    public static Result<String> success(){
        Result<String> result = new Result<>();
        result.code = 200;
        result.success = true;
        result.msg = "success";
        return result;
    }

    public static <T>Result<T> failed(T msg){
        Result<T> result = new Result<>();
        result.success = false;
        result.code = 202;
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public Integer getCode() {
        return code;
    }

    public T getMsg() {
        return msg;
    }

    public static void main(String[] args) {
        Result<String> result = Result.failed("xxxxx");
    }
}
