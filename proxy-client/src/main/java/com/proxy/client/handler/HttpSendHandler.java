package com.proxy.client.handler;


import com.proxy.client.service.ClientBeanManager;
import com.proxy.common.codec.http.MyHttpResponseEncoder;
import com.proxy.common.protobuf.ProxyMessage;
import com.proxy.common.protocol.CommonConstant;
import com.proxy.common.util.ProxyMessageUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理真实服务器返回的响应
 * 处理:重定向问题
 *
 * @author ztgreat
 */
public class HttpSendHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpSendHandler.class);

    /**
     * 用于http 消息编码处理
     *
     * @// TODO: 2018/2/10 需要review
     */
    private MyHttpResponseEncoder httpResponseEncoder;

    public HttpSendHandler() {
        super();
        httpResponseEncoder = new MyHttpResponseEncoder();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        //http 请求
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            //处理http消息
            httpHandler(ctx, response);
        } else {
            ReferenceCountUtil.release(msg);
            logger.error("不支持的http消息:丢弃消息");
        }
    }

    /**
     * 处理http 响应
     */
    private void httpHandler(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {

        //客户端与真实服务器的channel
        Channel realServerChannel = ctx.channel();
        //客户端与代理服务的channel
        Channel channel = ClientBeanManager.getProxyService().getChannel();

        // 这里在处理重定向的情况，简单粗暴的处理
        int code = response.status().code();
        if (code == 302 || code == 303) {
            String proxyServer = ClientBeanManager.getProxyService().getProxyServer(realServerChannel);
            String localtion = String.valueOf(response.headers().get(HttpHeaderNames.LOCATION));
            int index = localtion.indexOf("/", 8);
            localtion = "http://" + proxyServer + localtion.substring(index);
            response.headers().set(HttpHeaderNames.LOCATION, localtion);
        }
        List<Object> list = new ArrayList<>();
        httpResponseEncoder.encode(ctx, response, list);
        for (Object o : list) {
            logger.debug("转发HTTP消息到代理服务器");
            ByteBuf buf = (ByteBuf) o;
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            buf.release();
            Long sessionID = ClientBeanManager.getProxyService().getRealServerChannelSessionID(realServerChannel);
            ProxyMessage proxyMessage = ProxyMessageUtil.buildMsg(sessionID, CommonConstant.MessageType.TYPE_TRANSFER, null, null, null, data);
            channel.writeAndFlush(proxyMessage);
        }
    }


}
