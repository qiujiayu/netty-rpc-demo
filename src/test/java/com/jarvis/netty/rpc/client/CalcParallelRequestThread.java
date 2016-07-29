package com.jarvis.netty.rpc.client;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jarvis.netty.rpc.core.client.MessageSendProxy;
import com.jarvis.netty.rpc.core.client.RpcClient;
import com.jarvis.netty.rpc.services.Calculate;

/**
 * 并发线程模拟
 * @author jiayu.qiu
 */
public class CalcParallelRequestThread implements Runnable {

    private final CountDownLatch signal;

    private final CountDownLatch finish;

    private final int taskNumber;

    private final RpcClient rpcClient;

    public CalcParallelRequestThread(RpcClient rpcClient, CountDownLatch signal, CountDownLatch finish, int taskNumber) {
        this.rpcClient=rpcClient;
        this.signal=signal;
        this.finish=finish;
        this.taskNumber=taskNumber;
    }

    public void run() {
        try {
            signal.await();

            Calculate calc=MessageSendProxy.getService(Calculate.class, this.rpcClient);
            int add=calc.add(taskNumber, taskNumber);
            System.out.println("calc add result:[" + add + "]");

            finish.countDown();
        } catch(InterruptedException ex) {
            Logger.getLogger(CalcParallelRequestThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}