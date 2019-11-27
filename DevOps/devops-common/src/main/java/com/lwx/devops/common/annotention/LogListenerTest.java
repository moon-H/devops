package com.lwx.devops.common.annotention;

/**
 * @description: LogListener测试
 * @author: liwx
 * @create: 2019-11-27 10:34
 **/
public class LogListenerTest {
    public static void main(String[] args) {
        LogListenerProcessor loglistenerProceser = new LogListenerProcessor();
        try {
            loglistenerProceser.parseMethod(LogListener.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @LogListener(name = "lwx")
    private void testMethod1(String msg) {
        System.out.println("-----------" + msg);
    }


}
