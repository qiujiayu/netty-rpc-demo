package com.jarvis.netty.rpc.core.server;

import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.reflect.MethodUtils;

import com.jarvis.netty.rpc.model.MessageRequest;
import com.jarvis.netty.rpc.model.MessageResponse;

/**
 * Rpc服务器处理消息任务
 * @author jiayu.qiu
 */
public class MessageRecvTask implements Callable<MessageResponse> {

    private final MessageRequest request;

    private final Map<String, Object> handlerMap;

    MessageRecvTask(MessageRequest request, Map<String, Object> handlerMap) {
        this.request=request;
        this.handlerMap=handlerMap;
    }

    public MessageResponse call() {
        MessageResponse response=new MessageResponse();
        response.setMessageId(request.getMessageId());
        try {
            Object result=reflect(request);
            response.setResult(result);

        } catch(Throwable t) {
            response.setError(t.toString());
            t.printStackTrace();
            System.err.printf("RPC Server invoke error!\n");
        }
        return response;
    }

    private Object reflect(MessageRequest request) throws Throwable {
        String className=request.getClassName();
        Object serviceBean=handlerMap.get(className);
        String methodName=request.getMethodName();
        Object[] parameters=request.getParameters();
        return MethodUtils.invokeMethod(serviceBean, methodName, parameters);
    }

}