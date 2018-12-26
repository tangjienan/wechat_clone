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

        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new ChunkedWriteHandler());

        pipeline.addLast(new HttpObjectAggregator(1024*64));

        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        pipeline.addLast(new ChatHandler());
    }
}
