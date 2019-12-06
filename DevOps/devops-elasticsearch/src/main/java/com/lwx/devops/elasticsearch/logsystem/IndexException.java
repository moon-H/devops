/*
 * Copyright (c) 2019, shankephone All Rights Reserved.
 * Project Name:log-system
 * Package Name:com.shankephone.log.common.error
 * Date:2019/11/15 11:18
 *
 */

package com.lwx.devops.elasticsearch.logsystem;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

/**
 * 类名:
 * 描述:
 * 时间: 2019/11/15 11:18
 *
 * @author fengql
 * @see
 * @since JDK 1.8
 */
@Slf4j
public class IndexException extends BaseException{

    private static final String name = "索引异常";

    public IndexException(){
        super();
    }

    public IndexException(String message, Throwable e){
        super(message, e);
    }

    public IndexException(String message, JSONObject json){
        super(message , "  " + name + "信息：" + json.toJSONString());
    }
}