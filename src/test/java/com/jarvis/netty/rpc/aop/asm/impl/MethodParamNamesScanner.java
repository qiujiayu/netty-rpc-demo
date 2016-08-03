package com.jarvis.netty.rpc.aop.asm.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodParamNamesScanner {

    /**
     * 获取方法参数名列表
     * @param clazz
     * @param m
     * @return
     * @throws IOException
     */
    public static List<String> getMethodParamNames(Class<?> clazz, Method m) throws Exception {
        Parameter[] ps=m.getParameters();
        System.out.print(m.getName() + "(");
        for(Parameter p: ps) {
            System.out.print(p.getName() + ",");
        }
        System.out.println(")");
        if(clazz.isInterface()) {
            throw new Exception("不能使用获取interface中的参数名称。");
        }
        InputStream in=clazz.getResourceAsStream("/" + clazz.getName().replace('.', '/') + ".class");
        return getMethodParamNames(in, m);

    }

    public static List<String> getMethodParamNames(InputStream in, Method m) throws IOException {
        return getParamNames(in, new EnclosingMetadata(m.getName(), Type.getMethodDescriptor(m), m.getParameterTypes().length));

    }

    /**
     * 获取构造器参数名列表
     * @param clazz
     * @param constructor
     * @return
     */
    public static List<String> getConstructorParamNames(Class<?> clazz, Constructor<?> constructor) {
        try {
            InputStream in=clazz.getResourceAsStream("/" + clazz.getName().replace('.', '/') + ".class");
            return getConstructorParamNames(in, constructor);
        } catch(IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch(Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new ArrayList<String>();
    }

    public static List<String> getConstructorParamNames(InputStream ins, Constructor<?> constructor) throws Exception {
        return getParamNames(ins, new EnclosingMetadata(constructor.getName(), Type.getConstructorDescriptor(constructor), constructor.getParameterTypes().length));
    }

    /**
     * 获取参数名列表辅助方法
     * @param in
     * @param m
     * @return
     * @throws IOException
     */
    private static List<String> getParamNames(InputStream in, EnclosingMetadata m) throws IOException {
        ClassReader cr=new ClassReader(in);
        ClassNode cn=new ClassNode();
        cr.accept(cn, ClassReader.EXPAND_FRAMES);// 建议EXPAND_FRAMES
        // ASM树接口形式访问
        List<MethodNode> methods=cn.methods;
        List<String> list=new ArrayList<String>();
        for(int i=0; i < methods.size(); ++i) {
            List<LocalVariable> varNames=new ArrayList<LocalVariable>();
            MethodNode method=methods.get(i);
            method.accept(cn);
            // 验证方法签名
            if(method.desc.equals(m.desc) && method.name.equals(m.name)) {
                System.out.println(method.name + "--->desc->" + method.desc + ":" + m.desc);
                List<LocalVariableNode> local_variables=method.localVariables;
                for(int l=0; l < local_variables.size(); l++) {
                    String varName=local_variables.get(l).name;
                    // index-记录了正确的方法本地变量索引。(方法本地变量顺序可能会被打乱。而index记录了原始的顺序)
                    int index=local_variables.get(l).index;
                    if(!"this".equals(varName)) // 非静态方法,第一个参数是this
                        varNames.add(new LocalVariable(index, varName));
                }
                LocalVariable[] tmpArr=varNames.toArray(new LocalVariable[varNames.size()]);
                // 根据index来重排序，以确保正确的顺序
                Arrays.sort(tmpArr);
                for(int j=0; j < m.size; j++) {
                    list.add(tmpArr[j].name);
                }
                break;

            }

        }
        return list;
    }

    /**
     * 方法本地变量索引和参数名封装
     * @author xby Administrator
     */
    static class LocalVariable implements Comparable<LocalVariable> {

        public int index;

        public String name;

        public LocalVariable(int index, String name) {
            this.index=index;
            this.name=name;
        }

        public int compareTo(LocalVariable o) {
            return this.index - o.index;
        }
    }

    /**
     * 封装方法描述和参数个数
     * @author xby Administrator
     */
    static class EnclosingMetadata {

        // method name
        public String name;

        // method description
        public String desc;

        // params size
        public int size;

        public EnclosingMetadata(String name, String desc, int size) {
            this.name=name;
            this.desc=desc;
            this.size=size;
        }
    }

    public static void main(String[] args) throws Exception {
        for(Method m: SayHelloImpl.class.getDeclaredMethods()) {
            List<String> list=getMethodParamNames(SayHelloImpl.class, m);
            System.out.println(m.getName() + " param names:");
            for(String str: list) {
                System.out.println(str);
            }
            System.out.println("------------------------");
        }
    }
}
