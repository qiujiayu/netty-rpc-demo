package com.jarvis.netty.rpc.core.client;

import com.jarvis.netty.rpc.serialize.support.MessageCodecUtil;
import com.jarvis.netty.rpc.serialize.support.RpcSerializeType;
import com.jarvis.netty.rpc.serialize.support.hessian.HessianCodecUtil;
import com.jarvis.netty.rpc.serialize.support.hessian.HessianDecoder;
import com.jarvis.netty.rpc.serialize.support.hessian.HessianEncoder;
import com.jarvis.netty.rpc.serialize.support.kryo.KryoCodecUtil;
import com.jarvis.netty.rpc.serialize.support.kryo.KryoDecoder;
import com.jarvis.netty.rpc.serialize.support.kryo.KryoEncoder;
import com.jarvis.netty.rpc.serialize.support.kryo.KryoPoolFactory;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * Rpc客户端管道初始化
 * @author jiayu.qiu
 */
public class MessageSendChannelInitializer extends ChannelInitializer<SocketChannel> {

    private RpcSerializeType serializeType;

    public MessageSendChannelInitializer buildRpcSerializeProtocol(RpcSerializeType serializeType) {
        this.serializeType=serializeType;
        return this;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline=socketChannel.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, MessageCodecUtil.MESSAGE_LENGTH, 0, MessageCodecUtil.MESSAGE_LENGTH));
        pipeline.addLast(new LengthFieldPrepender(MessageCodecUtil.MESSAGE_LENGTH));
        switch(serializeType) {
            case JDK: {
                pipeline.addLast(new ObjectEncoder());
                pipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
                break;
            }
            case KRYO: {
                KryoCodecUtil util=new KryoCodecUtil(KryoPoolFactory.getKryoPoolInstance());
                pipeline.addLast(new KryoEncoder(util));
                pipeline.addLast(new KryoDecoder(util));
                break;
            }
            case HESSIAN: {
                HessianCodecUtil util=new HessianCodecUtil();
                pipeline.addLast(new HessianEncoder(util));
                pipeline.addLast(new HessianDecoder(util));
                break;
            }
        }
        pipeline.addLast(new MessageSendHandler());
    }

}