package com.lwx.devops.common.jvm;

/**
 * @description:
 * @author: liwx
 * @create: 2020-07-05 15:58
 **/
public class Father extends YeYe {

    public final static String strFather = "HelloJVM_Father";

    static {
        System.out.println("Father静态代码块");
    }
}



