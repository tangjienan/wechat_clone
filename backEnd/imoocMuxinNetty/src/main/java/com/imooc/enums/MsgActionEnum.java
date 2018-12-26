package com.imooc.enums;

/**
 * Created by donezio on 12/25/18.
 */
public enum MsgActionEnum {

    CONNECT(1, "initialize connection or reconnect"),
    CHAT(2, "message"),
    SIGNED(3, "message  received"),
    KEEPALIVE(4, "heartbeat"),
    PULL_FRIEND(5, "pull friends");

    public final Integer type;
    public final String content;

    MsgActionEnum(Integer type, String content){
        this.type = type;
        this.content = content;
    }

    public Integer getType() {
        return type;
    }


}
