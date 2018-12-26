package com.imooc.enums;

/**
 * Created by donezio on 12/24/18.
 */
public enum SearchFriendsStatusEnum {

    SUCCESS(0, "OK"),
    USER_NOT_EXIST(1, "no user..."),
    NOT_YOURSELF(2, "cant add yourself..."),
    ALREADY_FRIENDS(3, "already a friend...");

    public final Integer status;
    public final String msg;

    SearchFriendsStatusEnum(Integer status, String msg){
        this.status = status;
        this.msg = msg;
    }

    public Integer getStatus() {
        return status;
    }

    public static String getMsgByKey(Integer status) {
        for (SearchFriendsStatusEnum type : SearchFriendsStatusEnum.values()) {
            if (type.getStatus() == status) {
                return type.msg;
            }
        }
        return null;
    }

}