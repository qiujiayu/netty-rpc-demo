package com.jarvis.netty.rpc.aop.asm.impl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.time.StopWatch;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.springframework.util.ClassUtils;

import com.jarvis.netty.rpc.core.client.MessageSendProxy;
import com.jarvis.netty.rpc.core.client.RpcClient;
import com.jarvis.netty.rpc.serialize.support.RpcSerializeType;

public class ImplGenerator implements Opcodes {

    private static GeneratorClassLoader classLoader=new GeneratorClassLoader();

    private static Map<Class<?>, Object> classCache=new HashMap<Class<?>, Object>();

    @SuppressWarnings("unchecked")
    public static <T> T generate(Class<T> clazz) throws Exception {
        if(!clazz.isInterface()) {
            throw new IllegalAccessException("必须传入interface");
        }
        T res=(T)classCache.get(clazz);
        if(null == res) {
            ClassReader cr=new ClassReader(clazz.getName());
            ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);
            MyClassVisitor classAdapter=new MyClassVisitor(cw);
            cr.accept(classAdapter, ClassReader.SKIP_DEBUG);
            String className=classAdapter.getSubClassName();
            /*
             * 写入文件
             */
            byte[] code=cw.toByteArray();
            String saveFile=className + ".class";
            FileOutputStream fos=new FileOutputStream(saveFile);
            fos.write(code, 0, code.length);
            fos.flush();
            fos.close();
            Class<?> tmp=classLoader.defineClassFromClassFile(className, code);
            res=(T)tmp.getConstructor(RpcClient.class).newInstance(new RpcClient("localhost:8801", RpcSerializeType.HESSIAN));
            classCache.put(clazz, res);
            try {
                FileInputStream fis=new FileInputStream(saveFile);
                ClassReader classReader=new ClassReader(fis);
                Map<Member, String[]> map=new ConcurrentHashMap<Member, String[]>(32);
                classReader.accept(new ParameterNameDiscoveringVisitor(clazz, map), 0);
                // return map;
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    public static void main(final String args[]) throws Exception {
        ISayHello iSayHello=(ISayHello)generate(ISayHello.class);
        int cnt=100000;
        // 开始计时
        StopWatch sw=new StopWatch();
        sw.start();

        for(int i=0; i < cnt; i++) {
            // String a, Integer b, boolean c,char d,byte e,float f, int g, long h, double i
            iSayHello.MethodA("t", 10, true, 'd', (byte)1, 0.1f, 11, 20000L, 0.0002d, (short)2, "j");
            // iSayHello.MethodB("a",1L);
            // iSayHello.Abs(2);
        }
        String tip=String.format("调用总共耗时: [%s] 毫秒", sw.getTime());
        System.out.println(tip);
        iSayHello=MessageSendProxy.getService(ISayHello.class, new RpcClient("localhost:8801", RpcSerializeType.HESSIAN));
        sw=new StopWatch();
        sw.start();

        for(int i=0; i < cnt; i++) {
            // String a, Integer b, boolean c,char d,byte e,float f, int g, long h, double i
            iSayHello.MethodA("t", 10, true, 'd', (byte)1, 0.1f, 11, 20000L, 0.0002d, (short)2, "j");
            // iSayHello.MethodB("a",1L);
            // iSayHello.Abs(2);
        }
        tip=String.format("调用总共耗时: [%s] 毫秒", sw.getTime());
        System.out.println(tip);
    }

    private static class GeneratorClassLoader extends ClassLoader {

        public Class<?> defineClassFromClassFile(String className, byte[] classFile) throws ClassFormatError {
            return defineClass(className, classFile, 0, classFile.length);
        }
    }

    /**
     * Helper class that inspects all methods (constructor included) and then attempts to find the parameter names for that member.
     */
    private static class ParameterNameDiscoveringVisitor extends ClassVisitor {

        private static final String STATIC_CLASS_INIT="<clinit>";

        private final Class<?> clazz;

        private final Map<Member, String[]> memberMap;

        public ParameterNameDiscoveringVisitor(Class<?> clazz, Map<Member, String[]> memberMap) {
            super(ASM5);
            this.clazz=clazz;
            this.memberMap=memberMap;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new MethodVisitor(Opcodes.ASM5) {

                // assume static method until we get a first parameter name
                public void visitLocalVariable(String name, String description, String signature, Label start, Label end, int index) {
                    System.out.println("--->" + name);
                }
            };
        }

        private static boolean isSyntheticOrBridged(int access) {
            return(((access & Opcodes.ACC_SYNTHETIC) | (access & Opcodes.ACC_BRIDGE)) > 0);
        }

        private static boolean isStatic(int access) {
            return((access & Opcodes.ACC_STATIC) > 0);
        }
    }

    private static class LocalVariableTableVisitor extends MethodVisitor {

        private static final String CONSTRUCTOR="<init>";

        private final Class<?> clazz;

        private final Map<Member, String[]> memberMap;

        private final String name;

        private final Type[] args;

        private final String[] parameterNames;

        private final boolean isStatic;

        private boolean hasLvtInfo=false;

        /*
         * The nth entry contains the slot index of the LVT table entry holding the argument name for the nth parameter.
         */
        private final int[] lvtSlotIndex;

        public LocalVariableTableVisitor(Class<?> clazz, Map<Member, String[]> map, String name, String desc, boolean isStatic) {
            super(ASM5);
            this.clazz=clazz;
            this.memberMap=map;
            this.name=name;
            this.args=Type.getArgumentTypes(desc);
            this.parameterNames=new String[this.args.length];
            this.isStatic=isStatic;
            this.lvtSlotIndex=computeLvtSlotIndices(isStatic, this.args);
        }

        @Override
        public void visitCode() {
            System.out.println("visitCode");
        }

        @Override
        public void visitLocalVariable(String name, String description, String signature, Label start, Label end, int index) {
            System.out.println(name);
            this.hasLvtInfo=true;
            for(int i=0; i < this.lvtSlotIndex.length; i++) {
                if(this.lvtSlotIndex[i] == index) {
                    this.parameterNames[i]=name;
                }
            }
        }

        @Override
        public void visitEnd() {
            if(this.hasLvtInfo || (this.isStatic && this.parameterNames.length == 0)) {
                // visitLocalVariable will never be called for static no args methods
                // which doesn't use any local variables.
                // This means that hasLvtInfo could be false for that kind of methods
                // even if the class has local variable info.
                this.memberMap.put(resolveMember(), this.parameterNames);
            }
        }

        @Override
        public void visitParameter(String name, int access) {
            System.out.println("name" + name + "," + access);
            super.visitParameter(name, access);
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
            System.out.println("parameter" + parameter + "," + desc);
            return super.visitParameterAnnotation(parameter, desc, visible);
        }

        private Member resolveMember() {
            ClassLoader loader=this.clazz.getClassLoader();
            Class<?>[] argTypes=new Class<?>[this.args.length];
            for(int i=0; i < this.args.length; i++) {
                argTypes[i]=ClassUtils.resolveClassName(this.args[i].getClassName(), loader);
            }
            try {
                if(CONSTRUCTOR.equals(this.name)) {
                    return this.clazz.getDeclaredConstructor(argTypes);
                }
                return this.clazz.getDeclaredMethod(this.name, argTypes);
            } catch(NoSuchMethodException ex) {
                throw new IllegalStateException("Method [" + this.name + "] was discovered in the .class file but cannot be resolved in the class object", ex);
            }
        }

        private static int[] computeLvtSlotIndices(boolean isStatic, Type[] paramTypes) {
            int[] lvtIndex=new int[paramTypes.length];
            int nextIndex=(isStatic ? 0 : 1);
            for(int i=0; i < paramTypes.length; i++) {
                lvtIndex[i]=nextIndex;
                if(isWideType(paramTypes[i])) {
                    nextIndex+=2;
                } else {
                    nextIndex++;
                }
            }
            return lvtIndex;
        }

        private static boolean isWideType(Type aType) {
            // float is not a wide type
            return(aType == Type.LONG_TYPE || aType == Type.DOUBLE_TYPE);
        }
    }
}
