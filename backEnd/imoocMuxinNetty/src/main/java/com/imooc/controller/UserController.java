package com.imooc.controller;

import com.imooc.enums.OperatorFriendRequestTypeEnum;
import com.imooc.enums.SearchFriendsStatusEnum;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.UsersBO;
import com.imooc.pojo.vo.MyFriendsVO;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.UserService;
import com.imooc.utils.FastDFSClient;
import com.imooc.utils.FileUtils;
import com.imooc.utils.IMoocJSONResult;
import com.imooc.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created by donezio on 12/22/18.
 */
@RestController
@RequestMapping("u")
public class UserController {

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private UserService userService;

    @PostMapping("/registOrLogin")
    public IMoocJSONResult registOrLogin(@RequestBody Users user) throws Exception {

        if (StringUtils.isBlank(user.getUsername())
                || StringUtils.isBlank(user.getPassword())) {
            return IMoocJSONResult.errorMsg("username or password can not be null...");
        }



        boolean usernameIsExist = userService.queryUsernameIsExist(user.getUsername());
        Users userResult = null;
        if (usernameIsExist) {
            // Log in
            userResult = userService.queryUserForLogin(user.getUsername(), MD5Utils.getMD5Str(user.getPassword()));
            if (userResult == null) {
                return IMoocJSONResult.errorMsg("password or username incorrect");
            }
        } else {
            // Regist
            user.setNickname(user.getUsername());
            user.setFaceImage("");
            user.setFaceImageBig("");
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
            userResult = userService.saveUser(user);
        }

        UsersVO userVO = new UsersVO();
        BeanUtils.copyProperties(userResult, userVO);

        return IMoocJSONResult.ok(userVO);
    }

    @PostMapping("/uploadFaceBase64")
    public IMoocJSONResult uploadFaceBase64(@RequestBody UsersBO userBO) throws Exception {

        // get base64 data from front-end and convert to file
        String base64Data = userBO.getFaceData();
        String userFacePath = "/Users/macbookpro/Desktop/wechat/data" + userBO.getUserId() + "userface64.png";
        FileUtils.base64ToFile(userFacePath, base64Data);

        // upload to fastdfs
        MultipartFile faceFile = FileUtils.fileToMultipart(userFacePath);
        String url = fastDFSClient.uploadBase64(faceFile);
        System.out.println(url);


        String thump = "_80x80.";
        String arr[] = url.split("\\.");
        String thumpImgUrl = arr[0] + thump + arr[1];

        Users user = new Users();
        user.setId(userBO.getUserId());
        user.setFaceImage(thumpImgUrl);
        user.setFaceImageBig(url);

        Users result = userService.updateUserInfo(user);

        return IMoocJSONResult.ok(result);
    }

    @PostMapping("/setNickname")
    public IMoocJSONResult setNickname(@RequestBody UsersBO userBO) throws Exception {

        Users user = new Users();
        user.setId(userBO.getUserId());
        user.setNickname(userBO.getNickname());

        Users result = userService.updateUserInfo(user);

        return IMoocJSONResult.ok(result);
    }

    @PostMapping("/search")
    public IMoocJSONResult searchUser(String myUserId, String friendUsername)
            throws Exception {

        // 0. 判断 myUserId friendUsername 不能为空
        if (StringUtils.isBlank(myUserId)
                || StringUtils.isBlank(friendUsername)) {
            return IMoocJSONResult.errorMsg("");
        }

        Integer status = userService.preconditionSearchFriends(myUserId, friendUsername);
        if (status == SearchFriendsStatusEnum.SUCCESS.status) {
            Users user = userService.queryUserInfoByUsername(friendUsername);
            UsersVO userVO = new UsersVO();
            BeanUtils.copyProperties(user, userVO);
            return IMoocJSONResult.ok(userVO);
        } else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return IMoocJSONResult.errorMsg(errorMsg);
        }
    }

    @PostMapping("/addFriendRequest")
    public IMoocJSONResult addFriendRequest(String myUserId, String friendUsername)
            throws Exception {

        // Check for null
        if (StringUtils.isBlank(myUserId)
                || StringUtils.isBlank(friendUsername)) {
            return IMoocJSONResult.errorMsg("");
        }

        Integer status = userService.preconditionSearchFriends(myUserId, friendUsername);
        if (status == SearchFriendsStatusEnum.SUCCESS.status) {
            userService.sendFriendRequest(myUserId, friendUsername);
        } else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return IMoocJSONResult.errorMsg(errorMsg);
        }

        return IMoocJSONResult.ok();
    }

    @PostMapping("/queryFriendRequests")
    public IMoocJSONResult queryFriendRequests(String userId) {

        // 0. 判断不能为空
        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.errorMsg("");
        }

        // 1. 查询用户接受到的朋友申请
        return IMoocJSONResult.ok(userService.queryFriendRequestList(userId));
    }

    @PostMapping("/operFriendRequest")
    public IMoocJSONResult operFriendRequest(String acceptUserId, String sendUserId,
                                             Integer operType) {

        // not null
        if (StringUtils.isBlank(acceptUserId)
                || StringUtils.isBlank(sendUserId)
                || operType == null) {
            return IMoocJSONResult.errorMsg("");
        }

        // illegal type
        if (StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))) {
            return IMoocJSONResult.errorMsg("");
        }

        if (operType == OperatorFriendRequestTypeEnum.IGNORE.type) {
            // ignore and delete
            userService.deleteFriendRequest(sendUserId, acceptUserId);
        } else if (operType == OperatorFriendRequestTypeEnum.PASS.type) {
            // accept
            userService.passFriendRequest(sendUserId, acceptUserId);
        }

        // update friend table
        List<MyFriendsVO> myFirends = userService.queryMyFriends(acceptUserId);

        return IMoocJSONResult.ok(myFirends);
    }


    @PostMapping("/myFriends")
    public IMoocJSONResult myFriends(String userId) {
        // 0. userId 判断不能为空
        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.errorMsg("");
        }

        // 1. 数据库查询好友列表
        List<MyFriendsVO> myFirends = userService.queryMyFriends(userId);

        return IMoocJSONResult.ok(myFirends);
    }

    @PostMapping("/getUnReadMsgList")
    public IMoocJSONResult getUnReadMsgList(String acceptUserId) {
        // 0. userId 判断不能为空
        if (StringUtils.isBlank(acceptUserId)) {
            return IMoocJSONResult.errorMsg("");
        }

        // 查询列表
        List<com.imooc.pojo.ChatMsg> unreadMsgList = userService.getUnReadMsgList(acceptUserId);

        return IMoocJSONResult.ok(unreadMsgList);
    }

}
