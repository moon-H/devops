package com.lwx.devops.common.jvm;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: LoadInitializationTest
 * @author: liwx
 * @create: 2020-07-05 16:02
 **/
public class InitializationTest {

    public static void main(String[] args) {
//        System.out.println(Son.strSon);
        System.out.println(Son.strFather);
        AtomicInteger atomicInteger=new AtomicInteger(1);
        System.out.println("#########"+atomicInteger);
    }
}
