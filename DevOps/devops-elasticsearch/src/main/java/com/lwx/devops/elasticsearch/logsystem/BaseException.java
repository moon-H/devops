/*
 * Copyright (c) 2019, shankephone All Rights Reserved.
 * Project Name:search-parent
 * Package Name:com.shankephone.search.indexer.common
 * Date:2019/11/14 11:07
 *
 */

package com.lwx.devops.elasticsearch.logsystem;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

/**
 * 类名:
 * 描述:
 * 时间: 2019/11/14 11:07
 *
 * @author fengql
 * @see
 * @since JDK 1.8
 */
@Slf4j
public class BaseException extends RuntimeException{

    public BaseException(){
        super();
    }

    public BaseException(String msg){
        super(msg);
    }

    public BaseException(String message, Throwable e){
        super(message, e);
    }

    public BaseException(String message, JSONObject json){
        super(message + " ： " + json.toJSONString());
    }

    public BaseException(String message, String error){
        super(message + " ： " + error);
    }

    // 根据异常种类返回不同的结果
    public static boolean whichException(Exception e) {
        try {
            if (e instanceof java.net.ConnectException || e instanceof java.net.SocketTimeoutException
                    || e instanceof org.apache.http.ConnectionClosedException || e instanceof java.net.SocketException) {
                // 满足以上连接elasticsearch集群连不上的网络等问题，要不断重试
                log.error("捕获了按类型分类的异常" + e.getMessage());
                return true;
            }
            if (e != null && e.getMessage() != null) {
                if (e.getMessage().contains("type=no_shard_available_action_exception")
                        || e.getMessage().contains("type=version_conflict_engine_exception")
                        || e.getMessage().contains("type=node_not_connected_exception")
                        || e.getMessage().contains("type=index_not_found_exception")) {
                    log.error("捕获了按内容分类的异常" + e.getMessage());
                    return true;
                }
            }
        } catch (Exception e2) {
            return false;
        }
        return false;
    }

}