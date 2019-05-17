package com.pinyougou.cart.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;

import entity.Result;
import com.pinyougou.common.utils.IdWorker;;

@RestController
@RequestMapping("/pay")
public class PayController {

	@Reference(timeout=6000)
	private WeixinPayService weixinPayService;
	
	@Reference
	private OrderService orderService;
	
	@RequestMapping("/createNative")
	public Map createNative() {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();// 获取当前登陆的用户名
		System.out.println("========"+name);
		TbPayLog payLog = orderService.searchPayLogFromRedis(name);
		
		if(payLog!=null) {
			
			return weixinPayService.createNative(payLog.getOutTradeNo()+ "", "1");
			
		}else {
			return new HashMap<>();
		}
		
	
	}

	/**查询支付状态
	 * @param out_trade_no
	 * @return
	 */
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		Result result = null;
		System.out.println("----------");
		int i=0;
		while (true) {
			System.out.println(out_trade_no+"sssssss");
			// 调用查询接口
			Map<String, String> map = weixinPayService.queryPayStatus(out_trade_no);
			if (map == null) {// 出错
				result = new Result(false, "支付出错");
				break;
			}
			if (map.get("trade_state").equals("SUCCESS")) {// 如果成功
				result = new Result(true, "支付成功");
				//System.out.println("map=========="+ map.get("transaction_id"));
				//修改订单状态
				orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
				
				break;
			}
			try {
				Thread.sleep(3000);// 间隔三秒
			} catch (InterruptedException e) {
				e.printStackTrace();

			}
			i++;
			if(i>=100) {
				result=new Result(false,"支付超时");
				break;
			}
		}
		System.out.println(result);
		return result;
	}

}
