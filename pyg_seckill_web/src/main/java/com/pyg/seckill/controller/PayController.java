package com.pyg.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pay.service.WeixinPayService;
import com.pyg.pojo.TbPayLog;
import com.pyg.pojo.TbSeckillOrder;
import com.pyg.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference(timeout = 50000)
    private WeixinPayService weixinPayService;

    @Reference(timeout = 50000)
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/createNative")
    public Map createNative(){
        //获取当前用户
        String userId= SecurityContextHolder.getContext().getAuthentication().getName();
        //到redis查询支付日志
        TbSeckillOrder tbSeckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userId);
        if(tbSeckillOrder!=null){
            long fen=  (long)(tbSeckillOrder.getMoney().doubleValue()*100);//金额（分）
            return weixinPayService.createNative(tbSeckillOrder.getId()+"",fen+"");
        }else {
            return new HashMap();
        }
    }
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        String userId=SecurityContextHolder.getContext().getAuthentication().getName();
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
                seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), map.get("transaction_id"));
                break;
            }
            try {
                Thread.sleep(3000);//间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if(x>=20){
                result=new  Result(false, "二维码超时");
                Map<String,String> payresult = weixinPayService.closePay(out_trade_no);
                if( !"SUCCESS".equals(payresult.get("result_code")) ){//如果返回结果是正常关闭
                    if("ORDERPAID".equals(payresult.get("err_code"))){
                        result=new Result(true, "支付成功");
                        seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), map.get("transaction_id"));
                    }
                }
                if(result.isSuccess()==false){
                    System.out.println("超时，取消订单");
                    //2.调用删除
                    seckillOrderService.deleteOrderFromRedis(userId, Long.valueOf(out_trade_no));
                }
                break;
            }
        }
        return result;
    }
}
