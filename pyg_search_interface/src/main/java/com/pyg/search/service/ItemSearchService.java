package com.pyg.search.service;

import com.pyg.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    public Map<String,Object> search(Map searchMap);
    public void importList(List list);
    public void deleteByGoodsIds(List goodsIdList);

}
