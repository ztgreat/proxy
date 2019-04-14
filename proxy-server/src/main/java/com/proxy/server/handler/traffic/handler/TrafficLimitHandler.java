package com.proxy.server.handler.traffic.handler;

import com.google.common.util.concurrent.RateLimiter;
import com.proxy.server.ProxyServer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 限流 Handler
 * 共享模式的 handler
 * @author  ztgreat
 */
@ChannelHandler.Sharable
public class TrafficLimitHandler extends ChannelInboundHandlerAdapter {


    private static Logger logger = LoggerFactory.getLogger(TrafficLimitHandler.class);

    /**
     * 每秒 concurrent的并发量
     */
    private static RateLimiter rateLimiter = RateLimiter.create(ProxyServer.concurrent);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        rateLimiter.acquire();
        super.channelActive(ctx);
    }

}