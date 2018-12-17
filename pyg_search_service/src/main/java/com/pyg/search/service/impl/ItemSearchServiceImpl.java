package com.pyg.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 3000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

//    @Override
//    public Map<String, Object> search(Map searchMap) {
//        Map<String, Object> map=new HashMap<>();
//        Query query = new SimpleQuery();
//        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
//        query.addCriteria(criteria);
//        ScoredPage<TbItem> tbItems = solrTemplate.queryForPage(query, TbItem.class);
//        map.put("rows",tbItems.getContent());
//        return map;
//    }
    private Map searchList(Map searchMap){
        Map<String, Object> map=new HashMap<>();
        HighlightQuery highlightQuery = new SimpleHighlightQuery();
        HighlightOptions item_title = new HighlightOptions().addField("item_title");
        item_title.setSimplePrefix("<em style='color:red'>");
        item_title.setSimplePostfix("</em>");
        highlightQuery.setHighlightOptions(item_title);
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        highlightQuery.addCriteria(criteria);

        if(!"".equals(searchMap.get("category"))){
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFacetQuery(filterCriteria);
            highlightQuery.addFilterQuery(filterQuery);
        }

        if(!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            highlightQuery.addFilterQuery(filterQuery);
        }

        if(searchMap.get("spec")!=null){
            Map<String,String> specMap= (Map) searchMap.get("spec");
            for(String key:specMap.keySet() ){
                Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key) );
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
        }

        if (!"".equals(searchMap.get("price"))){
            String[] prices = ((String) searchMap.get("price")).split("-");
             if(!prices[0].equals("0")){//如果区间起点不等于0
                 Criteria fillterCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);
                 FilterQuery simpleFilterQuery = new SimpleFilterQuery(fillterCriteria);
                 highlightQuery.addFilterQuery(simpleFilterQuery);
             }
             if(!prices[1].equals("*")){
                 Criteria fillterCriteria = new Criteria("item_price").lessThanEqual(prices[1]);
                 FilterQuery simpleFilterQuery = new SimpleFilterQuery(fillterCriteria);
                 highlightQuery.addFilterQuery(simpleFilterQuery);
             }
        }

        Integer pageNo = (Integer) searchMap.get("pageNo");
        if(pageNo==null){
            pageNo=1;//默认第一页
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if(pageSize==null){
            pageSize=20;//默认每页20
        }
        highlightQuery.setOffset((pageNo-1)*pageSize);
        highlightQuery.setRows(pageSize);

        String sort = (String) searchMap.get("sort");
        String sortField = (String) searchMap.get("sortField");
        if(sort!=null&&!sort.equals("")){
            if(sort.equals("ASC")){
                Sort orders = new Sort(Sort.Direction.ASC, "item_" + sortField);
                highlightQuery.addSort(orders);
            }else {
                Sort orders = new Sort(Sort.Direction.DESC, "item_" + sortField);
                highlightQuery.addSort(orders);
            }
        }


        HighlightPage<TbItem> tbItems = solrTemplate.queryForHighlightPage(highlightQuery, TbItem.class);
        for (HighlightEntry<TbItem> tbItemHighlightEntry : tbItems.getHighlighted()) {
            TbItem entity = tbItemHighlightEntry.getEntity();

            if(tbItemHighlightEntry.getHighlights().size()>0&&tbItemHighlightEntry.getHighlights().get(0).getSnipplets().size()>0){
                entity.setTitle(tbItemHighlightEntry.getHighlights().get(0).getSnipplets().get(0));
            }
        }
        map.put("rows",tbItems.getContent());
        map.put("totalPages",tbItems.getTotalPages());//返回总页数
        long totalElements = tbItems.getTotalElements();
        map.put("total",totalElements);//返回总记录数
        return map;
    }
    private List searchCategoryList(Map searchMap){
        ArrayList<String> list = new ArrayList<>();
        SimpleQuery simpleQuery = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        simpleQuery.addCriteria(criteria);
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        simpleQuery.setGroupOptions(groupOptions);

        GroupPage<TbItem> tbItems = solrTemplate.queryForGroupPage(simpleQuery, TbItem.class);
        GroupResult<TbItem> item_category = tbItems.getGroupResult("item_category");
        Page<GroupEntry<TbItem>> groupEntries = item_category.getGroupEntries();
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> tbItemGroupEntry : content) {
            list.add(tbItemGroupEntry.getGroupValue());
        }
        return list;

    }
    private Map searchBrandAndSpecList(String category){
        Map map=new HashMap();
        Long typeid = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if(typeid!=null){
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeid);
            map.put("brandList",brandList);

            List specList = (List) redisTemplate.boundHashOps("specList").get(typeid);
            map.put("specList",specList);
        }
        return map;
    }
    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String,Object> map=new HashMap<>();
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));
        //1.查询列表
        map.putAll(searchList(searchMap));
        //2.根据关键字查询商品分类
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);
//        if(categoryList.size()>0){
//            map.putAll(searchBrandAndSpecList(categoryList.get(0)));
//        }
        String categoryName=(String)searchMap.get("category");
        if(!"".equals(categoryName)){//如果有分类名称
            map.putAll(searchBrandAndSpecList(categoryName));
        }else{//如果没有分类名称，按照第一个查询
            if(categoryList.size()>0){
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }

        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        System.out.println("导入"+list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        Query query = new SimpleQuery();
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
        System.out.println("删除商品ID"+goodsIdList);
    }
}
