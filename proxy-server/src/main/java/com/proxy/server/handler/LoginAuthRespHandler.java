package com.proxy.server.handler;


import com.proxy.common.entity.server.ClientNode;
import com.proxy.common.protobuf.ProxyMessage;
import com.proxy.common.protocol.CommonConstant;
import com.proxy.common.util.ProxyMessageUtil;
import com.proxy.server.service.ServerBeanManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * 登录安全认证响应 handler
 */
public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {


    private static Logger logger = LoggerFactory.getLogger(LoginAuthRespHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {


        if (msg instanceof ProxyMessage) {
            ProxyMessage message = (ProxyMessage) msg;

            //获取消息类型
            byte type = message.getType();

            //如果是心跳请求消息
            if (type == CommonConstant.Login.TYPE_LOGIN_REQ) {

                Channel userChannel = ctx.channel();
                InetSocketAddress sa = (InetSocketAddress) userChannel.remoteAddress();

                logger.info("客户端({})请求登录认证", sa.getHostName());

                String clientKey = new String(message.getData());


                ClientNode clientNode;
                clientNode = ServerBeanManager.getClientService().get(clientKey);
                if (clientNode != null && clientNode.getStatus() == CommonConstant.ClientStatus.ONLINE) {

                    if (sa.getHostName().equals(clientNode.getHost())) {
                        //同一个客户端再次登录
                        //关闭连接
                        closeChannle(ctx);
                        return;
                    }

                    //已经存在一个相同key的客户端登录了
                    String loginMsg = "登录失败:已经存在一个相同key的客户端登录了";
                    loginRespone(ctx, loginMsg, CommonConstant.Login.LOGIN_FAIL);
                    closeChannle(ctx);
                    return;
                }

                if (clientNode != null && clientNode.getStatus() != CommonConstant.ClientStatus.FORBIDDEN) {
                    //登录响应
                    loginRespone(ctx, "登录成功", CommonConstant.Login.LOGIN_SUCCESS);
                    //保存客户端信息
                    saveClient2Cache(clientNode, ctx, message);
                    logger.info("客户端({})登录成功", sa.getHostName());
                } else {
                    logger.info("客户端({})登录失败:客户端尚未注册或被禁止登录", sa.getHostName());
                    String loginMsg = "登录失败:客户端尚未注册或被禁止登录";
                    loginRespone(ctx, loginMsg, CommonConstant.Login.LOGIN_FAIL);
                    closeChannle(ctx);
                }

            } else {
                ctx.fireChannelRead(msg);
            }
        } else {
            //错误的消息格式
            //关闭用户连接
            closeChannle(ctx);
        }

    }


    /**
     * 保存or更新 client到内存,同时启动代理服务
     */
    private void saveClient2Cache(ClientNode client, ChannelHandlerContext ctx, ProxyMessage message) {

        String key = new String(message.getData());
        ctx.channel().attr(CommonConstant.ServerChannelAttributeKey.CLIENT_KEY).set(key);
        InetSocketAddress sa = (InetSocketAddress) ctx.channel().remoteAddress();
        client.setHost(sa.getAddress().getHostName());
        client.setPort(sa.getPort());
        client.setChannel(ctx.channel());
        client.setStatus(CommonConstant.ClientStatus.ONLINE);

        /**
         * 当客户端连接成功后(可能重启),把以前存在的用户连接关闭掉
         */
        Map<Long, Channel> sessionIDTOChannel = ServerBeanManager.getUserSessionService().getAll();
        for (Map.Entry<Long, Channel> entry : sessionIDTOChannel.entrySet()) {
            String tempClientKey = ServerBeanManager.getUserSessionService().getClientKey(entry.getValue());
            if (client.getClientKey().equals(tempClientKey)) {
                //从集合移除
                ServerBeanManager.getUserSessionService().remove(entry.getKey());
                //关闭用户连接
                entry.getValue().close();
                logger.info("{}:关闭失效的用户端连接", client.getClientKey());
            }
        }

        //添加客户端到代理服务集合
        ServerBeanManager.getClientService().add(client.getClientKey(), client);

    }

    /**
     * 登录响应
     */
    private void loginRespone(ChannelHandlerContext ctx, String msg, byte loginResult) {

        ProxyMessage loginResp = ProxyMessageUtil.buildLoginResp(new byte[]{loginResult}, msg.getBytes());
        ctx.writeAndFlush(loginResp);
    }

    private void closeChannle(ChannelHandlerContext ctx) {
        if (ctx != null && ctx.channel() != null && ctx.channel().isActive()) {
            Channel userChannel = ctx.channel();
            InetSocketAddress sa = (InetSocketAddress) userChannel.localAddress();
            logger.info("客户端({})认证失败或者连接异常", sa.getHostName());
            ctx.channel().close();
        }

    }


}
