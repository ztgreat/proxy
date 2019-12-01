package com.proxy.server.handler;

import com.proxy.common.entity.server.ClientNode;
import com.proxy.common.protobuf.ProxyMessage;
import com.proxy.common.protocol.CommonConstant;
import com.proxy.server.event.TransferEvent;
import com.proxy.server.service.ServerBeanManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 代理服务器 handler
 *
 * @author ztgreat
 */
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(ServerChannelHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ProxyMessage message = (ProxyMessage) msg;
        byte type = message.getType();
        switch (type) {
            // 代理客户端与真实服务器连接成功
            case CommonConstant.MessageType.TYPE_CONNECT_SUCCESS:
                handleConnectMessage(ctx, message);
                break;

            // 需要用户重新发起连接请求(当客户端断连再重新连接上后的情况)
            case CommonConstant.MessageType.TYPE_RECONNECT:
                handleReConnectMessage(ctx, message);
                break;
            // 转发代理客户端消息
            case CommonConstant.MessageType.TYPE_TRANSFER:
                handleTransferMessage(ctx, message);
                break;
            //代理客户端与真实服务器连接失败
            case CommonConstant.MessageType.TYPE_CONNECT_FAIL:
                handleDisConnectMessage(ctx, message);
                break;
            default:
                break;
        }
    }

    /**
     * 代理服务器接收到客户端发送的消息后,
     * 转发给 用户端 channle
     */
    private void handleTransferMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {

        long sessionId = proxyMessage.getSessionID();
        Channel userChannel = ServerBeanManager.getUserSessionService().get(sessionId);
        if (userChannel != null) {
            ByteBuf buf = ctx.alloc().buffer(proxyMessage.getData().length);
            buf.writeBytes(proxyMessage.getData());
            userChannel.writeAndFlush(buf);
            logger.debug("代理服务器将数据转发给用户");
        } else {
            logger.debug("没有用户channle,丢弃数据");
        }
    }

    /**
     * 代理客户端与真实服务器连接建立成功
     */
    private void handleConnectMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {

        String key = ServerBeanManager.getClientService().getClientKey(ctx.channel());
        ClientNode node = ServerBeanManager.getClientService().get(key);

        logger.debug("代理客户端({})与真实服务器连接成功", node.getClientKey());
        Long sessionId = proxyMessage.getSessionID();
        Channel userChannel = ServerBeanManager.getUserSessionService().get(sessionId);
        if (userChannel != null) {
            userChannel.pipeline().fireUserEventTriggered(new TransferEvent());
            userChannel.config().setAutoRead(true);
            logger.debug("用户请求访问通道设置为可读");
        }


    }

    /**
     * 需要用户重新发起请求
     */
    private void handleReConnectMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {

        long sessionId = proxyMessage.getSessionID();
        Channel userChannel = ServerBeanManager.getUserSessionService().get(sessionId);
        if (userChannel != null) {
            userChannel.close();
            ServerBeanManager.getUserSessionService().remove(sessionId);
            logger.debug("关闭用户连接,需要重新请求");
        }
    }

    /**
     * 代理客户端与真实服务器连接失败
     */
    private void handleDisConnectMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {

        logger.info("代理客户端与真实服务器连接失败");
        long sessionId = proxyMessage.getSessionID();
        Channel userChannel = ServerBeanManager.getUserSessionService().get(sessionId);
        if (userChannel != null) {
            logger.info("关闭请求访问通道");
            userChannel.close();
            ServerBeanManager.getUserSessionService().remove(sessionId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        exceptionHandle(ctx.channel());
    }

    /**
     * 代理服务器发生异常
     */
    private void exceptionHandle(Channel channel) {
        // 获取客户端key
        String key = ServerBeanManager.getClientService().getClientKey(channel);
        //获取客户端节点信息
        ClientNode node = ServerBeanManager.getClientService().get(key);
        if (node != null) {
            node.setStatus(CommonConstant.ClientStatus.OFFLINE);
            channel.close();
            logger.error("异常:代理服务器与客户端({})失去连接", node.getClientKey());
        }
    }
}
