package com.jarvis.netty.rpc.serialize.support.hessian;

import com.jarvis.netty.rpc.serialize.support.MessageCodecUtil;
import com.jarvis.netty.rpc.serialize.support.MessageDecoder;

/**
 * Hessian解码器
 * @author jiayu.qiu
 */
public class HessianDecoder extends MessageDecoder {

    public HessianDecoder(MessageCodecUtil util) {
        super(util);
    }
}