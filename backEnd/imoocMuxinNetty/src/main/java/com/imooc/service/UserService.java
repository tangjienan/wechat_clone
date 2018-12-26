package com.imooc.service;

import com.imooc.netty.ChatMsg;
import com.imooc.pojo.Users;
import com.imooc.pojo.vo.FriendRequestVO;
import com.imooc.pojo.vo.MyFriendsVO;

import java.util.List;

/**
 * Created by donezio on 12/22/18.
 */

public interface UserService {

    public boolean queryUsernameIsExist(String username);

    public Users queryUserForLogin(String username, String pwd);

    public Users saveUser(Users user);

    public Users updateUserInfo(Users user);

    public Integer preconditionSearchFriends(String myUserId, String friendUsername);

    public Users queryUserInfoByUsername(String username);

    public void sendFriendRequest(String myUserId, String friendUsername);

    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId);

    public void deleteFriendRequest(String sendUserId, String acceptUserId);

    public void passFriendRequest(String sendUserId, String acceptUserId);

    public List<MyFriendsVO> queryMyFriends(String userId);

    public String saveMsg(ChatMsg chatMsg);

    public void updateMsgSigned(List<String> msgIdList);

    public List<com.imooc.pojo.ChatMsg> getUnReadMsgList(String acceptUserId);

}
