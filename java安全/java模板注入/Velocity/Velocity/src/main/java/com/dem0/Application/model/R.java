package com.dem0.Application.model;
public class R {
    /**
     *标识返回状态
     */
    private Integer code;

    /**
     * 标识返回内容
     */
    private Object data;
    /**
     * 标识返回消息
     */
    private String message;


    /**
     * 成功返回
     * @param data
     * @return
     */
    public static R ok(Object data){
        return new R(RHttpStatusEnum.SUCCESS.getCode(),data,RHttpStatusEnum.SUCCESS.getMessage());
    }

    /**
     * 成功返回
     * @param data
     * @return
     */
    public static R ok(Object data,String message){
        return new R(RHttpStatusEnum.SUCCESS.getCode(),data,message);
    }

    /**
     * 失败返回
     * @param rHttpStatusEnum
     * @return
     */
    public static R error(RHttpStatusEnum rHttpStatusEnum){
        return new R(rHttpStatusEnum.getCode(),null,rHttpStatusEnum.getMessage());
    }
    public static R error(Integer code,String message){
        R r = new R();
        r.code(code);
        r.data(null);
        r.message(message);
        return r;
    }



    public R() {

    }

    public R(Integer code, Object data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public R code(Integer code) {
        this.code = code;
        return this;
    }

    public Object getData() {
        return data;
    }

    public R data(Object data) {
        this.data = data;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public R message(String message) {
        this.message = message;
        return this;
    }
}


