package com.imooc.netty;

import com.imooc.utils.SpringUtil;
import com.imooc.enums.MsgActionEnum;
import com.imooc.service.UserService;
import com.imooc.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by donezio on 12/21/18.
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    public static ChannelGroup users =
            new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg)
            throws Exception {
        // message from front end, convert to json first
        String content = msg.text();
        Channel currentChannel = ctx.channel();

        // 1 get message
        System.out.println(content);
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        Integer action = dataContent.getAction();
        // 2 check message type
        if (action == MsgActionEnum.CONNECT.type) {
            //  2.1 initial channel, connect userID and channel ID
            String senderId = dataContent.getChatMsg().getSenderId();
            UserChannelRel.put(senderId, currentChannel);
            // test
            for (Channel c : users) {
                System.out.println("channel long id "+ c.id().asLongText());
            }
            UserChannelRel.output();
        }
        else if (action == MsgActionEnum.CHAT.type) {
            //  2.2 message, save message to database, unsigned message

            //  extract message
            ChatMsg chatMsg = dataContent.getChatMsg();
            String msgText = chatMsg.getMsg();
            String receiverId = chatMsg.getReceiverId();
            String senderId = chatMsg.getSenderId();

            // manually acquire spring bean, need to convert type from object
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
            String msgId = userService.saveMsg(chatMsg);
            chatMsg.setMsgId(msgId);

            DataContent dataContentMsg = new DataContent();
            dataContentMsg.setChatMsg(chatMsg);

            // send message
            // get channel id for recerive
            Channel receiverChannel = UserChannelRel.get(receiverId);
            if (receiverChannel == null) {
                // user is not online, used push notification
            } else {
                Channel findChannel = users.find(receiverChannel.id());
                if (findChannel != null) {
                    // online
                    receiverChannel.writeAndFlush(
                            new TextWebSocketFrame(
                                    JsonUtils.objectToJson(dataContentMsg)));
                } else {
                    // offLine
                }
            }
        }
        else if (action == MsgActionEnum.SIGNED.type) {
            //  2.3 signed message
            UserService userService = (UserService)SpringUtil.getBean("userServiceImpl");

            // used extend for singed message, seperate by comma
            String msgIdsStr = dataContent.getExtand();
            String msgIds[] = msgIdsStr.split(",");

            List<String> msgIdList = new ArrayList<>();
            for (String mid : msgIds) {
                if (StringUtils.isNotBlank(mid)) {
                    msgIdList.add(mid);
                }
            }

            System.out.println(msgIdList.toString());

            if (msgIdList != null && !msgIdList.isEmpty() && msgIdList.size() > 0) {
                // batch process
                userService.updateMsgSigned(msgIdList);
            }


        } else if (action == MsgActionEnum.KEEPALIVE.type) {
            System.out.println("heartbeat signal from client");
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        users.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asShortText();
        System.out.println("client removed，channel id ：" + channelId);
        users.remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //closed channel and remove it from channel grounp when there is exception
        ctx.channel().close();
        users.remove(ctx.channel());
    }
}
