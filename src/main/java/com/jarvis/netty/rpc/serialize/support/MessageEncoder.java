package com.jarvis.netty.rpc.serialize.support;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC消息编码接口
 * @author jiayu.qiu
 */
public class MessageEncoder extends MessageToByteEncoder<Object> {

    private MessageCodecUtil util=null;

    public MessageEncoder(final MessageCodecUtil util) {
        this.util=util;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final Object msg, final ByteBuf out) throws Exception {
        util.encode(out, msg);
    }
}