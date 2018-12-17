package com.pyg.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyg.cart.service.CartService;
import com.pyg.group.Cart;
import com.pyg.utils.CookieUtil;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.rmi.CORBA.Util;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
public class CartController {
    @Reference(timeout=6000)
    private CartService cartService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private HttpServletResponse httpServletResponse;

    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String cartList = CookieUtil.getCookieValue(httpServletRequest, "cartList", "utf-8");
        if(cartList==null||cartList.equals("")){
            cartList="[]";
        }
        List<Cart> carts = JSON.parseArray(cartList, Cart.class);
        if(username.equals("anonymousUser")){//如果未登录
            //读取本地购物车//
            return carts;
        }else{//如果已登录
            List<Cart> cartList_redis =cartService.findCartListFromRedis(username);//从redis中提取
            if(carts.size()>0){//如果本地存在购物车
                //合并购物车
                cartList_redis=cartService.mergeCartList(cartList_redis, carts);
                //清除本地cookie的数据
                CookieUtil.deleteCookie(httpServletRequest, httpServletResponse, "cartList");
                //将合并后的数据存入redis
                cartService.saveCartListToRedis(username, cartList_redis);
            }
            return cartList_redis;
        }

    }

    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId,Integer num){
//        httpServletResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
//        httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户："+username);
        try {
            List<Cart> cartList = findCartList();
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if(username.equals("anonymousUser")) { //如果是未登录，保存到cookie
                CookieUtil.setCookie(httpServletRequest,httpServletResponse,"cartList",JSON.toJSONString(cartList),3600,"utf-8");
                System.out.println("向cookie存入数据");
            }else{//如果是已登录，保存到redis
                cartService.saveCartListToRedis(username, cartList);
            }
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }

    }


}
