package com.lwx.devops.elasticsearch.logsystem;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

@Slf4j
public class XmlUtils {

    @SuppressWarnings("unchecked")
    public static <T> T xml2Java(String resource, Class<T> clazz) {
        try {
            URL xmlUrl = ClassUtils.getResource(resource);
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller u = context.createUnmarshaller();
            return (T) u.unmarshal(xmlUrl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static <T> void java2XmlString(T t, OutputStream output) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(t.getClass());
        Marshaller m = context.createMarshaller();
        m.marshal(t, output);
    }

    public static JSONObject parseToXml(String xmlStr) {
        JSONObject json = new JSONObject();
        Document document = null;
        try {
            document = DocumentHelper.parseText(xmlStr);
        } catch (DocumentException e) {
            log.error("解析xml文本异常！" + xmlStr);
            throw new IndexException("解析xml异常！" + xmlStr, e);
        }
        Element root = document.getRootElement();
        List<Element> list = root.elements();
        Element body = list.get(0);
        List<Element> message = body.elements();
        for (Element element : message) {
            String callName = element.getName();
            List<Element> params = element.elements();
            //TODO 接口名称处理
            json.put("interfaceName", callName);
            for (Element param : params) {
                String paramName = param.getName();
                String paramValue = param.getText();
                json.put(paramName, paramValue);
            }
        }
        int len = json.size();
        if (len == 0) {
            return null;
        }
        return json;

    }

    public static JSONObject parseToJson(String xmlStr) {
        Document document = null;
        try {
            document = DocumentHelper.parseText(xmlStr);
        } catch (DocumentException e) {
            log.error("解析xml文本异常！" + xmlStr);
            throw new IndexException("解析xml异常！" + xmlStr, e);
        }
        Node node = document.selectSingleNode("/soap:Envelope/soap:Body");
        Element body = (Element) node;
        List<Element> list = body.elements();
        JSONObject json = new JSONObject();
        for (Element el : list) {
            json.put("interfaceName", el.getName());
            recursion(el, json);
        }
        return json;
    }

    public static JSONObject recursion(Element el, JSONObject json) {
        String name = el.getName();
        if (el.hasContent()) {
            JSONObject j = new JSONObject();
            json.put(name, j);
            List<Element> list = el.elements();
            if (list.size() > 0) {
                for (Element e : list) {
                    recursion(e, j);
                }
            } else {
                json.put(name, el.getText());
            }
        } else {
            json.put(name, "");
        }
        return json;
    }


}
