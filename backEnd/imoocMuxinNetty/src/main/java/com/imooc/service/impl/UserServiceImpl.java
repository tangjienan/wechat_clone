package com.imooc.service.impl;

import com.imooc.enums.MsgActionEnum;
import com.imooc.enums.MsgSignFlagEnum;
import com.imooc.enums.SearchFriendsStatusEnum;
import com.imooc.mapper.*;
import com.imooc.netty.ChatMsg;
import com.imooc.netty.DataContent;
import com.imooc.netty.UserChannelRel;
import com.imooc.pojo.FriendsRequest;
import com.imooc.pojo.MyFriends;
import com.imooc.pojo.Users;
import com.imooc.pojo.vo.FriendRequestVO;
import com.imooc.pojo.vo.MyFriendsVO;
import com.imooc.service.UserService;
import com.imooc.utils.FastDFSClient;
import com.imooc.utils.FileUtils;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.QRCodeUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by donezio on 12/22/18.
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private FriendsRequestMapper friendsRequestMapper;

    @Autowired
    private UsersMapper userMapper;

    @Autowired
    private Sid sid;

    @Autowired
    private QRCodeUtils qrCodeUtils;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private MyFriendsMapper myFriendsMapper;

    @Autowired
    private UsersMapperCustom usersMapperCustom;

    @Autowired
    private ChatMsgMapper chatMsgMapper;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username) {

        Users user = new Users();
        user.setUsername(username);

        Users result = userMapper.selectOne(user);

        return result != null ? true : false;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String pwd) {

        Example userExample = new Example(Users.class);
        Criteria criteria = userExample.createCriteria();

        criteria.andEqualTo("username", username);
        criteria.andEqualTo("password", pwd);

        Users result = userMapper.selectOneByExample(userExample);

        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users saveUser(Users user) {
        String userId = sid.nextShort();
        String qrCodePath = "/Users/macbookpro/Desktop/wechat/data" + userId + "qrcode.png";
        qrCodeUtils.createQRCode(qrCodePath, "muxin_qrcode:" + user.getUsername());
        MultipartFile qrCodeFile = FileUtils.fileToMultipart(qrCodePath);
        String qrCodeUrl = "";
        try {
            qrCodeUrl = fastDFSClient.uploadQRCode(qrCodeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        user.setQrcode(qrCodeUrl);
        user.setId(sid.nextShort());
        userMapper.insert(user);
        return user;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users updateUserInfo(Users user) {
        userMapper.updateByPrimaryKeySelective(user);
        return queryUserById(user.getId());
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    private Users queryUserById(String userId) {
        return userMapper.selectByPrimaryKey(userId);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Integer preconditionSearchFriends(String myUserId, String friendUsername) {

        Users user = queryUserInfoByUsername(friendUsername);

        // 1. Doesn't exist
        if (user == null) {
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }

        // 2. Account is yourself
        if (user.getId().equals(myUserId)) {
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }

        // 3. is your friend
        Example mfe = new Example(MyFriends.class);
        Criteria mfc = mfe.createCriteria();
        mfc.andEqualTo("myUserId", myUserId);
        mfc.andEqualTo("myFriendUserId", user.getId());
        MyFriends myFriendsRel = myFriendsMapper.selectOneByExample(mfe);
        if (myFriendsRel != null) {
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }

        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserInfoByUsername(String username) {
        Example ue = new Example(Users.class);
        Criteria uc = ue.createCriteria();
        uc.andEqualTo("username", username);
        return userMapper.selectOneByExample(ue);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void sendFriendRequest(String myUserId, String friendUsername) {


        Users friend = queryUserInfoByUsername(friendUsername);


        Example fre = new Example(FriendsRequest.class);
        Criteria frc = fre.createCriteria();
        frc.andEqualTo("sendUserId", myUserId);
        frc.andEqualTo("acceptUserId", friend.getId());
        FriendsRequest friendRequest = friendsRequestMapper.selectOneByExample(fre);
        // If request not exist
        if (friendRequest == null) {

            String requestId = sid.nextShort();

            FriendsRequest request = new FriendsRequest();
            request.setId(requestId);
            request.setSendUserId(myUserId);
            request.setAcceptUserId(friend.getId());
            request.setRequestDateTime(new Date());
            friendsRequestMapper.insert(request);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId) {
        return usersMapperCustom.queryFriendRequestList(acceptUserId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteFriendRequest(String sendUserId, String acceptUserId) {
        Example fre = new Example(FriendsRequest.class);
        Criteria frc = fre.createCriteria();
        frc.andEqualTo("sendUserId", sendUserId);
        frc.andEqualTo("acceptUserId", acceptUserId);
        friendsRequestMapper.deleteByExample(fre);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        System.out.println("hey i am saving friends");
        saveFriends(sendUserId, acceptUserId);
        saveFriends(acceptUserId, sendUserId);
        deleteFriendRequest(sendUserId, acceptUserId);
        Channel sendChannel = UserChannelRel.get(sendUserId);
        if (sendChannel != null) {

            DataContent dataContent = new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);

            sendChannel.writeAndFlush(
                    new TextWebSocketFrame(
                            JsonUtils.objectToJson(dataContent)));
        }


    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void saveFriends(String sendUserId, String acceptUserId) {
        MyFriends myFriends = new MyFriends();
        String recordId = sid.nextShort();
        myFriends.setId(recordId);
        myFriends.setMyFriendUserId(acceptUserId);
        myFriends.setMyUserId(sendUserId);
        myFriendsMapper.insert(myFriends);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<MyFriendsVO> queryMyFriends(String userId) {
        List<MyFriendsVO> myFirends = usersMapperCustom.queryMyFriends(userId);
        return myFirends;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public String saveMsg(ChatMsg chatMsg) {

        com.imooc.pojo.ChatMsg msgDB = new com.imooc.pojo.ChatMsg();
        String msgId = sid.nextShort();
        msgDB.setId(msgId);
        msgDB.setAcceptUserId(chatMsg.getReceiverId());
        msgDB.setSendUserId(chatMsg.getSenderId());
        msgDB.setCreateTime(new Date());
        msgDB.setSignFlag(MsgSignFlagEnum.unsign.type);
        msgDB.setMsg(chatMsg.getMsg());

        chatMsgMapper.insert(msgDB);

        return msgId;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateMsgSigned(List<String> msgIdList) {
        usersMapperCustom.batchUpdateMsgSigned(msgIdList);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<com.imooc.pojo.ChatMsg> getUnReadMsgList(String acceptUserId) {

        Example chatExample = new Example(com.imooc.pojo.ChatMsg.class);
        Criteria chatCriteria = chatExample.createCriteria();
        chatCriteria.andEqualTo("signFlag", 0);
        chatCriteria.andEqualTo("acceptUserId", acceptUserId);

        List<com.imooc.pojo.ChatMsg> result = chatMsgMapper.selectByExample(chatExample);

        return result;
    }
}