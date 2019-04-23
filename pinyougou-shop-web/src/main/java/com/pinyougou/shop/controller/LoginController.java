package com.pinyougou.shop.controller;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.handler.codec.spdy.SpdySynReplyFrame;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/login")
public class LoginController {
	
/**获取用户登录信息
 * @return 返回当前用户名
 */
@RequestMapping("/name")
public Map login() {
	
	String name = SecurityContextHolder.getContext().getAuthentication().getName();
	System.out.println(name);
	Map map=new HashMap<>();
	map.put("loginName", name);
	
	return map;
	
}
}
