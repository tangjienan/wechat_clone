package com.imooc.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.time.LocalDateTime;

/**
 * Created by donezio on 12/21/18.
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    // ?????????????channle
    private static ChannelGroup clients =
            new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg)
            throws Exception {
        // ????????????
        String content = msg.text();
        System.out.println("???????" + content);

//		for (Channel channel: clients) {
//			channel.writeAndFlush(
//				new TextWebSocketFrame(
//						"[????]" + LocalDateTime.now()
//						+ "?????, ????" + content));
//		}
        // ???????????for?????
        clients.writeAndFlush(
                new TextWebSocketFrame(
                        "服务器在 " + LocalDateTime.now()
                                + "接收到消息, " + content));

    }

    /**
     * ?????????????????
     * ??????channle?????ChannelGroup??????
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        clients.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // ???handlerRemoved?ChannelGroup???????????channel
//		clients.remove(ctx.channel());
        System.out.println("long channle id: "
                + ctx.channel().id().asLongText());
        System.out.println("short channle id: "
                + ctx.channel().id().asShortText());
    }
}
