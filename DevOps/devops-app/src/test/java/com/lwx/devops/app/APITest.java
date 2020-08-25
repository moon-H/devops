package com.lwx.devops.app;

import com.lwx.devops.app.DevOpsApplication;
import com.lwx.devops.common.concurrent.BoundBuffer;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.constraints.AssertTrue;

/**
 * @description: 接口测试
 * @author: liwx
 * @create: 2020-06-30 09:30
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DevOpsApplication.class)
public class APITest extends TestCase {
    @Test
    public void testBuffer() {
        System.out.println("##################");
        BoundBuffer boundBuffer = new BoundBuffer(10);
        assertTrue(boundBuffer.isEmpty());
        assertFalse(boundBuffer.isFull());
    }

    @Test
    public void testIsFullAfterPuts() throws InterruptedException {
        BoundBuffer<Integer> bb = new BoundBuffer<>(10);
        for (int i = 0; i < 10; i++) {
            bb.put(i);
        }
        assertTrue(bb.isEmpty());
        assertFalse(bb.isFull());
    }

    @Test
    public void testTakeBlocksWhenEmpty() throws InterruptedException {
        final BoundBuffer<Integer> bb = new BoundBuffer<>(10);
        Thread taker = new Thread() {
            public void run() {
                try {
                    System.out.println("############1122334455");
                    int unused = bb.take();
                    System.out.println("############55667788");
                    fail();
                } catch (InterruptedException e) {
                    System.out.println("############InterruptedException");
                }
            }
        };
        try {
            taker.start();
            System.out.println("############11111111111");
            System.out.println("############222222222222");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("############66666666666");
                    try {
                        bb.put(11);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            Thread.sleep(100);
            taker.interrupt();

            taker.join(100);
            System.out.println("############33333333333");
            assertFalse(taker.isAlive());
        } catch (Exception e) {
            System.out.println("############444444444444");
            fail();
        }
    }

}
