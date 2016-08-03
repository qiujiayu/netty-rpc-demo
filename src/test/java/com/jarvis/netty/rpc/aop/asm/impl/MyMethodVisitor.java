package com.jarvis.netty.rpc.aop.asm.impl;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class MyMethodVisitor extends AdviceAdapter {

    Label startFinally=new Label();

    public MyMethodVisitor(int access, MethodVisitor mv, String methodName, String description, String className) {
        super(Opcodes.ASM5, mv, access, methodName, description);
    }

    public void visitCode() {
        super.visitCode();
        mv.visitLabel(startFinally);
    }

    public void visitMaxs(int maxStack, int maxLocals) {
        Label endFinally=new Label();
        mv.visitTryCatchBlock(startFinally, endFinally, endFinally, null);
        mv.visitLabel(endFinally);
        onFinally(Opcodes.ATHROW);
        mv.visitInsn(Opcodes.ATHROW);
        super.visitMaxs(maxStack, maxLocals);
    }

    protected void onMethodEnter() {
        // If required, add some code when a method begin
    }

    protected void onMethodExit(int opcode) {
        if(opcode != ATHROW) {
            onFinally(opcode);
        }
    }

    private void onFinally(int opcode) {
        if(opcode == Opcodes.ATHROW) {
            mv.visitInsn(Opcodes.DUP); // Exception thrown by the method
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "abc/xyz/CatchError", "recordException", "(Ljava/lang/Object)V", false);
        } else {
            mv.visitInsn(Opcodes.DUP); // Return object
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "abc/xyz/CatchError", "getReturnObject", "(Ljava/lang/Object)V", false);
        }
    }
}