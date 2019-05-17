package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.utils.HttpClient;
import com.pinyougou.pay.service.WeixinPayService;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

	@Value("${appid}")
	private String appid;

	@Value("${partner}")
	private String partner;

	@Value("${partnerkey}")
	private String partnerkey;

	/*
	 * (non-Javadoc)生成二维码
	 * 
	 * @see
	 * com.pinyougou.pay.service.WeixinPayService#createNative(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Map createNative(String out_trade_no, String total_fee) {
		// System.out.println(partner+"商户id");
		Map<String, String> param = new HashMap<>();
		param.put("appid", appid);// 公众号
		param.put("mch_id", partner);// 商户号
		param.put("out_trade_no", out_trade_no);// 商户单号
		param.put("nonce_str", WXPayUtil.generateNonceStr());// 随机字符串
		param.put("body", "品优购");// 商品描述
		param.put("total_fee", total_fee);// 标价金额
		param.put("spbill_create_ip", "127.0.0.1");// ip
		param.put("notify_url", "www.https://baidu.com");// 回调地址
		param.put("trade_type", "NATIVE");// 交易类型

		try {
			System.out.println("partnerkey:" + partnerkey);
			String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);// 生成要发送的xml

			HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			client.setHttps(true);// 开启支持https
			client.setXmlParam(signedXml);

			client.post();

			// 获得结果
			String result = client.getContent();
			System.out.println("返回结果" + result);
			Map<String, String> resultMap = WXPayUtil.xmlToMap(result);// 返回的map

			Map<String, String> map = new HashMap<>();
			map.put("code_url", resultMap.get("code_url"));// 获取支付地址
			map.put("total_fee", total_fee);// 返回支付金额
			map.put("out_trade_no", out_trade_no);// 订单号
			return map;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new HashMap<>();
		}

	}

	/* (non-Javadoc)
	 * 
	 * 返回支付状态
	 * @see com.pinyougou.pay.service.WeixinPayService#queryPayStatus(java.lang.String)
	 */
	@Override
	public Map queryPayStatus(String out_trade_no) {
		Map<String, String> param = new HashMap<>();
		param.put("appid", appid);
		param.put("mch_id", partner);// 商户号
		param.put("out_trade_no", out_trade_no);// 订单号
		param.put("nonce_str", WXPayUtil.generateNonceStr());// 随机字符串
		String url = "https://api.mch.weixin.qq.com/pay/orderquery";
		try {
			String signedXml = WXPayUtil.generateSignedXml(param, partnerkey); //
			HttpClient client = new HttpClient(url);
			client.setHttps(true);
			client.setXmlParam(signedXml);
			client.post();
			String result = client.getContent();
			Map<String, String> map = WXPayUtil.xmlToMap(result);
			System.out.println(map);
			return map;

		} catch (Exception e) { 
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public Map closePay(String out_trade_no) {
		//1.封装参数
		Map param=new HashMap();
		param.put("appid", appid);
		param.put("mch_id", partner);
		param.put("out_trade_no", out_trade_no);
		param.put("nonce_str", WXPayUtil.generateNonceStr());
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			//2.发送请求
			HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlParam);
			httpClient.post();
			
			//3.获取结果
			String xmlResult = httpClient.getContent();
			Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
			System.out.println("调动查询API返回结果："+xmlResult);
			
			return map;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
