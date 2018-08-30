package com.proxy.common.codec.http;


import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.util.List;

/**
 * 用于对Http响应的编码
 * @author  ztgreat
 */
public class MyHttpResponseEncoder extends HttpResponseEncoder {

    @Override
    public void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        super.encode(ctx, msg, out);
    }
}
