package com.lwx.devops.common.concurrent;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: BoundedBuffer 生产者-消费者程序
 * @author: liwx
 * @create: 2020-08-02 18:06
 **/
public class PutTakeTest {
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    private final AtomicInteger putSum = new AtomicInteger(0);
    private final AtomicInteger takeSum = new AtomicInteger(0);
    private final CyclicBarrier barrier;
    private BoundBuffer<Integer> bb;
    private final int nTrials, nPairs;

    PutTakeTest(int capacity, int nPairs, int nTrials) {
        this.bb = new BoundBuffer<>(capacity);
        this.nTrials = nTrials;
        this.nPairs = nPairs;
        this.barrier = new CyclicBarrier(nPairs * 2 + 1);
    }

}
