package com.pyg.sellergoods.service;

import com.pyg.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService {
    /*
     *@Description 返回品牌列表
     *@Param []
     *@return java.util.List<com.pyg.sellergoods.service.TbBrand>
     **/
    public List<TbBrand> findAll();
    public PageResult findPage(int pageNum, int pageSize);
    public void add(TbBrand brand);
    /**
     * 修改
     */
    public void update(TbBrand brand);
    /**
     * 根据ID获取实体
     * @param id
     * @return
     */
    public TbBrand findOne(Long id);
    public void delete(Long[] ids);
    public PageResult findPage(TbBrand brand, int pageNum,int pageSize);
    List<Map> selectOptionList();
}
