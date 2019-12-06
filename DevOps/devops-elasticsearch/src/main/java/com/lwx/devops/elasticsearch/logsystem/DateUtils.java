/*
 * Copyright (c) 2019, shankephone All Rights Reserved.
 * Project Name:log-system
 * Package Name:com.shankephone.log.util
 * Date:2019/11/19 10:28
 *
 */

package com.lwx.devops.elasticsearch.logsystem;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 类名:
 * 描述:
 * 时间: 2019/11/19 10:28
 *
 * @author fengql
 * @see
 * @since JDK 1.8
 */
@Slf4j
public class DateUtils {

    public final static String pattern = "yyyy-MM-dd HH:mm:ss.SSS";

    public static Date parseDate(String str, String pattern){
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            Date date = sdf.parse(str);
            return date;
        } catch (ParseException e) {
            log.error("解析时间错误： " + str + "-->" + pattern);
            e.printStackTrace();
            return null;
        }
    }

    public static Date parseDateTime(String str){
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            Date date = sdf.parse(str);
            return date;
        } catch (ParseException e) {
            log.error("解析时间错误： " + str + "-->" + pattern);
            e.printStackTrace();
            return null;
        }
    }

    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static String formatDate(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

}