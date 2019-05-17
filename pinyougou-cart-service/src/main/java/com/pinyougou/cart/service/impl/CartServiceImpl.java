package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.activemq.filter.function.regexMatchFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private TbItemMapper tbItemMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;

	/*
	 * (non-Javadoc)
	 * 
	 * 添加商品到购物车
	 * 
	 * @see
	 * com.pinyougou.cart.service.CartService#addGoodsToCartList(java.util.List,
	 * java.lang.Long, java.lang.Integer)
	 */
	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		// 1.根据商品 SKU ID 查询 SKU 商品信息
		TbItem item = tbItemMapper.selectByPrimaryKey(itemId);
		if(item==null){
			throw new RuntimeException("商品不存在");
		}
		if(!item.getStatus().equals("1")){
			throw new RuntimeException("商品状态不合法");
		}	
		// 2.获取商家 ID
		String sellerId = item.getSellerId();
		// 3.根据商家 ID 判断购物车列表中是否存在该商家的购物车
		Cart cart = searchCartBySellerId(cartList, sellerId);
		// 4.如果购物车列表中不存在该商家的购物车
		if (cart == null) {
			// 4.1 新建购物车对象
			cart = new Cart();
			cart.setSellerId(sellerId);
			cart.setSellerName(item.getSeller());
			TbOrderItem tbOrderItem = createOrderItem(item, num);
			List orderItemList = new ArrayList<>();
			orderItemList.add(tbOrderItem);
			cart.setOrderItemList(orderItemList);
			// 4.2 将新建的购物车对象添加到购物车列表
			cartList.add(cart);
		} else {// 5.如果购物车列表中存在该商家的购物车

			// 查询购物车明细列表中是否存在该商品
			TbOrderItem tbOrderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
			if (tbOrderItem == null) {// 5.1. 如果没有，新增购物车明细
				tbOrderItem = createOrderItem(item, num);
				cart.getOrderItemList().add(tbOrderItem);
			} else {// 5.2. 如果有，在原购物车明细上添加数量，更改金额
				tbOrderItem.setNum(tbOrderItem.getNum() + num);
				tbOrderItem.setTotalFee(new BigDecimal(tbOrderItem.getNum() * tbOrderItem.getPrice().doubleValue()));// 更新价格

				if (tbOrderItem.getNum() <= 0) {
					cart.getOrderItemList().remove(tbOrderItem);// 如果操作后数量小于0 就移除该元素
				}
				// 如果移除后cart明细的数量为0 页移除cart
				if (cart.getOrderItemList().size() == 0) {
					cartList.remove(cart);

				}

			}

		}

		return cartList;
	}

	/**
	 * 根据商家id判断购物车是否有该商家订单
	 * 
	 * @param cartList
	 * @param sellerId
	 */
	private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
		for (Cart cart : cartList) {
			if (cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;

	}

	/**
	 * 创建订单明细
	 * 
	 * @param item
	 * @param num
	 */
	private TbOrderItem createOrderItem(TbItem item, Integer num) {
		if (num <= 0) {

			throw new RuntimeException("数据不合法");
		}
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setNum(num);
		orderItem.setItemId(item.getId());
		orderItem.setPrice(item.getPrice());
		orderItem.setTitle(item.getTitle());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setPicPath(item.getImage());
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
		return orderItem;
	}

	/**
	 * 判断购物车明细列表中是否存在该商品
	 * 
	 * @param list
	 * @param itemId
	 * @return
	 */
	private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> list, Long itemId) {
		for (TbOrderItem tbOrderItem : list) {
			if (tbOrderItem.getItemId().longValue() == itemId.longValue()) {
				return tbOrderItem;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * 从redis中获取用户得购物车
	 * @see com.pinyougou.cart.service.CartService#findCartListFromRedis(java.lang.String)
	 */
	@Override
	public List<Cart> findCartListFromRedis(String username) {
		System.out.println("从redis中获取用户购物车"+username);
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		System.out.println(cartList+"cartlsit");
		if(cartList==null) {
			cartList=new ArrayList<>();//如果redis中没有就给他一个空集合
		}
		//System.out.println(cartList+"AAAAAAAAAA");
		return cartList;
	}

	/* (non-Javadoc)
	 * 将用户的购物车存入redis
	 * @see com.pinyougou.cart.service.CartService#saveCartListToRedis(java.lang.String, java.util.List)
	 */
	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		//System.out.println(redisTemplate+"redistemplate");
		//System.out.println("存入redis"+cartList+"----"+username);
		redisTemplate.boundHashOps("cartList").put(username,cartList);
		
		
		
	}

	/**
	 * 合并购物车
	 * @param cartList1
	 * @param cartList2
	 * @return
	 */
	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
	for (Cart cart : cartList2) {
		List<TbOrderItem> orderItemList = cart.getOrderItemList();
		for (TbOrderItem tbOrderItem : orderItemList) {
			addGoodsToCartList(cartList1, tbOrderItem.getItemId(), tbOrderItem.getNum());
			
		}
	}
		return cartList1;
	}

}