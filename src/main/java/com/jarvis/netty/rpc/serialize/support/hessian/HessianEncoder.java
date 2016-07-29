package com.jarvis.netty.rpc.serialize.support.hessian;

import com.jarvis.netty.rpc.serialize.support.MessageCodecUtil;
import com.jarvis.netty.rpc.serialize.support.MessageEncoder;

/**
 * Hessian编码器
 * @author jiayu.qiu
 */
public class HessianEncoder extends MessageEncoder {

    public HessianEncoder(MessageCodecUtil util) {
        super(util);
    }
}