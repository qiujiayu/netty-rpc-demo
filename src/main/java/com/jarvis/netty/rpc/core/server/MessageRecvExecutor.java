package com.jarvis.netty.rpc.core.server;

import java.nio.channels.spi.SelectorProvider;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.jarvis.netty.rpc.core.NamedThreadFactory;
import com.jarvis.netty.rpc.core.RpcThreadPool;
import com.jarvis.netty.rpc.model.MessageRequest;
import com.jarvis.netty.rpc.model.MessageResponse;
import com.jarvis.netty.rpc.serialize.support.RpcSerializeType;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Rpc服务器执行模块
 * @author jiayu.qiu
 */
public class MessageRecvExecutor {

    private final int port;

    // 默认JKD本地序列化协议
    private final RpcSerializeType serializeType;

    private Map<String, Object> handlerMap=new ConcurrentHashMap<String, Object>();

    private static ListeningExecutorService threadPoolExecutor;

    public MessageRecvExecutor(int port, RpcSerializeType serializeType) {
        this.port=port;
        this.serializeType=serializeType;
    }

    /**
     * 注册服务
     * @param obj
     * @param service
     */
    public <T> void regeditService(T obj, Class<T> service) {
        String name=service.getName();
        System.out.println("regeditService--->" + name);
        handlerMap.put(name, obj);
    }

    /**
     * 开启服务器
     * @throws Exception
     */
    public void startServer() throws Exception {
        // netty的线程池模型设置成主从线程池模式，这样可以应对高并发请求
        // 当然netty还支持单线程、多线程网络IO模型，可以根据业务需求灵活配置
        ThreadFactory threadRpcFactory=new NamedThreadFactory("NettyRPC ThreadFactory");

        // 方法返回到Java虚拟机的可用的处理器数量
        int parallel=Runtime.getRuntime().availableProcessors() * 2;

        EventLoopGroup boss=new NioEventLoopGroup();
        EventLoopGroup worker=new NioEventLoopGroup(parallel, threadRpcFactory, SelectorProvider.provider());

        try {
            ServerBootstrap bootstrap=new ServerBootstrap();
            bootstrap.group(boss, worker).channel(NioServerSocketChannel.class).childHandler(new MessageRecvChannelInitializer(handlerMap, serializeType))
                .option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future=bootstrap.bind(port).sync();
            System.out.printf("Netty RPC Server start success!\nport:%d\nprotocol:%s\n\n", port, serializeType);
            future.channel().closeFuture().sync();
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

    public static void handleMessageRecvTask(final Callable<MessageResponse> task, final ChannelHandlerContext ctx, final MessageRequest request) {
        if(threadPoolExecutor == null) {
            synchronized(MessageRecvExecutor.class) {
                if(threadPoolExecutor == null) {
                    threadPoolExecutor=MoreExecutors.listeningDecorator((ThreadPoolExecutor)RpcThreadPool.getExecutor(16, -1));
                }
            }
        }

        ListenableFuture<MessageResponse> listenableFuture=threadPoolExecutor.submit(task);
        // Netty服务端把计算结果异步返回
        Futures.addCallback(listenableFuture, new FutureCallback<MessageResponse>() {

            public void onSuccess(MessageResponse response) {
                ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {

                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        System.out.println("RPC Server Send message-id respone:" + request.getMessageId());
                    }
                });
            }

            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, threadPoolExecutor);
    }
}