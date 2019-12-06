/*
 * Copyright (c) 2019, shankephone All Rights Reserved.
 * Project Name:log-system
 * Package Name:com.shankephone.log.util
 * Date:2019/11/21 11:32
 *
 */

package com.lwx.devops.elasticsearch.logsystem;

import java.util.regex.Pattern;

/**
 * 类名:
 * 描述:
 * 时间: 2019/11/21 11:32
 *
 * @author fengql
 * @see
 * @since JDK 1.8
 */
public class RegExpUtils {

    public static boolean containsNoNumber(String str){
        Pattern pattern = Pattern.compile("\\D");
        boolean result = pattern.matcher(str).find();
        return result;
    }

}