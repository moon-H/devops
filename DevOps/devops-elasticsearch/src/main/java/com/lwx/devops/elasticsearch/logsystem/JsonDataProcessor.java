/*
 * Copyright (c) 2019, shankephone All Rights Reserved.
 * Project Name:log-system
 * Package Name:com.shankephone.log.common
 * Date:2019/12/4 16:51
 *
 */

package com.lwx.devops.elasticsearch.logsystem;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 类名:
 * 描述:
 *
 * @author fengql
 * @since JDK 1.8
 * 时间: 2019/12/4 16:51
 */
@Slf4j
public class JsonDataProcessor {

    /**
     * 处理数据：处理成索引文档的结构
     * @param jsonData
     * @return
     */
    public static JSONObject processData(JSONObject jsonData) {
        //log.info("处理后数据：" + jsonData.toJSONString());

        //设置初始值
        jsonData = initData(jsonData);

        //日志类型处理
        jsonData = getLogCategory(jsonData);

        //处理日志时间戳
        jsonData = handleTime(jsonData);

        JSONObject msg = getMsgObject(jsonData);
        if(msg != null){
            //标识异常
            jsonData = handleException(jsonData, msg);
            //处理请求消息
            jsonData = handleRequest(jsonData, msg);
            //响应消息中的payload对象
            jsonData = handleResponse(jsonData, msg);
            jsonData.put("msg",msg);
        } else {
            msg = new JSONObject();
            jsonData.put("msg",msg);
        }
        //添加全文检索字段
        jsonData = handleSearchKey(jsonData);

        //log.info("日志类型：" + jsonData.getString("logCategory") + "-" + jsonData.getString("timestampFmt"));
        return jsonData;
    }

    /**
     * 处理异常
     * @param jsonData
     * @param msg
     * @return
     */
    public static JSONObject handleException(JSONObject jsonData, JSONObject msg) {
        Pattern p1 = Pattern.compile("\\w+(\\.[\\w$]*)*\\.[\\w$]*Exception:");
        Pattern p2 = Pattern.compile("at\\s\\w+(\\.[\\w$]+)+\\(\\w+.\\w+:\\d+\\)");
        Pattern p3 = Pattern.compile("Caused by:\\s\\w+(\\.[\\w$]+)+:");
        boolean result1 = p1.matcher(msg.toJSONString()).find();
        boolean result2 = p2.matcher(msg.toJSONString()).find();
        boolean result3 = p3.matcher(msg.toJSONString()).find();
        if(result1 || result2 || result3){
            jsonData.put("isException", Constants.LOG_EXCEPTION_YES);
        }
        return jsonData;
    }

    /**
     * 设置初始值
     * @param jsonData
     * @return
     */
    public static JSONObject initData(JSONObject jsonData) {
        //设置文档ID
        String uuid = UUID.randomUUID().toString();
        jsonData.put("id", uuid);
        //保存日志报文
        //jsonData.put("logMsg", jsonData.getString("msg"));
        //初始化日志为非异常
        jsonData.put("isException",Constants.LOG_EXCEPTION_NO);
        return jsonData;
    }

    /**
     * 获取消息体字段(msg)的JSON
     * @param jsonData
     * @return
     */
    public static JSONObject getMsgObject(JSONObject jsonData){
        String msgstr = jsonData.getString("msg");
        if(msgstr == null || "".equals(msgstr)){
            return null;
        }
        JSONObject msg = null;
        try {
            msg = jsonData.getJSONObject("msg");
        } catch (Exception e) {
            //log.warn("msg转换json失败：" + msgstr);
            return null;
        }
        if (msg == null || "".equals(msg)) {
            return null;
        }
        return msg;
    }

    /**
     * 添加全文检索字段
     * @param jsonData
     * @return
     */
    public static JSONObject handleSearchKey(JSONObject jsonData) {
        StringBuffer searchKey = new StringBuffer();
        for (String key: Constants.SEARCH_KEY) {
            if(jsonData.containsKey(key)){
                String value = jsonData.getString(key);
                if(value != null && value.length() > 0){
                    if(!"".equals(searchKey)){
                        searchKey.append(" ");
                    }
                    if("timestamp".equals(key)){
                        Date date = jsonData.getDate(key);
                        String dayFmt = DateUtils.formatDate(date, "yyyy-MM-dd");
                        String day = DateUtils.formatDate(date, "yyyyMMdd");
                        String hour = DateUtils.formatDate(date, "yyyyMMddHH");
                        String minute = DateUtils.formatDate(date, "yyyyMMddHHmm");
                        searchKey.append(" " + dayFmt + " " + day + " " + " " + hour + " " + minute);
                    } else {
                        searchKey.append(value);
                    }
                }
            }
        }
        String tmp = searchKey.toString().replaceAll("\\s", "");
        if(!"".equals(tmp)){
            jsonData.put("searchKey", searchKey);
        }
        String currentTime = DateUtils.formatDate(new Date(),"yyyy-MM-dd HH:mm:ss.SSS");
        jsonData.put("indexTime", currentTime);
        return jsonData;
    }


