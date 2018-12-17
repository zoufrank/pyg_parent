package com.pyg.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.cart.service.CartService;
import com.pyg.group.Cart;
import com.pyg.mapper.TbItemMapper;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车数据....."+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if(cartList==null){
            cartList=new ArrayList();
        }
        return cartList;
    }
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis存入购物车数据....."+username);
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        System.out.println("合并购物车");
        for(Cart cart: cartList2){
            for(TbOrderItem orderItem:cart.getOrderItemList()){
                cartList1= addGoodsToCartList(cartList1,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList1;
    }

    //1.根据商品SKU ID查询SKU商品信息
    //2.获取商家ID
    //3.根据商家ID判断购物车列表中是否存在该商家的购物车
    //4.如果购物车列表中不存在该商家的购物车
    //4.1 新建购物车对象
    //4.2 将新建的购物车对象添加到购物车列表
    //5.如果购物车列表中存在该商家的购物车
    // 查询购物车明细列表中是否存在该商品
    //5.1. 如果没有，新增购物车明细
    //5.2. 如果有，在原购物车明细上添加数量，更改金额
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
        if(tbItem==null){
            throw new RuntimeException("商品不存在");
        }
        if(!tbItem.getStatus().equals("1")){
            throw new RuntimeException("商品状态无效");
        }
        String sellerId = tbItem.getSellerId();
        Cart cart = searchCartBySellerId(cartList,sellerId);
        if (cart==null){
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(tbItem.getSeller());
            TbOrderItem orderItem = createOrderItem(tbItem,num);
            List orderItemList=new ArrayList();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2将购物车对象添加到购物车列表
            cartList.add(cart);
        }else{
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
            if (orderItem==null){
                orderItem=createOrderItem(tbItem,num);
                cart.getOrderItemList().add(orderItem);
            }else {
                orderItem.setNum(orderItem.getNum()+ num);
                orderItem.setTotalFee(new BigDecimal(tbItem.getPrice().doubleValue()*orderItem.getNum()));
                if(orderItem.getNum()<=0){
                    cart.getOrderItemList().remove(orderItem);
                }
                if(cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem tbOrderItem : orderItemList) {
            if(tbOrderItem.getItemId().longValue()==itemId.longValue()){
                return tbOrderItem;
            }
        }
        return null;
    }

    private TbOrderItem createOrderItem(TbItem tbItem, Integer num) {
        if(num<=0){
            throw new RuntimeException("数量非法");
        }
        TbOrderItem tbOrderItem = new TbOrderItem();
        tbOrderItem.setGoodsId(tbItem.getGoodsId());
        tbOrderItem.setItemId(tbItem.getId());
        tbOrderItem.setNum(num);
        tbOrderItem.setPicPath(tbItem.getImage());
        tbOrderItem.setPrice(tbItem.getPrice());
        tbOrderItem.setSellerId(tbItem.getSellerId());
        tbOrderItem.setTitle(tbItem.getTitle());
        tbOrderItem.setTotalFee(new BigDecimal(tbItem.getPrice().doubleValue()*num));
        return tbOrderItem;
    }

    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }
}
