package com.imooc.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;


public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent)evt;

            if (event.state() == IdleState.READER_IDLE) {
                System.out.println("read idle...");
            } else if (event.state() == IdleState.WRITER_IDLE) {
                System.out.println("write idle...");
            } else if (event.state() == IdleState.ALL_IDLE) {

                //System.out.println("channel close，users的数量为：" + ChatHandler.users.size());

                Channel channel = ctx.channel();
                // 关闭无用的channel，以防资源浪费
                channel.close();

                System.out.println("channel closed，current user number：" + ChatHandler.users.size());
            }
        }

    }

}