    /**
     * 处理响应消息数据
     * @param jsonData
     * @param msg
     * @return
     */
    public static JSONObject handleResponse(JSONObject jsonData, JSONObject msg) {
        //处理响应消息
        JSONObject response =  msg.getJSONObject("responseLoggingMessage");
        jsonData.put("responseBody", response.toJSONString());
        if(response == null || "".equals(response)){
            return jsonData;
        }
        JSONObject payload = null;
        int logCategory = jsonData.getInteger("logCategory");
        if(logCategory == Constants.LOG_CATEGORY_REQUEST.intValue()){
            String payloadStr = response.getString("payload");
            if(payloadStr != null && !"".equals(payloadStr)){

                try {
                    payload = JSONObject.parseObject(payloadStr);
                    String retCode = payload.getString("retCode");
                    String retMsg = payload.getString("retMsg");
                    jsonData.put("retMsg", retMsg);
                    //========处理响应码
                    //如果是0000改为0
                    if(retCode != null && retCode.equals("0000")){
                        payload.put("retCode", Constants.RESULT_CODE_SUCCESS);
                        jsonData.put("retCode", Constants.RESULT_CODE_SUCCESS);
                    }
                    //如果包含非数字，返回99
                    boolean result = RegExpUtils.containsNoNumber(retCode);
                    if (result) {
                        payload.put("retCode", Constants.RESULT_CODE_OTHER);
                        jsonData.put("retCode", Constants.RESULT_CODE_OTHER);
                    } else {
                        payload.put("retCode", Integer.parseInt(retCode));
                        jsonData.put("retCode", Integer.parseInt(retCode));
                    }
                    response.put("payload", payload);
                    jsonData.getJSONObject("msg").put("responseLoggingMessage", response);
                } catch (Exception e) {
                    log.error("响应消息payload非json类型：" + payloadStr);
                }
            }
        }
        if(logCategory == Constants.LOG_CATEGORY_WEBSERVICE.intValue()){
            String payloadStr = response.getString("payload");
            if(payloadStr != null && !"".equals(payloadStr.trim())){
                payload = XmlUtils.parseToJson(payloadStr);
                if(payload != null && !"".equals(payload)){
                    String interfaceName = payload.getString("interfaceName");
                    if(interfaceName != null && !"".equals(interfaceName)){
                        JSONObject params = payload.getJSONObject(interfaceName);
                        String retMsg = params.getString("respCodeMemo");
                        String retCode = params.getString("respCode");
                        jsonData.put("retMsg", retMsg);
                        //========处理响应码
                        if(retCode == null || "".equals(retCode)){
                            jsonData.put("isException", Constants.LOG_EXCEPTION_YES);
                            payload = new JSONObject();
                            payload.put("text", payloadStr);
                            payload.put("retCode", Constants.RESULT_CODE_OTHER);
                            payload.put("retMsg", "payload解析异常：请参照payload.text信息");
                            response.put("payload", payload);
                            jsonData.put("retCode", Constants.RESULT_CODE_OTHER);
                            jsonData.put("retMsg", payloadStr);
                            log.info("webservice异常日志! 返回payload: " + payloadStr);
                        } else {
                            //如果是0000改为0
                            if(retCode.equals("0000")){
                                payload.put("retCode", Constants.RESULT_CODE_SUCCESS);
                                jsonData.put("retCode", Constants.RESULT_CODE_SUCCESS);
                            }
                            //如果包含非数字，返回99
                            boolean result = RegExpUtils.containsNoNumber(retCode);
                            if (result) {
                                payload.put("retCode", Constants.RESULT_CODE_OTHER);
                                jsonData.put("retCode", Constants.RESULT_CODE_OTHER);
                            } else {
                                payload.put("retCode", Integer.parseInt(retCode));
                                jsonData.put("retCode", Integer.parseInt(retCode));
                            }
                            payload.put("retMsg", retMsg);
                            jsonData.put("retMsg", retMsg);
                            response.put("payload", payload);
                        }
                    }

                } else {
                    payload = new JSONObject();
                    payload.put("text", payloadStr);
                    payload.put("retCode", Constants.RESULT_CODE_OTHER);
                    payload.put("retMsg", "payload解析异常：请参照payload.text信息");
                    response.put("payload", payload);
                }
            }
        }
        String processingTime = response.getString("processingTime");
        if(processingTime != null && !"".equals(processingTime.trim())){
            response.put("processingTime", Integer.parseInt(processingTime));
            jsonData.put("processingTime", Integer.parseInt(processingTime));
        }
        String responseCode = response.getString("responseCode");
        if(responseCode != null && !"".equals(responseCode.trim())){
            response.put("responseCode", Integer.parseInt(responseCode));
            jsonData.put("responseCode", Integer.parseInt(responseCode));
        }

        //提升业务数据（deviceId和orderNo)
        Map<String,Object> map = handleTopData(jsonData, response, payload);
        jsonData.putAll(map);
        return jsonData;
    }

