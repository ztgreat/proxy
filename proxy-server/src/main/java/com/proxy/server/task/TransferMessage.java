package com.proxy.server.task;


import com.proxy.common.protobuf.ProxyMessageProtos;
import com.proxy.common.protocol.Message;
import com.proxy.common.util.LoggerUtils;
import com.proxy.common.util.ProxyMessageUtil;
import com.proxy.server.service.ServerBeanManager;
import io.netty.channel.Channel;

/**
 * 转发消息
 */
public class TransferMessage implements Runnable {


    private  Message message;

    public TransferMessage(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        Channel channel=null;

        //1、取出数据
        if (message==null){
            return;
        }

        //2、该端口的代理客户端
        channel=message.getClientChannel();
        if (channel==null){
            return;
        }

        //3、将数据重新封装 通过代理客户端的channel 发送出去

        ProxyMessageProtos.ProxyMessage proxyMessage= ProxyMessageUtil.buildMsg(message.getSessionID()
                ,message.getType()
                ,message.getProxyType()
                ,message.getPriority()
                ,message.getCommand()
                ,message.getData());

        try {
            channel.writeAndFlush(proxyMessage);
        }catch (Exception e){
            LoggerUtils.info(this.getClass(), "转发消息发生异常:"+e.getMessage());
            if (channel!=null)
                ServerBeanManager.getUserSessionService().get(message.getSessionID()).close();
        }
    }
}
