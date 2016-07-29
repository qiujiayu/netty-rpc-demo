package com.jarvis.netty.rpc.serialize.support.kryo;

import com.jarvis.netty.rpc.serialize.support.MessageCodecUtil;
import com.jarvis.netty.rpc.serialize.support.MessageEncoder;

/**
 * Kryo编码器
 * @author jiayu.qiu
 */
public class KryoEncoder extends MessageEncoder {

    public KryoEncoder(MessageCodecUtil util) {
        super(util);
    }
}