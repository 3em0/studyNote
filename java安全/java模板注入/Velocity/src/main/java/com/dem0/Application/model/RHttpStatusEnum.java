package com.dem0.Application.model;


/**
 * @Author Joker DJ
 * @Date 2021/8/7 21:16
 * @Version 1.0
 */
public enum RHttpStatusEnum {

    SUCCESS(200,"success"),
    HTTP_NOT_FOUND(404,"未找到相关内容"),
    SERVER_ERROR( 500, "服务器忙，请稍后在试");
    private final int code;
    private final String message;
    RHttpStatusEnum(Integer code,String message){
        this.code=code;
        this.message=message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}



