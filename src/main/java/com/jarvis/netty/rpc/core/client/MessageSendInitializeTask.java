package com.jarvis.netty.rpc.core.client;

import java.util.concurrent.Callable;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Rpc客户端线程任务处理
 * @author jiayu.qiu
 */
public class MessageSendInitializeTask implements Callable<Boolean> {

    private final RpcClient rpcClient;

    public MessageSendInitializeTask(RpcClient rpcClient) {
        this.rpcClient=rpcClient;
    }

    @Override
    public Boolean call() {
        Bootstrap b=new Bootstrap();

        b.group(rpcClient.getEventLoopGroup()).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true)
            .handler(new MessageSendChannelInitializer().buildRpcSerializeProtocol(rpcClient.getSerializeType()));

        ChannelFuture channelFuture=b.connect(rpcClient.getRemoteAddr());
        channelFuture.addListener(new ChannelFutureListener() {

            public void operationComplete(final ChannelFuture channelFuture) throws Exception {
                if(channelFuture.isSuccess()) {
                    MessageSendHandler handler=channelFuture.channel().pipeline().get(MessageSendHandler.class);
                    rpcClient.setMessageSendHandler(handler);
                }
            }
        });
        return Boolean.TRUE;
    }
}