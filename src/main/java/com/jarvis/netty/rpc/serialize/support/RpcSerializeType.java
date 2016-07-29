package com.jarvis.netty.rpc.serialize.support;

/**
 * RPC消息序序列化协议类型
 * @author jiayu.qiu
 */
public enum RpcSerializeType {
        JDK("jdknative"), //
        KRYO("kryo"), //
        HESSIAN("hessian")//
        ;

    private String name;

    private RpcSerializeType(String name) {
        this.name=name;
    }

    public String toString() {
        return this.getClass().getSimpleName() + "." + this.name() + "[name=" + name + "]";
    }

    public String getName() {
        return name;
    }
}