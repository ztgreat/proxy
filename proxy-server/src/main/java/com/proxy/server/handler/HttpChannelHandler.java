package com.proxy.server.handler;


import com.proxy.common.codec.http.MyHttpRequestEncoder;
import com.proxy.common.entity.server.ClientNode;
import com.proxy.common.entity.server.ProxyChannel;
import com.proxy.common.entity.server.ProxyRealServer;
import com.proxy.common.protocol.CommonConstant;
import com.proxy.common.protocol.Message;
import com.proxy.server.event.TransferEvent;
import com.proxy.server.service.ServerBeanManager;
import com.proxy.server.util.ProxyUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 处理用户http请求
 * 当用户第一个http请求到来时，NoticeChannelHandler会解析头部
 * 取出请求域名,然后根据域名通知客户端和后台建立连接，此时该handler会收到第一个http包
 * 缓存http包，当客户端和真实服务器连接成功后，会触发事件，该handler会收到该事件，然后发送缓存的数据包
 * 后续发送的数据，如果已经建立了连接，则不需要缓存，直接发送。
 *
 * @author ztgreat
 */
public class HttpChannelHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpChannelHandler.class);

    /**
     * 用于http 消息编码处理
     */
    private MyHttpRequestEncoder httpRequestEncoder;


    private volatile boolean transfer = false;

    private List<FullHttpRequest> messages = new LinkedList<>();

    public HttpChannelHandler() {
        super();
        httpRequestEncoder = new MyHttpRequestEncoder();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        //http 请求
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            synchronized (this) {
                if (!transfer) {
                    messages.add(request);
                    return;
                }
            }
            //处理http消息
            httpHandler(ctx, request);
        } else {
            ReferenceCountUtil.release(msg);
            logger.error("丢弃消息");
        }
    }

    /**
     * 处理http 请求
     */
    private void httpHandler(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {


        Channel userChannel = ctx.channel();

        Object proxyKey = ProxyUtil.getKey(request.headers().get(HttpHeaderNames.HOST), userChannel);

        //根据域名获取代理信息
        ProxyChannel proxyChannel = ServerBeanManager.getProxyChannelService().getServerProxy(proxyKey);

        //如果不存在domain,那么不存在该代理客户端，后续理应不再判断，暂时为了避免考虑不全，先判断多次
        if (proxyChannel == null) {
            ctx.channel().close();
            ReferenceCountUtil.release(request);
            logger.error("{}: 没有代理客户端", proxyKey);
            return;
        }

        ClientNode node = ServerBeanManager.getClientService().get(proxyChannel.getClientKey());
        if (node == null || node.getChannel() == null) {
            userChannel.close();
            ReferenceCountUtil.release(request);
            logger.error("{}: 没有代理客户端", proxyKey);
            return;
        }

        //封装消息
        Long sessionID = ServerBeanManager.getUserSessionService().getSessionID(userChannel);

        ProxyRealServer realServer = node.getServerPort2RealServer().get(proxyKey);


        String oldHost = request.headers().get(HttpHeaderNames.HOST);

        /**
         *修改 Referer 属性
         */
        String referer = request.headers().get(HttpHeaderNames.REFERER);
        if (StringUtils.isNotBlank(referer)) {
            if (StringUtils.isNotBlank(realServer.getAddress())) {

                String newReferer;
                if (realServer.getRealHostPort() == CommonConstant.DEFAULT_HTTP_PORT) {
                    newReferer = referer.replace(oldHost, realServer.getRealHost());
                } else {
                    newReferer = referer.replace(oldHost, realServer.getAddress());
                }
                request.headers().set(HttpHeaderNames.REFERER, newReferer);
            }
        }

        //设置host为请求服务器地址
        if (realServer.getRealHostPort() == CommonConstant.DEFAULT_HTTP_PORT) {
            request.headers().set(HttpHeaderNames.HOST, realServer.getRealHost());
        } else {
            request.headers().set(HttpHeaderNames.HOST, realServer.getAddress());
        }


        //设置 sessionID,记录会话
        request.headers().add(CommonConstant.SESSION_NAME, sessionID);


        List<Object> list = new ArrayList<>();
        httpRequestEncoder.encode(ctx, request, list);

        for (Object o : list) {
            ByteBuf buf = (ByteBuf) o;
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            buf.release();
            Message message = new Message();
            message.setClientChannel(node.getChannel());
            message.setData(data);
            message.setSessionID(sessionID);
            message.setType(CommonConstant.MessageType.TYPE_TRANSFER);
            message.setProxyType((byte) CommonConstant.ProxyType.HTTP);
            ServerBeanManager.getTransferService().toClient(message);
        }
        ReferenceCountUtil.release(request);

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof TransferEvent) {
            logger.debug("启动消息转发");
            synchronized (this) {
                this.transfer = true;
            }
            for (FullHttpRequest request : messages) {
                httpHandler(ctx, request);
            }
            messages.clear();
        } else {
            ctx.fireUserEventTriggered(evt);
            logger.debug("向上传递用户事件");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("用户请求发生异常");
        ctx.channel().close();
    }
}
