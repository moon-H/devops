package com.lwx.devops.common.aop;

import lombok.Data;

/**
 * @description: 积累
 * @author: liwx
 * @create: 2019-11-27 16:22
 **/
@Data
public class BaseResponse {
    private String retCode;
    private String retMsg;

}
