package com.jarvis.netty.rpc.serialize.support.hessian;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.jarvis.netty.rpc.serialize.support.MessageCodecUtil;

import io.netty.buffer.ByteBuf;

/**
 * Hessian编解码工具类
 * @author jiayu.qiu
 */
public class HessianCodecUtil implements MessageCodecUtil {

    private static HessianSerialize hessianSerialization=new HessianSerialize();

    public HessianCodecUtil() {

    }

    @Override
    public void encode(final ByteBuf out, final Object message) throws IOException {
        // System.out.println("hessian encoding ....");
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        hessianSerialization.serialize(byteArrayOutputStream, message);
        byteArrayOutputStream.flush();
        byte[] body=byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        int dataLength=body.length;
        out.writeInt(dataLength);
        out.writeBytes(body);
        // System.out.println("hessian data len:" + dataLength);
    }

    @Override
    public Object decode(byte[] body) throws IOException {
        ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(body);
        Object object=hessianSerialization.deserialize(byteArrayInputStream);
        byteArrayInputStream.close();
        return object;
    }
}