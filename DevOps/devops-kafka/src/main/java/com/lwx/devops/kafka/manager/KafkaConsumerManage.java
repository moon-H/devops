package com.lwx.devops.kafka.manager;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.JsonElement;
import com.lwx.devops.elasticsearch.EsManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @description: kafka消费者
 * @author: liwx
 * @create: 2019-11-09 20:26
 **/
@Component
public class KafkaConsumerManage {
    private static Logger logger = LoggerFactory.getLogger(KafkaConsumerManage.class);
    private EsManager esManager = new EsManager();

    @KafkaListener(topics = KafkaProducerManage.TOPIC_S1_LOG, groupId = "group1")
    public void onMessage(String message) {
        //insertIntoDb(buffer);//这里为插入数据库代码
        logger.debug("### kafka消费消息 [{}]", message);
        JSONObject jsonObject = new JSONObject(message);
        String msgJb = jsonObject.getString("msg");
        logger.debug("Msg原文 [{}]", msgJb);
        try {
            JSONObject jsonObject2 = new JSONObject(msgJb);
            logger.debug("########### jsonObject成功");
        } catch (JSONException e) {
            logger.debug("生成 jsonObject异常");
        }


    }

    /**
     * 获取第一层 对象
     *
     * @return
     */
    private JSONObject parseFirstHierarchy(JSONObject jb) {
        JSONObject firstHierarchy = new JSONObject();
        firstHierarchy.put("timestamp", jb.getString("timestamp"));
        firstHierarchy.put("serverName", jb.getString("serverName"));
        firstHierarchy.put("handlerServerIp", jb.getString("handlerServerIp"));
        firstHierarchy.put("cityCode", jb.getString("cityCode"));
        firstHierarchy.put("thread", jb.getString("thread"));
        firstHierarchy.put("level", jb.getString("level"));
        firstHierarchy.put("logger", jb.getString("logger"));
        return firstHierarchy;
    }

    /**
     * 获取msg 对象
     *
     * @return
     */
    private JSONObject parseMsg(JSONObject jb) {
        JSONObject msg = new JSONObject();
        if (jb.isNull("requestLoggingMessage")) {
            //包含requestLoggingMessage，当做请求报文处理
            JSONObject orgJb = jb.getJSONObject("requestLoggingMessage");
            JSONObject req = new JSONObject();
            req.put("address", orgJb.getString("address"));
            req.put("handlerDate", orgJb.getString("handlerDate"));
        }
        return msg;
    }
//    @KafkaListener(topics = KafkaProducerManage.TOPIC_S1_LOG, groupId = "group1")
//    public void onMessage2(String message) {
//        //insertIntoDb(buffer);//这里为插入数据库代码
//        logger.debug("### kafka消费消息2 [{}]", message);
//        JSONObject jsonObject=new JSONObject(message);
//
//    }
}
