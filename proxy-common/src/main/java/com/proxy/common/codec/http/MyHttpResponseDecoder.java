package com.proxy.common.codec.http;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseDecoder;

import java.util.List;

/**
 * 用于对Http请求的解码,暂时不需要
 *
 * @author ztgreat
 */
public class MyHttpResponseDecoder extends HttpResponseDecoder {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        super.decode(ctx, buffer, out);
    }
}
