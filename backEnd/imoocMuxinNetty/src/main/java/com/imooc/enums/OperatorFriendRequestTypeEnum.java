package com.imooc.enums;

/**
 * Created by donezio on 12/24/18.
 */
public enum OperatorFriendRequestTypeEnum {

    IGNORE(0, "ignore"),
    PASS(1, "accept");

    public final Integer type;
    public final String msg;

    OperatorFriendRequestTypeEnum(Integer type, String msg){
        this.type = type;
        this.msg = msg;
    }

    public Integer getType() {
        return type;
    }

    public static String getMsgByType(Integer type) {
        for (OperatorFriendRequestTypeEnum operType : OperatorFriendRequestTypeEnum.values()) {
            if (operType.getType() == type) {
                return operType.msg;
            }
        }
        return null;
    }

}