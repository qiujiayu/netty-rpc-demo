package com.jarvis.netty;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class UnsafeUtil {

    private volatile static UnsafeUtil instance;

    private Unsafe unsafe;

    private UnsafeUtil() {
        getUnsafe();
    }

    public static UnsafeUtil getInstance() {
        synchronized(UnsafeUtil.class) {
            if(null == instance) {
                synchronized(UnsafeUtil.class) {
                    if(null == instance) {
                        instance=new UnsafeUtil();

                    }
                }
            }
        }
        return instance;
    }

    /**
     * 获得Unsafe实例
     * @return
     */
    public Unsafe getUnsafe() {
        if(null != unsafe) {
            return unsafe;
        }
        try {
            Field f=Unsafe.class.getDeclaredField("theUnsafe"); // Internal reference
            f.setAccessible(true);
            unsafe=(Unsafe)f.get(null);
            return unsafe;
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 不通过构造函数实例化一个对象 注意：新实例中参数也不会自动赋值：比如设置默认值：int age=12 但返回的是0
     * @param cls
     * @return
     * @throws InstantiationException
     */
    @SuppressWarnings("unchecked")
    public <T> T newInstance(Class<T> cls) throws InstantiationException {
        return (T)unsafe.allocateInstance(cls);
    }

    /**
     * 计算Object 占用内存大小
     * @param o
     * @return
     */
    public long sizeOf(Object o) {
        HashSet<Field> fields=new HashSet<Field>();
        @SuppressWarnings("rawtypes")
        Class c=o.getClass();
        while(c != Object.class) {
            for(Field f: c.getDeclaredFields()) {
                if((f.getModifiers() & Modifier.STATIC) == 0) {
                    fields.add(f);
                }
            }
            c=c.getSuperclass();
        }

        // get offset
        long maxSize=0;
        for(Field f: fields) {
            long offset=unsafe.objectFieldOffset(f);
            if(offset > maxSize) {
                maxSize=offset;
            }
        }

        return ((maxSize / 8) + 1) * 8; // padding
    }

    /**
     * JDK1.7
     * @param object
     * @return
     */
    public long sizeOf2(Object object) {
        return unsafe.getAddress(normalize(unsafe.getInt(object, 4L)) + 12L);
    }

    /**
     * casting signed int to unsigned long, for correct address usage
     * @param value
     * @return
     */
    public long normalize(int value) {
        if(value >= 0)
            return value;
        return (~0L >>> 32) & value;
    }

    /**
     * 浅度复制
     * @param obj
     * @return
     */
    public Object shallowCopy(Object obj) {
        long size=sizeOf(obj);
        long start=toAddress(obj);
        long address=unsafe.allocateMemory(size);
        unsafe.copyMemory(start, address, size);
        return fromAddress(address);
    }

    /**
     * convert object to its address in memory
     * @param obj
     * @return
     */
    public long toAddress(Object obj) {
        Object[] array=new Object[]{obj};
        long baseOffset=unsafe.arrayBaseOffset(Object[].class);
        return normalize(unsafe.getInt(array, baseOffset));
    }

    public Object fromAddress(long address) {
        Object[] array=new Object[]{null};
        long baseOffset=unsafe.arrayBaseOffset(Object[].class);
        unsafe.putLong(array, baseOffset, address);
        return array[0];
    }
}
