package com.proxy.common.codec.http;

import com.proxy.common.protocol.CommonConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;

import java.util.List;

/**
 * http 消息解码器
 */
public class MyHttpRequestDecoder extends HttpRequestDecoder{

//    private  Long sessionId;
    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        super.decode(ctx, buffer, out);
        /*for (Object o:out){
            if (o instanceof HttpRequest){
                 HttpRequest request= (HttpRequest) o;

                 if (request.headers().get(CommonConstant.SESSION_NAME)!=null){
                    System.out.println("==========已存在sessionId============");
                    return;
                 }
                 request.headers().add(CommonConstant.SESSION_NAME,sessionId);
                 sessionId = null;
            }
        }*/
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


        /**
         * 下面注释代码有bug:
         * 当同一个请求被分片后 可能会被标记为两次session
         * 这样会当时数据包不完成，http解析不成一个完整的http报文
         *
         * 非该做法，服务器解析http 报文，然后header 中加入sessionID
         */

        /*if ((msg instanceof  ByteBuf) && sessionId ==null){
            ByteBuf buf= (ByteBuf) msg;
            if(buf.readableBytes()>=Long.BYTES)
                sessionId=buf.readLong();
        }*/

        super.channelRead(ctx, msg);
    }

}
