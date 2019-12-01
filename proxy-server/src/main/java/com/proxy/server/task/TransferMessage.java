package com.proxy.server.task;


import com.proxy.common.protobuf.ProxyMessage;
import com.proxy.common.protocol.Message;
import com.proxy.common.util.ProxyMessageUtil;
import com.proxy.server.service.ServerBeanManager;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 转发消息
 */
public class TransferMessage implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(TransferMessage.class);

    private Message message;

    public TransferMessage(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        Channel channel;

        //2、该端口的代理客户端
        channel = message.getClientChannel();
        if (Objects.isNull(channel)) {
            logger.error("[转发消息]端口的代理客户端channel 为null");
            return;
        }

        //3、将数据重新封装 通过代理客户端的channel 发送出去
        ProxyMessage proxyMessage = ProxyMessageUtil.buildMsg(message.getSessionID()
                , message.getType()
                , message.getProxyType()
                , message.getPriority()
                , message.getCommand()
                , message.getData());

        try {
            channel.writeAndFlush(proxyMessage);
        } catch (Exception e) {
            logger.error("[转发消息]转发消息发生异常:", e);
            ServerBeanManager.getUserSessionService().get(message.getSessionID()).close();
        }
    }
}
