package com.apimicroservices.verificationpic.controller;

import com.apimicroservices.verificationpic.service.SmsService;
import com.apimicroservices.verificationpic.util.MapUtil;
import com.apimicroservices.verificationpic.util.StringUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Api(value = "验证码服务", description = "验证码服务", tags = {"验证码服务接口"})
@Controller
@RequestMapping("/verification/sms")
public class VerficationSmsController {
    Logger log = LoggerFactory.getLogger(VerficationSmsController.class);
    @Autowired
    SmsService smsService;

    @ApiOperation(value = "发送手机短信验证码")
    @HystrixCommand(fallbackMethod = "defaultFallback")
    @RequestMapping(path = "/get", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "手机号码（使用该功能前请先在后台管理系统配置腾讯云的appid，appkey，templateId）", paramType = "query", dataType = "String", required = true)
    })
    @ResponseBody
    public Map<String, Object> send(String phone) throws Exception {
        log.info("发送手机验证码："+phone);
        Map<String, Object> send = smsService.send(phone);
        return send;
    }

    @ApiOperation("验证手机验证码")
    @HystrixCommand(fallbackMethod = "defaultFallback")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "生成验证码时的标识", paramType = "query", dataType = "String", required = true),
            @ApiImplicitParam(name = "code", value = "用户输入的验证码", paramType = "query", required = true)
    })
    @RequestMapping(path = "/check", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Map<String, Object> check(String phone, String code) {
        log.info("验证手机验证码，phone："+phone+"code:"+code);
        if (StringUtil.isEmpty(phone)) {
            Map<String, Object> map = MapUtil.getMap(300);
            map.put("msg", "缺少phone参数");
            return map;
        }
        if (StringUtil.isEmpty(code)) {
            Map<String, Object> map = MapUtil.getMap(300);
            map.put("msg", "缺少code参数");
            return map;
        }
        if (smsService.check(phone, code)) {
            return MapUtil.getMap(200);
        } else {
            Map<String, Object> map = MapUtil.getMap(-1);
            map.put("msg", "短信验证码错误");
            return map;
        }

    }

    @ApiOperation("配置短信验证码")
    @HystrixCommand(fallbackMethod = "defaultFallback")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "key", value = "配置的属性选项", paramType = "query", required = true),
            @ApiImplicitParam(name = "code", value = "属性选项对应的值", paramType = "query", required = true)
    })
    @RequestMapping(path = "/config", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Map<String, Object> config(String key, String code) throws Exception {
        log.info("配置手机验证码，key"+key+"code"+code);
        if (StringUtil.isEmpty(key)) {
            Map<String, Object> map = MapUtil.getMap(300);
            map.put("msg", "缺少key参数");
            return map;
        }
        if (StringUtil.isEmpty(code)) {
            Map<String, Object> map = MapUtil.getMap(300);
            map.put("msg", "缺少code参数");
            return map;
        }
        return smsService.config(key, code);
    }


    public Map<String, Object> defaultFallback(String key, String code,Throwable throwable) {
        log.error("服务降级处理" + throwable.getMessage());
        Map<String, Object> map = MapUtil.getMap(-1);
        map.put("msg", "服务器内部错误，命令熔断");
        return map;
    }

    public Map<String, Object> defaultFallback(String key,Throwable throwable) {
        log.error("服务降级处理" + throwable.getMessage());
        Map<String, Object> map = MapUtil.getMap(-1);
        map.put("msg", "服务器内部错误，命令熔断");
        return map;
    }

}
