package com.proxy.server.handler;


import com.proxy.common.entity.server.ClientNode;
import com.proxy.common.entity.server.ProxyChannel;
import com.proxy.common.entity.server.ProxyRealServer;
import com.proxy.common.protocol.CommonConstant;
import com.proxy.common.protocol.Message;
import com.proxy.server.service.ServerBeanManager;
import com.proxy.server.util.ProxyUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 通知:代理客户端与真实服务器建立连接
 * <p>
 * 当用户第一个http请求到来时，NoticeChannelHandler会解析头部
 * 取出请求域名,然后根据域名通知客户端和后台建立连接，然后向上传递这个请求
 *
 * @author ztgreat
 */
public class HttpNoticeChannelHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpNoticeChannelHandler.class);

    public HttpNoticeChannelHandler() {
        super();
    }

    /**
     * 为用户连接产生ID
     */
    private static Long getSessionID() {
        return ServerBeanManager.getSessionIDGenerate().generateId();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        Channel userChannel = ctx.channel();
        Long sessionID = ServerBeanManager.getUserSessionService().getSessionID(userChannel);
        if (msg instanceof HttpRequest) {

            HttpRequest request = (HttpRequest) msg;
            if (Objects.nonNull(sessionID)) {
                //向上传递消息
                ctx.fireChannelRead(msg);
                return;
            }
            //需要先建立连接
            ctx.channel().config().setAutoRead(false);

            //获取 key,可能是端口,也可能是域名
            Object proxyKey = ProxyUtil.getKey(request.headers().get(HttpHeaderNames.HOST), userChannel);

            //保存代理key 到用户 channel
            userChannel.attr(CommonConstant.UserChannelAttributeKey.PROXY_KEY).set(proxyKey.toString());

            //根据域名获取代理信息
            ProxyChannel proxyChannel = ServerBeanManager.getProxyChannelService().getServerProxy(proxyKey);

            //如果不存在domain,那么不存在该代理客户端，后续理应不再判断，暂时为了避免考虑不全，先判断多次
            if (proxyChannel == null) {
                closeChannle(ctx);
                ReferenceCountUtil.release(msg);
                logger.error("{}:代理客户端没有此代理通道信息", proxyKey);
                return;
            }

            ClientNode node = ServerBeanManager.getClientService().get(proxyChannel.getClientKey());
            if (node == null || node.getChannel() == null || node.getStatus() != CommonConstant.ClientStatus.ONLINE) {
                closeChannle(ctx);
                ReferenceCountUtil.release(msg);
                logger.error("{}:没有代理客户端", proxyKey);
                return;
            }
            sessionID = getSessionID();

            //将sessionID，用户ip,port，服务器port，封装到消息中

            Message message = new Message();
            message.setClientChannel(node.getChannel());
            message.setSessionID(sessionID);
            message.setType(CommonConstant.MessageType.TYPE_CONNECT_REALSERVER);
            ProxyRealServer realServer = node.getServerPort2RealServer().get(proxyKey);
            if (Objects.isNull(realServer)) {
                closeChannle(ctx);
                ReferenceCountUtil.release(msg);
                logger.error("{}:没有代理客户端", proxyKey);
                return;
            }
            String address = realServer.getAddress();
            message.setRemoteAddress(address);
            message.setData(address.getBytes());
            message.setProxyType(realServer.getProxyType().byteValue());

            //填写全称,包括端口
            message.setCommand(request.headers().get(HttpHeaderNames.HOST).getBytes());


            //将通道保存 调用UserService(会保存代理类型 type)
            ServerBeanManager.getUserSessionService().add(sessionID, ctx.channel(), realServer);

            //调用ServerService 将消息 放入队列
            ServerBeanManager.getTransferService().toClient(message);
            logger.debug("通知客户端({})与真实服务器{}建立连接 ", node.getClientKey(), address);

            ctx.fireChannelRead(msg);

            logger.debug("用户请求访问通道设置为不可读");
            return;

        }
        if (msg instanceof HttpContent) {
            if (Objects.nonNull(sessionID)) {
                //向上传递消息
                ctx.fireChannelRead(msg);
                return;
            }
            ReferenceCountUtil.release(msg);
            logger.error("需要先建立连接:丢弃消息");
            return;
        }

        ReferenceCountUtil.release(msg);
        logger.error("未识别的消息:丢弃消息");

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        logger.debug("用户连接失效");
        Long sessionID = ServerBeanManager.getUserSessionService().getSessionID(ctx.channel());

        if (Objects.isNull(sessionID)) {
            return;
        }

        //需要通知代理客户端和真实服务器断开连接
        Channel userChannel = ctx.channel();

        //获取 key,可能是端口,也可能是域名
        Object proxyKey;
        try {
            //端口
            proxyKey = Integer.valueOf(userChannel.attr(CommonConstant.UserChannelAttributeKey.PROXY_KEY).get());
        } catch (Exception e) {
            //域名
            proxyKey = userChannel.attr(CommonConstant.UserChannelAttributeKey.PROXY_KEY).get();
        }

        ProxyChannel proxyChannel = ServerBeanManager.getProxyChannelService().getServerProxy(proxyKey);

        //如果不存在key,那么不存在该代理客户端，后续理应不再判断，暂时为了避免考虑不全，先判断多次
        if (Objects.isNull(proxyChannel)) {
            return;
        }
        ClientNode node = ServerBeanManager.getClientService().get(proxyChannel.getClientKey());
        if (node == null || node.getChannel() == null || node.getStatus() != CommonConstant.ClientStatus.ONLINE) {
            return;
        }


        //将sessionID，用户ip,port，服务器port，封装到消息中

        Message message = new Message();
        message.setClientChannel(node.getChannel());
        message.setSessionID(sessionID);
        message.setType(CommonConstant.MessageType.TYPE_DISCONNECT);
        ProxyRealServer realServer = node.getServerPort2RealServer().get(proxyKey);
        if (Objects.isNull(realServer)) {
            return;
        }
        String address = realServer.getAddress();
        message.setRemoteAddress(address);
        message.setData(address.getBytes());


        ServerBeanManager.getUserSessionService().remove(sessionID);
        //调用ServerService 将消息 放入队列
        ServerBeanManager.getTransferService().toClient(message);
        logger.debug("通知客户端({})与真实服务器{}断开连接 ", node.getClientKey(), address);
        closeChannle(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }


    /**
     * 关闭用户连接
     */
    private void closeChannle(ChannelHandlerContext ctx) {

        Channel channel = ctx.channel();
        if (channel != null && channel.isActive()) {
            channel.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("发生异常({})", cause.getMessage());
        closeChannle(ctx);
    }


}
