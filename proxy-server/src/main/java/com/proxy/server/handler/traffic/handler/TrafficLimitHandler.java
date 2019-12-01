package com.proxy.server.handler.traffic.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 限流 Handler
 * 共享模式的 handler
 *
 * @author ztgreat
 */
@ChannelHandler.Sharable
public class TrafficLimitHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

}