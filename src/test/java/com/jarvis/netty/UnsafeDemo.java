package com.jarvis.netty;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import sun.misc.Unsafe;

/**
 * http://mishadoff.com/blog/java-magic-part-4-sun-dot-misc-dot-unsafe/ http://blog.csdn.net/fenglibing/article/details/17138079
 * @author jiayu.qiu
 */

@SuppressWarnings("restriction")

public class UnsafeDemo {

    private static UnsafeUtil unsafe=UnsafeUtil.getInstance();

    @Test
    public void testNewInstance() throws InstantiationException {
        // This creates an instance of player class without any initialization
        Player p=(Player)unsafe.newInstance(Player.class);
        System.out.println(p.getAge()); // Print 0,
        assertEquals(p.getAge(), 0);
        p.setAge(45); // Let's now set age 45 to un-initialized object
        assertEquals(p.getAge(), 45); // Print 45
    }

    @Test
    public void testMemory() throws NoSuchFieldException, SecurityException {
        Guard guard=new Guard();
        assertEquals(guard.giveAccess(), false); // false, no access
        // Guard guard2 = new Guard();
        // bypass
        Field f=guard.getClass().getDeclaredField("ACCESS_ALLOWED");
        unsafe.getUnsafe().putInt(guard, unsafe.getUnsafe().objectFieldOffset(f), 42); // memory corruption

        assertEquals(guard.giveAccess(), true); // true, access granted

        System.out.println("guard size of:" + unsafe.sizeOf(guard));
        unsafe.getUnsafe().putInt(guard, unsafe.sizeOf(guard) + unsafe.getUnsafe().objectFieldOffset(f), 42); // memory corruption

        // assertEquals(guard2.giveAccess(), true); // true, access granted
    }

    @Test
    public void hidePassword() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        // 隐藏密码
        String password=new String("l00k@myHor$e");
        String fake=new String(password.replaceAll(".", "?"));
        System.out.println(password); // l00k@myHor$e
        System.out.println(fake); // ????????????

        unsafe.getUnsafe().copyMemory(fake, 0L, null, unsafe.toAddress(password), unsafe.sizeOf(password));

        assertEquals(password, "????????????"); // ????????????
        assertEquals(fake, "????????????"); // ????????????

        // 比较安全的做法
        String password2=new String("l00k@myHor$e");
        Field stringValue=String.class.getDeclaredField("value");
        stringValue.setAccessible(true);
        char[] mem=(char[])stringValue.get(password2);
        for(int i=0; i < mem.length; i++) {
            mem[i]='?';
        }
        assertEquals(password2, "????????????"); // ????????????
    }

    // @Test
    public void testMultipleInheritance() {
        long intClassAddress=unsafe.normalize(unsafe.getUnsafe().getInt(new Integer(0), 4L));
        long strClassAddress=unsafe.normalize(unsafe.getUnsafe().getInt("", 4L));
        unsafe.getUnsafe().putAddress(intClassAddress + 36, strClassAddress);
    }

    private byte[] getClassContent(Class<?> cls) throws Exception {
        String name=cls.getName();
        String path=name.replaceAll("\\.", "/") + ".class";
        System.out.println(path);
        InputStream is=this.getClass().getClassLoader().getResourceAsStream(path);
        ByteArrayOutputStream os=new ByteArrayOutputStream();
        byte[] buf=new byte[512];
        int len;
        while((len=is.read(buf)) != -1) {
            os.write(buf, 0, len);
        }
        return os.toByteArray();
    }

    @Test
    public void testDynamicClasses() throws Exception {
        byte[] classContents=getClassContent(Player.class);
        Class<?> c=unsafe.getUnsafe().defineClass(null, classContents, 0, classContents.length, null, null);
        System.out.println(c.getName());
        assertEquals(c.getName(), Player.class.getName());
        // Object obj=c.newInstance();
        // assertEquals(c.getMethod("getAge").invoke(obj, null), 12); // 1
    }

    @Test
    public void testCAS() throws Exception {
        int NUM_OF_THREADS=1000;
        ExecutorService service=Executors.newFixedThreadPool(NUM_OF_THREADS);
        final CASCounter counter=new CASCounter();// creating instance of specific counter
        long before=System.currentTimeMillis();
        for(int i=0; i < NUM_OF_THREADS; i++) {
            service.submit(new Runnable() {

                public void run() {
                    for(int i=0; i < 1000; i++) {
                        counter.increment();
                    }
                }
            });
        }
        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
        long after=System.currentTimeMillis();
        System.out.println("Counter result: " + counter.getCounter());
        System.out.println("Time passed in ms:" + (after - before));
    }
}

@SuppressWarnings("restriction")
class CASCounter {

    private volatile long counter=0;

    private Unsafe unsafe=UnsafeUtil.getInstance().getUnsafe();

    private long offset;

    public CASCounter() throws Exception {
        offset=unsafe.objectFieldOffset(CASCounter.class.getDeclaredField("counter"));
    }

    public void increment() {
        long before=counter;
        while(!unsafe.compareAndSwapLong(this, offset, before, before + 1)) {
            before=counter;
        }
    }

    public long getCounter() {
        return counter;
    }
}

class Player {

    private int age=12;

    public Player() {
        this.age=50;
    }

    public int getAge() {
        return this.age;
    }

    public void setAge(int age) {
        this.age=age;
    }
}

class Guard {

    private int ACCESS_ALLOWED=1;

    public boolean giveAccess() {
        return 42 == ACCESS_ALLOWED;
    }
}