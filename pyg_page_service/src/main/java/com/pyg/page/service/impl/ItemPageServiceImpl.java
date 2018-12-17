package com.pyg.page.service.impl;

import com.pyg.mapper.TbGoodsDescMapper;
import com.pyg.mapper.TbGoodsMapper;
import com.pyg.mapper.TbItemCatMapper;
import com.pyg.mapper.TbItemMapper;
import com.pyg.page.service.ItemPageService;
import com.pyg.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Service
public class ItemPageServiceImpl implements ItemPageService {
    @Autowired
    private FreeMarkerConfig freeMarkerConfig;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;

    @Value("${pagedir}")
    private String pagedir;


    @Override
    public boolean genItemHtml(Long goodsId) {
        try {
            Configuration configuration = freeMarkerConfig.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");
            Map dataModel = new HashMap();
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods",tbGoods);
            TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc",tbGoodsDesc);

            String ItemCat1 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id()).getName();
            String ItemCat2 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id()).getName();
            String ItemCat3 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName();

            dataModel.put("itemCat1", ItemCat1);
            dataModel.put("itemCat2", ItemCat2);
            dataModel.put("itemCat3", ItemCat3);


            TbItemExample example= new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");
            criteria.andGoodsIdEqualTo(goodsId);
            example.setOrderByClause("is_default desc");
            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList",itemList);

            Writer out = new FileWriter(new File(pagedir+goodsId+".html"));
            template.process(dataModel,out);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try {
            for(Long goodsId:goodsIds){
                new File(pagedir+goodsId+".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
