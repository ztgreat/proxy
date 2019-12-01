package com.proxy.common.codec;


import com.proxy.common.protobuf.ProxyMessage;
import com.proxy.common.util.ProxyMessageUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;


public class ProxyMessageDecoder extends LengthFieldBasedFrameDecoder {


    public ProxyMessageDecoder(int maxFrameLength, int lengthFieldOffset,
                               int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);

    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {

        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        int length = frame.readInt();
        byte[] msg = new byte[length];
        frame.readBytes(msg);
        ProxyMessage proxyMessage = ProxyMessageUtil.decode(msg);
        frame.release();
        return proxyMessage;
    }

}
