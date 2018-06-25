package com.apimicroservices.verificationpic.service;

import com.apimicroservices.verificationpic.dao.SmsDao;
import com.apimicroservices.verificationpic.util.MapUtil;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
@Service
public class SmsServiceImp implements SmsService {
    @Autowired
    SmsDao smsDao;
    Logger log = LoggerFactory.getLogger(SmsServiceImp.class);

    @Override
    public Map<String, Object> send(String phone) {
        if (phone == null || "".equals(phone)) {
            Map<String, Object> map = MapUtil.getMap(300);
            map.put("msg", "缺少phone参数");
            return map;
        }
        String appid = smsDao.getConfig("appid");
        String appkey = smsDao.getConfig("appkey");
        String templateId = smsDao.getConfig("templateId");
        //不设置使用默认签名
        String smsSign = smsDao.getConfig("smsSign");
        //默认30分钟
        String timeout = smsDao.getConfig("timeout");
        String code = String.valueOf((int) (Math.random() * 9000) + 1000);
        if (appid == null || "".equals(appid)) {
            Map<String, Object> map = MapUtil.getMap(-1);
            map.put("msg", "缺少APPID，请登录后台管理系统配置");
            return map;
        }
        if (appkey == null || "".equals(appkey)) {
            Map<String, Object> map = MapUtil.getMap(-1);
            map.put("msg", "缺少appkey，请登录后台管理系统配置");
            return map;
        }
        if (templateId == null || "".equals(templateId)) {
            Map<String, Object> map = MapUtil.getMap(-1);
            map.put("msg", "缺少templateId，请登录后台管理系统配置");
            return map;
        }
        if (timeout == null || "".equals(timeout)) {
            timeout = "30";
        }
        smsDao.save(phone, code, Long.valueOf(timeout) * 60);
        Map<String, Object> map = MapUtil.getMap();
        try {
            ArrayList<String> list = new ArrayList<>();
            list.add(code);
            list.add(timeout);
            SmsSingleSender ssender = new SmsSingleSender(Integer.valueOf(appid), appkey);
            SmsSingleSenderResult result = ssender.sendWithParam("86", phone, Integer.valueOf(templateId), list, "", "", "");
            map.put("code", 200);
            map.put("msg", "success");
            map.put("smsresult", result.result);
            map.put("errMsg", result.errMsg);
            map.put("fee", result.fee);
            map.put("sid", result.sid);
        } catch (HTTPException e) {
            log.error("http状态码错误", e);
            map.put("code", -1);
            map.put("msg", "http状态码错误");
        } catch (JSONException e) {
            // json解析错误
            log.error("json解析错误", e);
            map.put("code", -1);
            map.put("msg", "json解析错误");
        } catch (IOException e) {
            // 网络IO错误
            log.error("网络IO错误", e);
            map.put("code", -1);
            map.put("msg", "网络IO错误");
        }
        return map;
    }

    @Override
    public boolean check(String phone, String code) {
        String s = smsDao.get(phone);
        if (s == null || "".equals(s) || !s.equals(code)) {
            return false;
        }
        return true;
    }

    @Override
    public Map<String, Object> config(String key, String code) throws Exception {
        ArrayList<String> list = new ArrayList<>();
        list.add("appid");
        list.add("appkey");
        list.add("templateId");
        list.add("smsSign");
        list.add("timeout");
        if (list.contains(key)) {
            smsDao.setConfig(key, code);
            return MapUtil.getMap(200);
        } else {
            Map<String, Object> map = MapUtil.getMap(-1);
            map.put("msg", "不支持的配置项");
            return map;
        }
    }
}
