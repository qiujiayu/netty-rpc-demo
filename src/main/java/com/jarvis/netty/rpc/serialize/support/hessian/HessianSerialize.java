package com.jarvis.netty.rpc.serialize.support.hessian;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import com.jarvis.netty.rpc.serialize.support.RpcSerialize;

/**
 * Hessian序列化/反序列化实现
 * @author jiayu.qiu
 */
public class HessianSerialize implements RpcSerialize {

    private static final SerializerFactory serializerFactory=new SerializerFactory();

    static {
        // serializerFactory.addFactory(new HessionBigDecimalSerializerFactory());
        // serializerFactory.addFactory(new HessionSoftReferenceSerializerFactory());
    }

    public void serialize(OutputStream output, Object object) {
        Hessian2Output ho=new Hessian2Output(output);
        try {
            ho.setSerializerFactory(serializerFactory);
            ho.writeObject(object);
            ho.flush();
            // ho.close();
            // output.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public Object deserialize(InputStream input) {
        Object result=null;
        try {
            Hessian2Input hi=new Hessian2Input(input);
            hi.setSerializerFactory(serializerFactory);
            result=hi.readObject();
            // System.out.println(result);
            hi.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}