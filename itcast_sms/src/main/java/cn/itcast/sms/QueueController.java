package cn.itcast.sms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class QueueController {
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;
    @RequestMapping("/sendsms")
    public void sendSms(){
        String phoneNumber ="17719442502";
        jmsMessagingTemplate.convertAndSend("Sms",phoneNumber);
    }
}
