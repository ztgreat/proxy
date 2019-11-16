package com.proxy.server.handler.traffic.handler;

import com.proxy.server.handler.traffic.TrafficCollector;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.InetSocketAddress;

/**
 * 流量统计 Handler
 *
 * @author ztgreat
 */
@ChannelHandler.Sharable
public class TrafficCollectionHandler extends ChannelDuplexHandler {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        InetSocketAddress sa = (InetSocketAddress) ctx.channel().localAddress();
        TrafficCollector trafficCollector = TrafficCollector.getCollector(sa.getPort());
        trafficCollector.incrementReadBytes(((ByteBuf) msg).readableBytes());
        trafficCollector.incrementReadMsgs(1);
        ctx.fireChannelRead(msg);
    }


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        InetSocketAddress sa = (InetSocketAddress) ctx.channel().localAddress();
        TrafficCollector trafficCollector = TrafficCollector.getCollector(sa.getPort());
        trafficCollector.incrementWriteBytes(((ByteBuf) msg).readableBytes());
        trafficCollector.incrementWriteMsgs(1);
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress sa = (InetSocketAddress) ctx.channel().localAddress();
        TrafficCollector.getCollector(sa.getPort()).getChannels().incrementAndGet();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress sa = (InetSocketAddress) ctx.channel().localAddress();
        TrafficCollector.getCollector(sa.getPort()).getChannels().decrementAndGet();
        super.channelInactive(ctx);
    }

}