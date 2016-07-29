package com.jarvis.netty.rpc.serialize.support;

import java.io.IOException;

import io.netty.buffer.ByteBuf;

/**
 * RPC消息编解码接口
 * @author jiayu.qiu
 */
public interface MessageCodecUtil {

    // RPC消息报文头长度4个字节
    final public static int MESSAGE_LENGTH=4;

    public void encode(final ByteBuf out, final Object message) throws IOException;

    public Object decode(byte[] body) throws IOException;
}