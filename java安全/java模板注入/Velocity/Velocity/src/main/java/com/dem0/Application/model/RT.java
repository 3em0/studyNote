package com.dem0.Application.model;


public class RT<T> {
    /**
     * 标识返回状态
     */
    private Integer code;

    /**
     * 标识返回内容
     */
    private T data;
    /**
     * 标识返回消息
     */
    private String message;


    /**
     * 成功返回
     *
     * @param data
     * @return
     */
    public static <T> RT ok(T data) {
        return new RT(RHttpStatusEnum.SUCCESS.getCode(), data, RHttpStatusEnum.SUCCESS.getMessage());
    }

    /**
     * 成功返回
     *
     * @param data
     * @return
     */
    public static <T> RT ok(T data, String message) {
        return new RT(RHttpStatusEnum.SUCCESS.getCode(), data, message);
    }

    /**
     * 失败返回
     *
     * @param rHttpStatusEnum
     * @return
     */
    public static RT error(RHttpStatusEnum rHttpStatusEnum) {
        return new RT(rHttpStatusEnum.getCode(), null, rHttpStatusEnum.getMessage());
    }
    public static RT error(Integer code,String message){
        RT r = new RT();
        r.code(code);
        r.data(null);
        r.message(message);
        return r;
    }

    public RT() {

    }

    public RT(Integer code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public RT code(Integer code) {
        this.code = code;
        return this;
    }

    public Object getData() {
        return data;
    }

    public RT data(T data) {
        this.data = data;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public RT message(String message) {
        this.message = message;
        return this;
    }
}


