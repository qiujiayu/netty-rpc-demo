package com.jarvis.netty.rpc.server;

import com.jarvis.netty.rpc.core.server.MessageRecvExecutor;
import com.jarvis.netty.rpc.serialize.support.RpcSerializeType;
import com.jarvis.netty.rpc.services.Calculate;
import com.jarvis.netty.rpc.services.CalculateImpl;

public class Server {

    public static void main(String[] args) throws Exception {
        int port=8080;
        System.out.println("server starting ... ...");
        MessageRecvExecutor executor=new MessageRecvExecutor(port, RpcSerializeType.KRYO);
        executor.regeditService(new CalculateImpl(), Calculate.class);
        
        executor.startServer();
    }

}
