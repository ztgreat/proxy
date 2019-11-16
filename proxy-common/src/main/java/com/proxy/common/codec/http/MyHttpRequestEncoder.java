package com.proxy.common.codec.http;


import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequestEncoder;

import java.util.List;

/**
 * 用于对Http请求的编码
 *
 * @author ztgreat
 */
public class MyHttpRequestEncoder extends HttpRequestEncoder {

    @Override
    public void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        super.encode(ctx, msg, out);
    }
}
