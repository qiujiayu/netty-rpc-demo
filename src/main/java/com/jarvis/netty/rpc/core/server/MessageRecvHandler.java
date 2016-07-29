package com.jarvis.netty.rpc.core.server;

import java.util.Map;

import com.jarvis.netty.rpc.model.MessageRequest;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Rpc服务器消息处理
 * @author jiayu.qiu
 */
public class MessageRecvHandler extends ChannelInboundHandlerAdapter {

    private final Map<String, Object> handlerMap;

    public MessageRecvHandler(Map<String, Object> handlerMap) {
        this.handlerMap=handlerMap;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageRequest request=(MessageRequest)msg;
        MessageRecvTask recvTask=new MessageRecvTask(request, handlerMap);
        // 不要阻塞nio线程，复杂的业务逻辑丢给专门的线程池
        MessageRecvExecutor.handleMessageRecvTask(recvTask, ctx, request);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 网络有异常要关闭通道
        ctx.close();
    }
}