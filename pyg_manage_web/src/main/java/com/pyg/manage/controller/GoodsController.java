package com.pyg.manage.controller;
import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.pyg.group.Goods;
//import com.pyg.page.service.ItemPageService;
import com.pyg.pojo.TbItem;
//import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbGoods;
import com.pyg.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

//	@Reference
//	private ItemSearchService itemSearchService;

//	@Reference(timeout = 4000)
//	private ItemPageService itemPageService;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private Destination queueTextDestination;

	@Autowired
	private Destination queueSolrDeleteDestination;

	@Autowired
	private Destination topicPageDestination;

	@Autowired
	private Destination topicPageDeleteDestination;

	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
//			itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param goods
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids,String status){
		try {
			goodsService.updateStatus(ids, status);
			if(status.equals("1")){
				List<TbItem> itemList= goodsService.findItemListByGoodsIdandStatus(ids,status);
				if(itemList.size()>0){
					//itemSearchService.importList(itemList);
					String s = JSON.toJSONString(itemList);
					jmsTemplate.send(queueTextDestination,new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return 	session.createTextMessage(s);
						}
					});
				}else{
					System.out.println("没有明细数据");
				}
				for (Long id : ids) {
					jmsTemplate.send(topicPageDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(id+"");
						}
					});
//					itemPageService.genItemHtml(id);

				}
			}
			return new Result(true,"成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"失败");
		}
	}
//	@RequestMapping("/genHtml")
//	public void genHtml(Long goodsId){
//		itemPageService.genItemHtml(goodsId);
//	}

}
