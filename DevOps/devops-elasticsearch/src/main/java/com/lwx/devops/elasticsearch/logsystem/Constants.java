/*
 * Copyright (c) 2019, shankephone All Rights Reserved.
 * Project Name:log-system
 * Package Name:com.shankephone.log.common
 * Date:2019/11/12 13:54
 *
 */

package com.lwx.devops.elasticsearch.logsystem;

/**
 * 类名:
 * 描述:
 * 时间: 2019/11/12 13:54
 *
 * @author fengql
 * @see
 * @since JDK 1.8
 */
public class Constants {

    //日志类型：0.普通日志, 1.请求日志, 2.接口调用, 9.错误日志, 19.异常日志
    public static final Integer LOG_CATEGORY_GENERAL = 0;
    public static final Integer LOG_CATEGORY_REQUEST = 1;
    public static final Integer LOG_CATEGORY_WEBSERVICE = 2;
    public static final Integer LOG_CATEGORY_ERROR = 9;

    //是否异常
    public static final Integer LOG_EXCEPTION_YES = 1;
    public static final Integer LOG_EXCEPTION_NO = 0;

    public static final String [] SEARCH_KEY = new String[]{
            "level", "cityCode", "serverName", "thread",
            "deviceId", "handlerServerIp", "requestId",
            "requestTime", "addressName"
    };

    //返回响应码
    public static final int RESULT_CODE_SUCCESS = 0;
    public static final int RESULT_CODE_OTHER = 99;

}