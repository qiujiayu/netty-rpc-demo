package com.jarvis.netty.rpc.core.client;

import java.util.concurrent.ConcurrentHashMap;

import com.jarvis.netty.rpc.model.MessageRequest;
import com.jarvis.netty.rpc.model.MessageResponse;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 客户端消息处理器
 * @author jiayu.qiu
 */
public class MessageSendHandler extends ChannelInboundHandlerAdapter {

    private ConcurrentHashMap<String, MessageCallBack> mapCallBack=new ConcurrentHashMap<String, MessageCallBack>();

    private volatile Channel channel;

    public Channel getChannel() {
        return channel;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel=ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageResponse response=(MessageResponse)msg;
        String messageId=response.getMessageId();
        MessageCallBack callBack=mapCallBack.get(messageId);
        if(callBack != null) {
            callBack.doCallback(response);
            mapCallBack.remove(messageId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public MessageCallBack sendRequest(MessageRequest request) {
        MessageCallBack callBack=new MessageCallBack(request);
        mapCallBack.put(request.getMessageId(), callBack);
        channel.writeAndFlush(request);
        return callBack;
    }
}