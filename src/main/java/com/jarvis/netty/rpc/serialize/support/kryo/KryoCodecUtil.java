package com.jarvis.netty.rpc.serialize.support.kryo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.esotericsoftware.kryo.pool.KryoPool;
import com.jarvis.netty.rpc.serialize.support.MessageCodecUtil;

import io.netty.buffer.ByteBuf;

/**
 * Kryo编解码工具类
 * @author jiayu.qiu
 */
public class KryoCodecUtil implements MessageCodecUtil {

    private KryoPool pool;

    public KryoCodecUtil(KryoPool pool) {
        this.pool=pool;
    }

    public void encode(final ByteBuf out, final Object message) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        KryoSerialize kryoSerialization=new KryoSerialize(pool);
        kryoSerialization.serialize(byteArrayOutputStream, message);
        byte[] body=byteArrayOutputStream.toByteArray();
        int dataLength=body.length;
        out.writeInt(dataLength);
        out.writeBytes(body);
        byteArrayOutputStream.close();
    }

    public Object decode(byte[] body) throws IOException {
        ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(body);
        KryoSerialize kryoSerialization=new KryoSerialize(pool);
        Object obj=kryoSerialization.deserialize(byteArrayInputStream);
        byteArrayInputStream.close();
        return obj;
    }
}