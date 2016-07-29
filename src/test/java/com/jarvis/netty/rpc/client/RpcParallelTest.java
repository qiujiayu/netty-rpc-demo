package com.jarvis.netty.rpc.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.StopWatch;

import com.jarvis.netty.rpc.core.client.RpcClient;
import com.jarvis.netty.rpc.serialize.support.RpcSerializeType;

/**
 * rpc并发测试代码
 * @author jiayu.qiu
 */
public class RpcParallelTest {

    private static RpcClient rpcClient;

    public static void parallelTask(int parallel, String serverAddress, RpcSerializeType protocol) throws InterruptedException {
        // 开始计时
        StopWatch sw=new StopWatch();
        sw.start();

        CountDownLatch signal=new CountDownLatch(1);
        CountDownLatch finish=new CountDownLatch(parallel);

        for(int index=0; index < parallel; index++) {
            CalcParallelRequestThread client=new CalcParallelRequestThread(rpcClient, signal, finish, index);
            new Thread(client).start();
        }

        // 10000个并发线程瞬间发起请求操作
        signal.countDown();
        finish.await();
        sw.stop();

        String tip=String.format("[%s] RPC调用总共耗时: [%s] 毫秒", protocol, sw.getTime());
        System.out.println(tip);

    }

    public static void main(String[] args) throws Exception {
        // 并行度10000
        int parallel=10000;

        String serverAddress="127.0.0.1:18887";
        RpcSerializeType protocol=RpcSerializeType.KRYO;
        rpcClient=new RpcClient(serverAddress, protocol);
        for(int i=0; i < 10; i++) {
            parallelTask(parallel, serverAddress, protocol);
            TimeUnit.SECONDS.sleep(3);
            System.out.printf("Netty RPC Server 消息协议序列化" + protocol.name() + "第[%d]轮并发验证结束!\n\n", i);
        }

        rpcClient.unLoad();
    }
}