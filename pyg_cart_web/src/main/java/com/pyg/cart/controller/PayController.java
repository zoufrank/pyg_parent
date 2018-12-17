package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.order.service.OrderService;
import com.pyg.pay.service.WeixinPayService;
import com.pyg.pojo.TbPayLog;
import com.pyg.utils.IdWorker;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference(timeout = 6000)
    private WeixinPayService weixinPayService;
    @Reference(timeout = 6000)
    private OrderService orderService;

    @RequestMapping("/createNative")
    private Map createNative(String total_fee){
        //获取当前用户
        String userId=SecurityContextHolder.getContext().getAuthentication().getName();
        //到redis查询支付日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        if(payLog!=null){
            return weixinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        }else {
            return new HashMap();
        }

    }
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result=null;
        int x=0;
        while(true){
            //调用查询接口
            Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
            System.out.println(map);
            if(map==null){//出错
                result=new Result(false, "支付出错");
                break;
            }
            if(map.get("trade_state").equals("SUCCESS")){//如果成功
                result=new  Result(true, "支付成功");
                break;
            }
            try {
                Thread.sleep(3000);//间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if(x>=100){
                result=new  Result(false, "二维码超时");
                break;
            }
        }
        return result;
    }
}
