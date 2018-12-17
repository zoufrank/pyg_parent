package cn.itcast.sms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SmsListener {
    @Autowired
    private SmsUtil smsUtil;
    @JmsListener(destination = "sms")
    public void sendSms(Map map){
        smsUtil.SmsSend((String) map.get("phoneNumber"),(int)map.get("random"));
    }
}