    public static Map<String, Object> handleTopData(JSONObject jsonData, JSONObject body, JSONObject payload) {
        Map<String, Object> map = new HashMap<String, Object>();
        //=====设置设备ID
        //先获取顶级设备ID
        String deviceId = jsonData.getString("deviceId");
        if(deviceId == null || "".equals(deviceId.trim()) || "null".equals(deviceId.trim())){
            deviceId = body.getString("deviceId");
            //如果设备ID为空，获取请求的payload中的设备ID
            if(deviceId == null || "".equals(deviceId.trim()) || "null".equals(deviceId.trim())){
                if(payload != null && !"".equals(payload)){
                    Object o = getValue(payload, "deviceId", String.class);
                    if(o == null){
                        deviceId = "";
                    } else {
                        deviceId = o.toString();
                    }
                }
            }
        }
        map.put("deviceId",deviceId);

        //=====设置订单号
        //先获取顶级的订单号
        String orderNo = jsonData.getString("orderNo");
        if(orderNo == null || "".equals(orderNo.trim()) || "null".equals(orderNo.trim())){
            orderNo = body.getString("orderNo");
            //如果订单号为空，获取请求的payload中的订单号
            if(orderNo == null || "".equals(orderNo.trim()) || "null".equals(orderNo.trim())){
                if(payload != null && !"".equals(payload)){
                    Object o = getValue(payload, "orderNo", String.class);
                    if(o == null){
                        orderNo = "";
                    } else {
                        orderNo = o.toString();
                    }
                }
            }
        }
        map.put("orderNo",orderNo);
        return map;
    }

