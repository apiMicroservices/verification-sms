package com.apimicroservices.verificationpic.service;

import java.util.Map;

public interface SmsService {
    /**
     * 发送短信验证码
     * @param phone 手机号码
     * @return 发送结果
     */
    Map<String , Object> send(String phone);

    /**
     * 校验手机验证码
     * @param phone 手机号
     * @param code 用户输入的验证码
     * @return 校验结果
     */
    boolean check(String phone, String code);

    /**
     * 配置短信验证信息
     * @param key 配置键
     * @param code 配置值
     */
    Map<String,Object> config(String key, String code) throws Exception;
}
