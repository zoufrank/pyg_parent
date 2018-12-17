package com.pyg.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

@Component
public class ItemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
       TextMessage textMessage= (TextMessage) message;
        try {
            String text = textMessage.getText();
            List<TbItem> tbItems = JSON.parseArray(text, TbItem.class);
            for(TbItem item:tbItems){
                System.out.println(item.getId()+" "+item.getTitle());
                Map specMap= JSON.parseObject(item.getSpec());//将spec字段中的json字符串转换为map
                item.setSpecMap(specMap);//给带注解的字段赋值
            }
            itemSearchService.importList(tbItems);
            System.out.println("成功导入到索引库");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
