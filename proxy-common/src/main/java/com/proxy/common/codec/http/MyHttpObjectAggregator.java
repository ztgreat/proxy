package com.proxy.common.codec.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

/**
 * http 消息解码器
 */
public class MyHttpObjectAggregator extends HttpObjectAggregator {


    public MyHttpObjectAggregator(int maxContentLength) {
        super(maxContentLength);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {
        super.decode(ctx, msg, out);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }
}
