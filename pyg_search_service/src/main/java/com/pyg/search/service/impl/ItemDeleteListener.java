package com.pyg.search.service.impl;

import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
@Component
public class ItemDeleteListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodsIds = (Long[]) objectMessage.getObject();
            System.out.println("ItemDeleteListener监听接收到消息..."+goodsIds);
            List<Long> longs = Arrays.asList(goodsIds);
            itemSearchService.deleteByGoodsIds(longs);
            System.out.println("成功删除索引库中的记录");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
