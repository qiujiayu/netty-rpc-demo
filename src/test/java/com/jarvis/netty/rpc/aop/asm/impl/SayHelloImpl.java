package com.jarvis.netty.rpc.aop.asm.impl;

import com.jarvis.netty.rpc.core.client.MessageSendProxy;
import com.jarvis.netty.rpc.core.client.RpcClient;

public class SayHelloImpl implements ISayHello {

    private final RpcClient rpcClient;

    public SayHelloImpl(RpcClient rpcClient) {
        this.rpcClient=rpcClient;
    }

    @Override
    public String MethodA(String a, Integer b, boolean c, char d, byte e, float f, int g, long h, double i, short s, String j) {
        String className="com.jarvis.netty.rpc.aop.asm.impl.ISayHello";
        String methodName="MethodA";
        return (String)MessageSendProxy.request(className, methodName, rpcClient, a, b, c, d, e, f, g, h, i, s, j);
    }

    @Override
    public short MethodB(String a, long b) {
        String className="com.jarvis.netty.rpc.aop.asm.impl.ISayHello";
        String methodName="MethodB";
        return (short)MessageSendProxy.request(className, methodName, rpcClient, a, b);
    }

    @Override
    public int Abs() {
        String className="com.jarvis.netty.rpc.aop.asm.impl.ISayHello";
        String methodName="Abs";
        return (int)MessageSendProxy.request(className, methodName, rpcClient);
    }

    @Override
    public void doVoid() {
        String className="com.jarvis.netty.rpc.aop.asm.impl.ISayHello";
        String methodName="doVoid";
        MessageSendProxy.request(className, methodName, rpcClient);
    }

    @Override
    public float getFloat() {
        String className="com.jarvis.netty.rpc.aop.asm.impl.ISayHello";
        String methodName="getFloat";
        return (float)MessageSendProxy.request(className, methodName, rpcClient);
    }

    @Override
    public boolean getBoolean() {
        String className="com.jarvis.netty.rpc.aop.asm.impl.ISayHello";
        String methodName="getBoolean";
        return (boolean)MessageSendProxy.request(className, methodName, rpcClient);
    }

    @Override
    public double getDouble() {
        String className="com.jarvis.netty.rpc.aop.asm.impl.ISayHello";
        String methodName="getDouble";
        return (double)MessageSendProxy.request(className, methodName, rpcClient);
    }

}
