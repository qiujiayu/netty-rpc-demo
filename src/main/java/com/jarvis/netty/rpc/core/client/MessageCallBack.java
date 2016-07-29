package com.jarvis.netty.rpc.core.client;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.jarvis.netty.rpc.model.MessageRequest;
import com.jarvis.netty.rpc.model.MessageResponse;

/**
 * Rpc消息回调
 * @author jiayu.qiu
 */
public class MessageCallBack {

    private final MessageRequest request;

    private MessageResponse response;

    private final Lock lock=new ReentrantLock();

    private final Condition finish=lock.newCondition();

    public MessageCallBack(MessageRequest request) {
        this.request=request;
    }

    public Object get() throws InterruptedException {
        try {
            lock.lock();
            // 设定一下超时时间，rpc服务器太久没有相应的话，就默认返回空吧。
            finish.await(10 * 1000, TimeUnit.MILLISECONDS);
            if(this.response != null) {
                return this.response.getResult();
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    public void doCallback(MessageResponse reponse) {
        try {
            lock.lock();
            finish.signal();
            this.response=reponse;
        } finally {
            lock.unlock();
        }
    }

    public MessageRequest getRequest() {
        return this.request;
    }
}