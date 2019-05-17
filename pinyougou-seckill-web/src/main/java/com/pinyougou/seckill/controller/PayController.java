package com.pinyougou.seckill.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.Result;
import com.pinyougou.common.utils.IdWorker;;

@RestController
@RequestMapping("/pay")
public class PayController {

	@Reference(timeout = 6000)
	private WeixinPayService weixinPayService;

	@Reference
	private SeckillOrderService seckillOrderService;

	@RequestMapping("/createNative")
	public Map createNative() {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();// 获取当前登陆的用户名
		System.out.println("========" + name);
		TbSeckillOrder searchOrderFromRedisByUserId = seckillOrderService.searchOrderFromRedisByUserId(name);

		if (searchOrderFromRedisByUserId != null) {

			return weixinPayService.createNative(searchOrderFromRedisByUserId.getId() + "",
					(long) ((searchOrderFromRedisByUserId.getMoney().doubleValue()) * 100) + "");

		} else {
			return new HashMap<>();
		}

	}

	/**
	 * 查询支付状态
	 * 
	 * @param out_trade_no
	 * @return
	 */
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		Result result = null;
		System.out.println("----------");
		int i = 0;
		while (true) {
			System.out.println(out_trade_no + "sssssss");
			// 调用查询接口
			Map<String, String> map = weixinPayService.queryPayStatus(out_trade_no);
			if (map == null) {// 出错
				result = new Result(false, "支付出错");
				break;
			}
			if (map.get("trade_state").equals("SUCCESS")) {// 如果成功
				result = new Result(true, "支付成功");
				// System.out.println("map=========="+ map.get("transaction_id"));
				// 修改订单状态
				System.out.println(" 修改订单状态:支付成功");
				seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no),
						map.get("transaction_id"));

				break;
			}
			try {
				Thread.sleep(3000);// 间隔三秒
			} catch (InterruptedException e) {
				e.printStackTrace();

			}
			i++;
			if (i >= 4) {
				result = new Result(false, "二维码超时");

				System.out.println("二维码超时");

				// 关闭支付
				Map<String, String> payResult = weixinPayService.closePay(out_trade_no);
				System.out.println("payResult"+payResult); 
				if (payResult != null && "FAIL".equals(payResult.get("return_code"))) {
					System.out.println("FAIL");
					if ("ORDERPAID".equals(payResult.get("err_code"))) {
						System.out.println("支付成功");
						result = new Result(true, "支付成功");
						// 保存订单
						seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), map.get("transaction_id"));
					}
				}

				// 删除订单
				if (result.getSuccess() == false) {
					System.out.println("删除订单");
					seckillOrderService.deleteOrderFromRedis(userId, Long.valueOf(out_trade_no));
				}
				break;
			}
		}
		System.out.println(result);
		return result;
	}

}
