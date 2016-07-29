package com.jarvis.netty.rpc.serialize.support.kryo;

import com.jarvis.netty.rpc.serialize.support.MessageCodecUtil;
import com.jarvis.netty.rpc.serialize.support.MessageDecoder;

/**
 * Kryo解码器
 * @author jiayu.qiu
 */
public class KryoDecoder extends MessageDecoder {

    public KryoDecoder(MessageCodecUtil util) {
        super(util);
    }
}