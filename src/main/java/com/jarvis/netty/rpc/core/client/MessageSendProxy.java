package com.jarvis.netty.rpc.core.client;

import java.lang.reflect.Method;
import java.util.UUID;

import com.google.common.reflect.AbstractInvocationHandler;
import com.google.common.reflect.Reflection;
import com.jarvis.netty.rpc.model.MessageRequest;

/**
 * Rpc客户端消息处理
 * @author jiayu.qiu
 */
public class MessageSendProxy extends AbstractInvocationHandler {

    private final RpcClient rpcClient;

    public MessageSendProxy(RpcClient rpcClient) {
        this.rpcClient=rpcClient;
    }

    @Override
    public Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        MessageRequest request=new MessageRequest();
        request.setMessageId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setTypeParameters(method.getParameterTypes());
        request.setParameters(args);

        MessageSendHandler handler=rpcClient.getMessageSendHandler();
        MessageCallBack callBack=handler.sendRequest(request);
        return callBack.get();
    }

    /**
     * 获取服务代理
     * @param rpcInterface
     * @return
     */
    public static <T> T getService(Class<T> rpcInterface, RpcClient rpcClient) {
        return (T)Reflection.newProxy(rpcInterface, new MessageSendProxy(rpcClient));
    }
}