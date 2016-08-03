package com.jarvis.netty.rpc.aop.asm.impl;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.springframework.asm.Type;

import com.jarvis.netty.rpc.core.client.RpcClient;

public class MyClassVisitor extends ClassVisitor implements Opcodes {

    private static final String rpcClientType=Type.getDescriptor(RpcClient.class);

    private static final String rpcClient="rpcClient";

    private String interfaceName;

    private String subClassPath;// 实现类路径

    private String subClassName;

    public MyClassVisitor(ClassVisitor cv) {
        // Responsechain 的下一个 ClassVisitor，这里我们将传入 ClassWriter，
        // 负责改写后代码的输出
        super(ASM5, cv);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        System.out.println(name);
        this.interfaceName=name.replaceAll("[/]", ".");
        this.subClassPath=name + "$Impl";
        this.subClassName=subClassPath.replaceAll("[/]", ".");
        System.out.println(subClassName);
        super.visit(version, ACC_PUBLIC + ACC_SUPER, subClassPath, signature, "java/lang/Object", new String[]{name});
    }

    public String getSubClassName() {
        return subClassName;
    }

    // 重写 visitMethod，访问到 "operation" 方法时，
    // 给出自定义 MethodVisitor，实际改写方法内容
    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        System.out.println(name + "--->" + desc);
        Type[] paramTypes=Type.getArgumentTypes(desc);
        int paramLength=0;
        if(null != paramTypes) {
            paramLength=paramTypes.length;
            for(Type t: paramTypes) {
                System.out.println("    type-->" + t);
            }
        }
        int ind=desc.lastIndexOf(')');
        Type returnType=Type.getType(desc.substring(ind + 1));
        System.out.println("returnType=" + returnType);
        MethodVisitor mv=super.visitMethod(ACC_PUBLIC, name, desc, null, null);
        int loadI=1;
        for(Type tp: paramTypes) {
            if(tp.equals(Type.LONG_TYPE) || tp.equals(Type.DOUBLE_TYPE)) {
                loadI++;
            }
            loadI++;
        }

        // Load class name and method name
        mv.visitCode();
        Label l0=new Label();
        mv.visitLabel(l0);
        mv.visitLdcInsn(this.subClassName);
        mv.visitVarInsn(ASTORE, loadI + 1);
        Label l1=new Label();
        mv.visitLabel(l1);
        mv.visitLdcInsn(name);
        mv.visitVarInsn(ASTORE, loadI + 2);
        Label l2=new Label();
        mv.visitLabel(l2);
        mv.visitVarInsn(ALOAD, loadI + 1);
        mv.visitVarInsn(ALOAD, loadI + 2);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, this.subClassPath, "rpcClient", "Lcom/jarvis/netty/rpc/core/client/RpcClient;");
        // Create array with length equal to number of parameters
        if(paramLength > 0) {
            mv.visitIntInsn(BIPUSH, paramLength);
        } else {
            mv.visitInsn(ICONST_2);
        }
        mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");

        // Fill the created array with method parameters
        int i=0;
        int aloadI=1;
        for(Type tp: paramTypes) {
            mv.visitInsn(DUP);
            if(i == 0) {
                mv.visitInsn(ICONST_0);
            } else if(i == 1) {
                mv.visitInsn(ICONST_1);
            } else if(i == 2) {
                mv.visitInsn(ICONST_2);
            } else if(i == 3) {
                mv.visitInsn(ICONST_3);
            } else if(i == 4) {
                mv.visitInsn(ICONST_4);
            } else if(i == 5) {
                mv.visitInsn(ICONST_5);
            } else {
                mv.visitIntInsn(BIPUSH, i);
            }

            if(tp.equals(Type.BOOLEAN_TYPE)) {
                mv.visitVarInsn(ILOAD, aloadI);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
            } else if(tp.equals(Type.BYTE_TYPE)) {
                mv.visitVarInsn(ILOAD, aloadI);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
            } else if(tp.equals(Type.CHAR_TYPE)) {
                mv.visitVarInsn(ILOAD, aloadI);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
            } else if(tp.equals(Type.SHORT_TYPE)) {
                mv.visitVarInsn(ILOAD, aloadI);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
            } else if(tp.equals(Type.INT_TYPE)) {
                mv.visitVarInsn(ILOAD, aloadI);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            } else if(tp.equals(Type.LONG_TYPE)) {
                mv.visitVarInsn(LLOAD, aloadI);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                aloadI++;
            } else if(tp.equals(Type.FLOAT_TYPE)) {
                mv.visitVarInsn(FLOAD, aloadI);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
            } else if(tp.equals(Type.DOUBLE_TYPE)) {
                mv.visitVarInsn(DLOAD, aloadI);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
                aloadI++;
            } else {
                mv.visitVarInsn(ALOAD, aloadI);
            }
            mv.visitInsn(AASTORE);
            i++;
            aloadI++;
        }

        // mv.visitMethodInsn(Opcodes.INVOKESTATIC, "callbackpackage/CallBack", "callbackfunc",
        // "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V");
        mv.visitMethodInsn(INVOKESTATIC, "com/jarvis/netty/rpc/core/client/MessageSendProxy", "request",
            "(Ljava/lang/String;Ljava/lang/String;Lcom/jarvis/netty/rpc/core/client/RpcClient;[Ljava/lang/Object;)Ljava/lang/Object;", false);
        // mv.visitInsn(POP);
        if(returnType.equals(Type.BOOLEAN_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
            mv.visitInsn(IRETURN);
        } else if(returnType.equals(Type.BYTE_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false);
            mv.visitInsn(IRETURN);
        } else if(returnType.equals(Type.CHAR_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
            mv.visitInsn(IRETURN);
        } else if(returnType.equals(Type.SHORT_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false);
            mv.visitInsn(IRETURN);
        } else if(returnType.equals(Type.INT_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
            mv.visitInsn(IRETURN);
        } else if(returnType.equals(Type.LONG_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
            mv.visitInsn(LRETURN);
        } else if(returnType.equals(Type.FLOAT_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
            mv.visitInsn(FRETURN);
        } else if(returnType.equals(Type.DOUBLE_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
            mv.visitInsn(DRETURN);
        } else if(returnType.equals(Type.VOID_TYPE)) {
            mv.visitInsn(RETURN);
        } else {
            mv.visitTypeInsn(CHECKCAST, returnType.getInternalName());
            mv.visitInsn(ARETURN);
        }
        mv.visitMaxs(4, 3);
        mv.visitEnd();
        return mv;
    }

    @Override
    public void visitEnd() {
        // 添加
        FieldVisitor fv=super.visitField(ACC_PRIVATE + ACC_FINAL, rpcClient, rpcClientType, null, null);
        fv.visitEnd();
        // 构造方法
        MethodVisitor mv=super.visitMethod(ACC_PUBLIC, "<init>", "(" + rpcClientType + ")V", null, null);
        mv.visitCode();
        Label l0=new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        Label l1=new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, subClassPath, rpcClient, rpcClientType);
        Label l2=new Label();
        mv.visitLabel(l2);
        mv.visitInsn(RETURN);
        Label l3=new Label();
        mv.visitLabel(l3);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

}
