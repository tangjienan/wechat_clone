package com.imooc.enums;

/**
 * Created by donezio on 12/25/18.
 */
public enum MsgSignFlagEnum {

    unsign(0, "unsigned"),
    signed(1, "signed");

    public final Integer type;
    public final String content;

    MsgSignFlagEnum(Integer type, String content){
        this.type = type;
        this.content = content;
    }

    public Integer getType() {
        return type;
    }
}