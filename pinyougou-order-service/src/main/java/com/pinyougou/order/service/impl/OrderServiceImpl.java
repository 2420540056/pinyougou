package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import com.pinyougou.common.utils.IdWorker;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;
import com.pinyougou.order.service.OrderService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */


@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	@Autowired
	private IdWorker IdWorker;
	
	@Autowired
	private RedisTemplate< String,Object> redisTemplate;
	
	@Autowired
	private TbOrderItemMapper tbOrderItemMapper;
	
	@Autowired
	private TbPayLogMapper paylogMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		System.out.println(order.getUserId()+"order-------------");
		
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());//从缓存中获取购物车数据
		List<String> orderIdList=new ArrayList<>();//订单id列表
		double total_money=0;//总金额
		System.out.println("cartList:============"+cartList);
		for (Cart cart : cartList) {
			long orderId = IdWorker.nextId();
			TbOrder tbOrder=new TbOrder();
			tbOrder.setOrderId(orderId);//存入id
			tbOrder.setUserId(order.getUserId());//存入用户id
			tbOrder.setPaymentType(order.getPaymentType());//支付类型
			tbOrder.setStatus("1");//状态 1未付款
			tbOrder.setSellerId(cart.getSellerId());//商家id
			tbOrder.setCreateTime(new Date());//订单创建日期
			tbOrder.setUpdateTime(new Date());//订单更新日期
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());//收件地址
			tbOrder.setReceiverMobile(order.getReceiverMobile());//收件人手机号码
			tbOrder.setReceiver(order.getReceiver());//收件人名字
			tbOrder.setSourceType(order.getSourceType());//订单来源
			double money =0;
			orderIdList.add(orderId+"");
			total_money+=money;
			List<TbOrderItem> orderItems = cart.getOrderItemList();
			for (TbOrderItem tbOrderItem : orderItems) {
				long id = IdWorker.nextId();
				tbOrderItem.setId(id);
				tbOrderItem.setOrderId(orderId);//订单id
				tbOrderItem.setSellerId(cart.getSellerId());//商家id
				money+=tbOrderItem.getTotalFee().doubleValue();//金额累加
				tbOrderItemMapper.insert(tbOrderItem);
			}
			tbOrder.setPayment(new BigDecimal(money));//支付金额
			orderMapper.insert(tbOrder);
		}
		if ("1".equals(order.getPaymentType())) {//如果是微信支付
				TbPayLog payLog=new TbPayLog();
				payLog.setCreateTime(new Date());//创建时间
				payLog.setTotalFee((long)(total_money*100));//总金额
				String outTradeNo = IdWorker.nextId()+"";//支付订单号
				System.out.println("支付订单号"+outTradeNo);
				payLog.setOutTradeNo(outTradeNo);
				payLog.setPayType("1");//支付类型  微信支付
				String ids = orderIdList.toString().replace("[", "").replace("]", "").replace(" ", "");
				payLog.setOrderList(ids);//订单号
				payLog.setTradeState("0");//支付状态
				payLog.setUserId(order.getUserId());//用户id
				paylogMapper.insert(payLog);//添加到数据库
				redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);//存入缓存
				
				
			
		}
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
				
	} 	

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		/* (non-Javadoc)
		 * 从redis中查询支付日志信息
		 * @see com.pinyougou.order.service.OrderService#searchPayLogFromRedis(java.lang.String)
		 */
		@Override
		public TbPayLog searchPayLogFromRedis(String userId) {
			
			return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
		}

		/* (non-Javadoc)
		 * 更新订单状态信息
		 * @see com.pinyougou.order.service.OrderService#updateOrderStatus(java.lang.String, java.lang.String)
		 */
		@Override
		public void updateOrderStatus(String out_trade_no, String transaction_id) {
			/*TbPayLog payLog = paylogMapper.selectByPrimaryKey(out_trade_no);//查询支付日志信息
			System.out.println(payLog+"paylog");
			payLog.setPayTime(new Date());//支付日期
			payLog.setTradeState("1");//设置支付状态
			payLog.setTransactionId(transaction_id);//设置交易号
			paylogMapper.updateByPrimaryKey(payLog);//更新支付日志
			String orderList = payLog.getOrderList();
			//获取订单号数组
			String[] ids = orderList.split(",");
			for (String orderid : ids) {
				TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.parseLong(orderid));
				if(tbOrder!=null) {
					tbOrder.setStatus("2");//改订单状态 已付款
					orderMapper.updateByPrimaryKey(tbOrder);//更新订单信息	
				}
			}
			
			redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());//清空缓存中日志信息	
		}
	*/
			System.out.println(out_trade_no+"out_trade_no");
			//1.修改支付日志的状态及相关字段
			TbPayLog payLog = paylogMapper.selectByPrimaryKey(out_trade_no);
			System.out.println(payLog+"paylog");
			payLog.setPayTime(new Date());//支付时间
			payLog.setTradeState("1");//交易成功
			payLog.setTransactionId(transaction_id);//微信的交易流水号
			
			paylogMapper.updateByPrimaryKey(payLog);//修改
			//2.修改订单表的状态
			String orderList = payLog.getOrderList();// 订单ID 串
			String[] orderIds = orderList.split(",");
			
			for(String orderId:orderIds){
				TbOrder order = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));
				order.setStatus("2");//已付款状态
				order.setPaymentTime(new Date());//支付时间
				orderMapper.updateByPrimaryKey(order);			
			}
			
			//3.清除缓存中的payLog
			redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
			
		}
		
}
