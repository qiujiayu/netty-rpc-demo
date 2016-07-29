package com.jarvis.netty.rpc.services;

/**
 * 计算器定义接口实现
 * @author jiayu.qiu
 */
public class CalculateImpl implements Calculate {

    // 两数相加
    public int add(int a, int b) {
        return a + b;
    }
}