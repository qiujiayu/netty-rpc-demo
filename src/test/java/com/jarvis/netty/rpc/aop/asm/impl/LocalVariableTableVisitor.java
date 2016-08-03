package com.jarvis.netty.rpc.aop.asm.impl;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class LocalVariableTableVisitor extends MethodVisitor implements Opcodes {

    public LocalVariableTableVisitor(MethodVisitor mv) {
        super(ASM5);
    }

    @Override
    public void visitCode() {
        System.out.println("visitCode11");
    }

    @Override
    public void visitLocalVariable(String name, String description, String signature, Label start, Label end, int index) {
        System.out.print("visitLocalVariable-->" + name + ',' + description + "," + signature + "," + start + "," + end + "index=" + index);
    }

    @Override
    public void visitEnd() {

    }

    @Override
    public void visitParameter(String name, int access) {
        super.visitParameter(name, access);
        System.out.println("name" + name + "," + access);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        // TODO Auto-generated method stub
        return super.visitParameterAnnotation(parameter, desc, visible);
    }
}
