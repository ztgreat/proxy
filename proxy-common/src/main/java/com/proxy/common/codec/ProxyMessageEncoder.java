package com.proxy.common.codec;

import com.proxy.common.protobuf.ProxyMessageProtos;
import com.proxy.common.util.ProxyMessageUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


public class ProxyMessageEncoder extends MessageToByteEncoder<ProxyMessageProtos.ProxyMessage> {


    protected void encode(ChannelHandlerContext ctx, ProxyMessageProtos.ProxyMessage msg, ByteBuf in) throws Exception {

        if (msg==null)
            throw  new Exception("The encode message is null");

        byte[] bytes = ProxyMessageUtil.encode(msg);
        in.writeInt(bytes.length);
        in.writeBytes(bytes);
    }



}
