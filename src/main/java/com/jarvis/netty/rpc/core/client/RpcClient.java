package com.jarvis.netty.rpc.core.client;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.jarvis.netty.rpc.core.RpcThreadPool;
import com.jarvis.netty.rpc.serialize.support.RpcSerializeType;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * RPC客户端
 * @author jiayu.qiu
 */
public class RpcClient {

    private final static String DELIMITER=":";

    // 方法返回到Java虚拟机的可用的处理器数量
    private final static int parallel=Runtime.getRuntime().availableProcessors() * 2;

    // netty nio线程池
    private EventLoopGroup eventLoopGroup=new NioEventLoopGroup(parallel);

    private final InetSocketAddress remoteAddr;

    private final RpcSerializeType serializeType;

    private static ListeningExecutorService threadPoolExecutor=MoreExecutors.listeningDecorator((ThreadPoolExecutor)RpcThreadPool.getExecutor(16, -1));

    private volatile MessageSendHandler messageSendHandler=null;

    // 等待Netty服务端链路建立通知信号
    private Lock lock=new ReentrantLock();

    private Condition connectStatus=lock.newCondition();

    private Condition handlerStatus=lock.newCondition();

    public RpcClient(String serverAddress, RpcSerializeType serializeType) {
        this.serializeType=serializeType;
        String[] ipAddr=serverAddress.split(DELIMITER);
        if(ipAddr.length == 2) {
            String host=ipAddr[0];
            int port=Integer.parseInt(ipAddr[1]);
            this.remoteAddr=new InetSocketAddress(host, port);
            load();
        } else {
            this.remoteAddr=null;
        }
    }

    private void load() {

        ListenableFuture<Boolean> listenableFuture=threadPoolExecutor.submit(new MessageSendInitializeTask(this));

        // 监听线程池异步的执行结果成功与否再决定是否唤醒全部的客户端RPC线程
        Futures.addCallback(listenableFuture, new FutureCallback<Boolean>() {

            public void onSuccess(Boolean result) {
                try {
                    lock.lock();

                    if(messageSendHandler == null) {
                        handlerStatus.await();
                    }

                    // Futures异步回调，唤醒所有rpc等待线程
                    if(result == Boolean.TRUE && messageSendHandler != null) {
                        connectStatus.signalAll();
                    }
                } catch(InterruptedException ex) {
                    Logger.getLogger(RpcClient.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    lock.unlock();
                }
            }

            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, threadPoolExecutor);

    }

    public void setMessageSendHandler(MessageSendHandler messageInHandler) {
        try {
            lock.lock();
            this.messageSendHandler=messageInHandler;
            handlerStatus.signal();
        } finally {
            lock.unlock();
        }
    }

    public MessageSendHandler getMessageSendHandler() throws InterruptedException {
        if(null != messageSendHandler) {
            return messageSendHandler;
        }
        try {
            lock.lock();
            // Netty服务端链路没有建立完毕之前，先挂起等待
            if(messageSendHandler == null) {
                connectStatus.await();
            }
            return messageSendHandler;
        } finally {
            lock.unlock();
        }
    }

    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    public void setEventLoopGroup(EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup=eventLoopGroup;
    }

    public InetSocketAddress getRemoteAddr() {
        return remoteAddr;
    }

    public RpcSerializeType getSerializeType() {
        return serializeType;
    }

    public void unLoad() {
        messageSendHandler.close();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }

}