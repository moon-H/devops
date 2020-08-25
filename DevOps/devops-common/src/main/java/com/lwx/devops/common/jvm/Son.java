package com.lwx.devops.common.jvm;

/**
 * @description:
 * @author: liwx
 * @create: 2020-07-05 16:01
 **/
class Son extends Father {
    public static String strSon = "HelloJVM_Son";

    static {
        System.out.println("Son静态代码块");
    }
}