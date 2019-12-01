package com.proxy.client.handler;


import com.proxy.client.service.ClientBeanManager;
import com.proxy.common.protobuf.ProxyMessage;
import com.proxy.common.protocol.CommonConstant;
import com.proxy.common.util.ProxyMessageUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartBeatReqHandler extends ChannelInboundHandlerAdapter {


    private static Logger logger = LoggerFactory.getLogger(HeartBeatReqHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof ProxyMessage) {

            ProxyMessage message = (ProxyMessage) msg;
            byte type = message.getType();
            //心跳响应消息
            if (type == CommonConstant.HearBeat.TYPE_HEARTBEAT_RESP) {
                logger.info("收到服务器心跳响应消息");
            } else {
                ctx.fireChannelRead(msg);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE:
                    handleReaderIdle(ctx);
                    break;
                case WRITER_IDLE:
                    handleWriterIdle(ctx);
                    break;
                case ALL_IDLE:
                    handleAllIdle(ctx);
                    break;
                default:
                    break;
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }

    }

    private void handleReaderIdle(ChannelHandlerContext ctx) {
        sendHeartBeat(ctx);
        logger.debug("READER_IDLE 读超时");
    }

    private void handleWriterIdle(ChannelHandlerContext ctx) {
        sendHeartBeat(ctx);
        logger.debug("WRITER_IDLE 写超时");
    }

    private void handleAllIdle(ChannelHandlerContext ctx) {

        sendHeartBeat(ctx);
        logger.debug("ALL_IDLE 写超时");
    }

    /**
     * 与服务器失去连接调用
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("发生异常:清理数据,客户端退出" + cause.getMessage());
        ClientBeanManager.getProxyService().clear();
        ctx.channel().close();
    }

    /**
     * 发送心跳包
     */
    private void sendHeartBeat(ChannelHandlerContext ctx) {
        if (ctx != null && ctx.channel().isActive()) {
            ProxyMessage heatBeat = ProxyMessageUtil.buildHeartBeatReq();
            ctx.writeAndFlush(heatBeat);
        }
    }

}
