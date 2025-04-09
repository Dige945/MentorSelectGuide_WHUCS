package com.example.whucs_mentorguide.common;

public class Result<T> {
    private String code;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public String getCode(){return code;}
    public void setCode(String code){this.code = code;}

    public String getMsg(){return msg;}
    public void setMsg(String msg){this.msg = msg;}

    public T getData(){return data;}

    public void setData(T data){this.data = data;}

    public static <T> Result<T> success(){
        Result result = new Result<>();
        result.setCode("0");
        result.setMsg("成功");
        return result;
    }

    public static <T> Result<T> success(T data){
        return new Result<>("0", "成功", data);
    }

    public static Result error(String code,String msg){
        Result result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
