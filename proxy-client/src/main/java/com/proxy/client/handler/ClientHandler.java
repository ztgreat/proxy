package com.proxy.client.handler;

import com.proxy.client.service.ClientBeanManager;
import com.proxy.common.entity.client.RealServer;
import com.proxy.common.protobuf.ProxyMessage;
import com.proxy.common.protocol.CommonConstant;
import com.proxy.common.util.ProxyMessageUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClientHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    /**
     * 用于连接真实服务器
     */
    private Bootstrap realServerBootStrap;

    public ClientHandler(Bootstrap realServerBootStrap) {
        this.realServerBootStrap = realServerBootStrap;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ProxyMessage message= (ProxyMessage) msg;
        byte type = message.getType();
        switch (type) {
            case CommonConstant.MessageType.TYPE_TRANSFER:
                handleTransferMessage(ctx, message);
                break;
            case CommonConstant.MessageType.TYPE_CONNECT_REALSERVER:
                handleConnectMessage(ctx, message);
                break;

            //代理客户端与真实服务器连接断开
            case CommonConstant.MessageType.TYPE_DISCONNECT:
                handleDisConnectMessage(ctx, message);
                break;

            default:
                break;
        }
    }
    private void handleTransferMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {
        Long sessionID = proxyMessage.getSessionID();

        RealServer realServer= ClientBeanManager.getProxyService().getRealServerChannel(sessionID);
        Channel realServerChannel = null;
        if ((realServer !=null) &&  (realServerChannel=realServer.getChannel()) != null) {

            byte requestType=proxyMessage.getProxyType();
            if (requestType== CommonConstant.ProxyType.TCP){

                //1.如果消息是tcp类型
                ByteBuf buf = ctx.alloc().buffer(proxyMessage.getData().length);
                buf.writeBytes(proxyMessage.getData());
                realServerChannel.writeAndFlush(buf);
                logger.debug("客户端转发tcp请求至真实服务器");

            }else if(requestType== CommonConstant.ProxyType.HTTP) {

                //2.如果消息是http类型
                ByteBuf buf = ctx.alloc().buffer(proxyMessage.getData().length);
                buf.writeBytes(proxyMessage.getData());
                ctx.fireChannelRead(buf);
            }

        }else {
            logger.debug("代理客户端未连接真实服务器,需要重新发起连接请求");

            // TODO: 2018/2/22 需要fix

            //方案1:通知服务器关闭用户的请求,让用户重新发起新的连接
            //目前:检测到和真实服务器失去连接后，会通知代理服务器断开与用户的连接
            proxyMessage = ProxyMessageUtil.buildReConnect(sessionID,null);
            ctx.channel().writeAndFlush(proxyMessage);

            //方案2:缓存数据,客户端重新与真实服务器建立连接,当连接建立成功后,再转发数据

        }
    }
    private void handleConnectMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {

        final Channel channel = ctx.channel();

        //会话id
        final Long sessionID = proxyMessage.getSessionID();
        //代理类型
        int proxyType=proxyMessage.getProxyType() & 0xFF;

        //代理服务器地址,用于重定向的时候替换header 中的Location地址
        final String proxyServer=new String(proxyMessage.getCommand());

        //真实服务器地址：ip:port
        String[] serverInfo = new String(proxyMessage.getData()).split(":");

        //真实服务器ip
        final  String ip = serverInfo[0];

        //真实服务器端口
        final  int port = Integer.parseInt(serverInfo[1]);


        realServerBootStrap.connect(ip, port).addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {

                    Channel realServerChannel = future.channel();

                    logger.debug("客户端连接真实服务器成功{}",ip+":"+port);

                    RealServer realServer = new RealServer();

                    realServer.setChannel(realServerChannel);
                    realServer.setRealHost(ip);
                    realServer.setRealHostPort(port);
                    realServer.setProxyType(proxyType);
                    realServer.setStatus(CommonConstant.ProxyStatus.ONLINE);

                    ClientBeanManager.getProxyService().addRealServerChannel(sessionID,realServer,realServerChannel,String.valueOf(proxyType),proxyServer);


                    ProxyMessage proxyMessage = ProxyMessageUtil.buildConnectSuccess(sessionID,null);
                    channel.writeAndFlush(proxyMessage);
                } else {
                    logger.error("客户端连接真实服务器({})失败:{}",ip+":"+port+" "+future.cause().getMessage());
                    ProxyMessage proxyMessage = ProxyMessageUtil.buildConnectFail(sessionID,null);
                    channel.writeAndFlush(proxyMessage);
                }
            }
        });
    }

    private void handleDisConnectMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {

        Long sessionID = proxyMessage.getSessionID();

        String[] serverInfo = new String(proxyMessage.getData()).split(":");
        final  String ip = serverInfo[0];
        final  int port = Integer.parseInt(serverInfo[1]);

        RealServer realServer =ClientBeanManager.getProxyService().getRealServerChannel(sessionID);
        Channel realServerChannel=null ;
        if (realServer !=null && (realServerChannel=realServer.getChannel()) != null) {
            realServerChannel.close();
            ClientBeanManager.getProxyService().removeRealServerChannel(sessionID);
            logger.debug("客户端与真实服务器{}断开",ip+":"+port);
        }

    }


    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {

        logger.debug("客户端和代理服务器连接通道可写");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        ClientBeanManager.getProxyService().clear();
        ctx.channel().close();
        logger.error("发生异常:清理数据,客户端退出"+cause.getMessage());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ClientBeanManager.getProxyService().clear();
        ctx.channel().close();
        logger.info("和服务器连接断开:清理数据");
    }
}
