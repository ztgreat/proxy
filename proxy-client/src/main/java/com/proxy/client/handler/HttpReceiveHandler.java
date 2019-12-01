package com.proxy.client.handler;


import com.proxy.client.service.ClientBeanManager;
import com.proxy.common.codec.http.MyHttpRequestEncoder;
import com.proxy.common.entity.client.RealServer;
import com.proxy.common.protocol.CommonConstant;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 处理用户请求的handler
 *
 * @author ztgreat
 */
public class HttpReceiveHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpReceiveHandler.class);

    /**
     * 用于http 消息编码处理
     *
     * @// TODO: 2018/2/10 需要review
     */
    private MyHttpRequestEncoder httpRequestEncoder;

    public HttpReceiveHandler() {
        super();
        httpRequestEncoder = new MyHttpRequestEncoder();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        //http 请求
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            //处理http消息
            httpHandler(ctx, request);

        } else {
            ReferenceCountUtil.release(msg);
            logger.error("不支持的http消息:丢弃消息");
        }
    }


    /**
     * 处理http 请求
     */
    private void httpHandler(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {


        String id = request.headers().get(CommonConstant.SESSION_NAME);
        Long sessionID = Long.valueOf(id);
        RealServer realServer = ClientBeanManager.getProxyService().getRealServerChannel(sessionID);
        Channel realServerChannel;

        if (realServer == null || (realServerChannel = realServer.getChannel()) == null) {
            logger.debug("无法获取真实服务器连接,丢弃消息");
            ReferenceCountUtil.release(request);
            return;
        }


        InetSocketAddress sa = (InetSocketAddress) realServerChannel.remoteAddress();
        String host = sa.getHostString();

        int port = sa.getPort();

        request.headers().set(HttpHeaderNames.HOST, host + ":" + port);

        /**
         * 使用http 1.1,并设置keep-alive
         * 否则: 当收到真实服务器消息后，真正组装消息的时候，真实服务器因为短连接原因关闭
         * 通道,会使已经缓存的消息，但是还没有发送的消息 发送不出去。
         */
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

        List<Object> list = new ArrayList<>();
        httpRequestEncoder.encode(ctx, request, list);
        for (Object o : list) {
            realServerChannel.writeAndFlush(o);
            logger.debug("转发http请求至真实服务器:{}:{}", request.method().name(), request.uri());
        }


    }

}
