package com.imooc.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * Created by donezio on 12/21/18.
 */
public class WSServerInitialzer extends ChannelInitializer<SocketChannel> {
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();

        // websocket ??http???????http????
        pipeline.addLast(new HttpServerCodec());
        // ?????????
        pipeline.addLast(new ChunkedWriteHandler());
        // ?httpMessage????????FullHttpRequest?FullHttpResponse
        // ???netty???????????hanler
        pipeline.addLast(new HttpObjectAggregator(1024*64));

        // ====================== ???????http??    ======================

        // ====================== ?????httpWebsocket ======================

        /**
         * websocket ???????????????????????? : /ws
         * ?handler??????????????
         * ?????????? handshaking?close, ping, pong? ping + pong = ??
         * ??websocket??????frames????????????????frames???
         */
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        // ????handler
        pipeline.addLast(new ChatHandler());
    }
}
