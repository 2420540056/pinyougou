package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.utils.CookieUtil;
import com.pinyougou.pojogroup.Cart;

import entity.Result;

@RestController
@RequestMapping("/cart")
public class CartController {

	@Reference(timeout = 6000)
	private CartService cartService;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private HttpServletResponse response;

	/**
	 * 从cookie中获取购物车
	 * 
	 * @return
	 */
	@RequestMapping("/findCartList")
	public List<Cart> findCartList() {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();// 获取当前登陆的用户名

		// 读取本地购物车
		String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		if (cartListString == null || cartListString.equals("")) {
			cartListString = "[]";
		}
		// 将cartListString转为list对象
		List<Cart> casrtCookieLsit = JSON.parseArray(cartListString, Cart.class);

		if ("anonymousUser".equals(name)) {// 如果没有登录
			System.out.println("从cookie中获取");
			return casrtCookieLsit;
		} else {// 如果用户已经登录就从redis中获取用户数据

			List<Cart> cartRedisList = cartService.findCartListFromRedis(name);
			// 判断已经登录就去将cookie中的购物车和redis中的购物车合并
			if (casrtCookieLsit.size() > 0) {// 如果cookie中有购物车
				// 调用方法完成合并
				cartRedisList = cartService.mergeCartList(cartRedisList, casrtCookieLsit);
				System.out.println("已完成合并");
				cartService.saveCartListToRedis(name, cartRedisList);
				CookieUtil.deleteCookie(request, response, "cartList");
			}

			System.out.println(cartRedisList + "redis中获取");
			return cartRedisList;
		}

	}

	@RequestMapping("/addGoodsToCartList")
	@CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
	public Result addGoodsToCartList(Long itemId, Integer num) {
		//System.out.println(itemId+"-----------"+num);
		//response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
		//response.setHeader("Access-Control-Allow-Credentials", "true");
		try {
			// 获取当前登录的用户名
			String name = SecurityContextHolder.getContext().getAuthentication().getName();
			// 判断用户是否已经登录
			List<Cart> cartList = findCartList();
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			//System.out.println(cartList+"cookie--------------");
			if ("anonymousUser".equals(name)) {// 如果没有登录就存入cookie

				// System.out.println(cartList);
				CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");// 更新后的购物车存入cookie

			} else {// 如果已经登录就存入redis
				cartService.saveCartListToRedis(name, cartList);

			}
			return new Result(true, "成功添加到购物车");
		} catch (RuntimeException e) {

			return new Result(false, e.getMessage());
		} catch (Exception e) {
			// TODO: handle exception
			return new Result(false, "添加到购物车失败");
		}

	}
}