    public static <T> Object getValue(JSONObject payload, String key, Class<T> clazz) {
        Set<String> keySet = payload.keySet();
        T t = null;
        for (String s : keySet) {
            if(key.equalsIgnoreCase(s)){
                t = clazz.cast(payload.getString(s));
                break;
            } else {
                try {
                    JSONObject j = payload.getJSONObject(s);
                    t = (T)getValue(j, key, clazz);
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return t;
    }

    /**
     * 处理请求消息数据
     * @param jsonData
     * @param msg
     * @return
     */
    public static JSONObject handleRequest(JSONObject jsonData, JSONObject msg) {
        JSONObject request = msg.getJSONObject("requestLoggingMessage");
        jsonData.put("requestBody", request.toJSONString());
        if(request == null || "".equals(request)){
            return jsonData;
        }
        //======设置请求ID
        String id = request.getString("id");
        jsonData.put("requestId", id);

        //========设置请求处理时间
        String handleDate = request.getString("handlerDate");
        Date date = DateUtils.parseDateTime(handleDate);
        jsonData.put("requestTime", date);
        jsonData.put("requestTimeFmt", handleDate);

        //=========处理Payload
        jsonData = handlePayload(jsonData, request);

        //=========接口名称处理
        //HTTP接口名称源于地址截取
        String url = request.getString("address");
        int logCategory = jsonData.getInteger("logCategory");
        if(logCategory == Constants.LOG_CATEGORY_REQUEST.intValue()){
            if(url != null && !"".equals(url)){
                int idx = url.lastIndexOf("/");
                String addressName = url.substring(idx + 1);
                jsonData.put("interfaceName", addressName);
            }
        }
        //WEBSERVICE接口名称取自payload,在xml转换时已经处理，故在这里不再处理
        if(logCategory == Constants.LOG_CATEGORY_WEBSERVICE.intValue()){
            JSONObject payload = request.getJSONObject("payload");
            String interfaceName = payload.getString("interfaceName");
            jsonData.put("interfaceName", interfaceName);
        }

        JSONObject payload = request.getJSONObject("payload");
        Map<String,Object> map = handleTopData(jsonData, request, payload);
        jsonData.putAll(map);
        return jsonData;
    }

    /**
     * 处理请求的payload
     * @param jsonData
     * @param request
     * @return
     */
    public static JSONObject handlePayload(JSONObject jsonData, JSONObject request) {
        int logCategory = jsonData.getInteger("logCategory");
        JSONObject payload = null;
        if(logCategory == Constants.LOG_CATEGORY_REQUEST.intValue()){
            String payloadStr = request.getString("payload");
            payload = handleParameters(payloadStr);
        }
        if(logCategory == Constants.LOG_CATEGORY_WEBSERVICE.intValue()){
            String payloadStr = request.getString("payload");
            payload = XmlUtils.parseToJson(payloadStr);
        }
        if(payload != null && !"".equals(payload)){
            //=========订单号处理
            if (payload.containsKey("orderNo")){
                jsonData.put("orderNo", payload.getString("orderNo"));
            }
            //补票订单接口（mainOrderNo：主订单号）
            if (payload.containsKey("mainOrderNo")){
                jsonData.put("orderNo", payload.getString("mainOrderNo"));
            }
            //=========手机号处理
            if (payload.containsKey("ownerId")){
                jsonData.put("ownerId", payload.getString("ownerId"));
            }
            //补票订单接口（mainOrderNo：主订单号）
            if (payload.containsKey("orderOwnerId")){
                jsonData.put("ownerId", payload.getString("orderOwnerId"));
            }
            request.put("payload", payload);
        } else {
            payload = new JSONObject();
            payload.put("text", request.getString("payload"));
            payload.put("retCode", Constants.RESULT_CODE_OTHER);
            payload.put("retMsg", "payload解析异常：请参照payload.text信息");
            request.put("payload", payload);
        }
        return jsonData;
    }

    /**
     * 处理时间数据
     * @param jsonData
     * @return
     */
    public static JSONObject handleTime(JSONObject jsonData) {
        String timestamp = jsonData.getString("timestamp");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {
            Date d = sdf.parse(timestamp);
            jsonData.put("timestamp", d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        jsonData.put("timestampFmt", timestamp);
        return jsonData;
    }

    /**
     * 日志索引分类：0.主线程启动日志，1.请求日志，2.接口调用日志
     * @param jsonData
     * @return
     */
    public static JSONObject getLogCategory(JSONObject jsonData){
        int logCategory = Constants.LOG_CATEGORY_GENERAL;
        String msgstr = jsonData.getString("msg");
        if(msgstr == null || "".equals(msgstr)){
            jsonData.put("logCategory", logCategory);
            return jsonData;
        }
       /* if(msgstr.contains("requestLoggingMessage")){
            logCategory = Constants.LOG_CATEGORY_REQUEST;
            jsonData.put("logCategory", logCategory);
        }*/
        JSONObject msg = null;
        try {
            msg = JSONObject.parseObject(msgstr);
        } catch (Exception e) {
            jsonData.put("logCategory", logCategory);
            jsonData.put("msg", msgstr);
            return jsonData;
        }
        jsonData.put("msg", msg);
        if(msg == null || "".equals(msg)){
            jsonData.put("logCategory", logCategory);
            return jsonData;
        }
        JSONObject request = msg.getJSONObject("requestLoggingMessage");
        if(request == null || "".equals(request)){
            jsonData.put("logCategory", logCategory);
            return jsonData;
        }
        String contentType = request.getString("contentType");
        if(contentType.contains("application") && contentType.contains("form")){
            logCategory = Constants.LOG_CATEGORY_REQUEST;
        }
        if(contentType.contains("text/xml")){
            logCategory = Constants.LOG_CATEGORY_WEBSERVICE;
        }
        String level = jsonData.getString("level");
        if("ERROR".equalsIgnoreCase(level)){
            logCategory = Constants.LOG_CATEGORY_ERROR;
        }
        jsonData.put("logCategory", logCategory);
        return jsonData;
    }

    /**
     * 处理http请求的参数
     * @param str
     * @return
     */
    public static JSONObject handleParameters(String str){
        JSONObject json = new JSONObject();
        if(str != null && !"".equals(str.trim())){
            String [] array = str.split("&");
            for (String s : array) {
                if(s != null && !"".equals(s.trim())){
                    String [] kvs = s.split("=");
                    if(kvs == null || kvs.length == 0){
                        continue;
                    }
                    if(kvs.length == 1){
                        String key = kvs[0];
                        String value = "";
                    } else {
                        String key = kvs[0];
                        String value = kvs[1];
                        if(key.equals("bizData")){
                            JSONObject val = JSONObject.parseObject(value);
                            if (val == null || "".equals(val)){
                                json.put(key, val);
                                continue;
                            }
                            Set<String> keySet = val.keySet();
                            for (String k : keySet) {
                                json.put(k, val.getString(k));
                            }
                        } else {
                            json.put(key, value);
                        }
                    }
                }
            }
        }
        return json;
    }



}