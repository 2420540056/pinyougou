package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.common.utils.IdWorker;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;

import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	@Autowired
	private IdWorker idWorker;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		/* (non-Javadoc)
		 * 提交订单
		 * @see com.pinyougou.seckill.service.SeckillOrderService#submitOrder(java.lang.Long, java.lang.String)
		 */
		@Override
		public void submitOrder(Long seckillId, String userId) {
			 TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
			
			if(seckillGoods==null) {
				throw new RuntimeException("商品不存在");
			}
			if(seckillGoods.getStockCount()<=0) {
				throw new RuntimeException("商品已被抢光");
			}
			seckillGoods.setStockCount(seckillGoods.getStockCount()-1);//库存减一
			redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);//存入redis
			
			if(seckillGoods.getStockCount()==0) {//库存没有
				seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);//更新数据 存入数据库
				redisTemplate.boundHashOps("seckillGoods").delete(seckillId);//清空缓存
			}
			
			//保存订单
			TbSeckillOrder tbSeckillOrder=new TbSeckillOrder();
			tbSeckillOrder.setId(idWorker.nextId());//存入id
			tbSeckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价格
			tbSeckillOrder.setCreateTime(new Date());//订单创建时间
			tbSeckillOrder.setSeckillId(seckillId);
			tbSeckillOrder.setSellerId(seckillGoods.getSellerId());//商家id
			tbSeckillOrder.setUserId(userId);//设置用户id
			tbSeckillOrder.setStatus("0");//设置支付状态
			redisTemplate.boundHashOps("seckillOrder").put(userId, tbSeckillOrder);//存入redis缓存
		}

		/* (non-Javadoc)
		 * 从缓存中获取订单信息
		 * @see com.pinyougou.seckill.service.SeckillOrderService#searchOrderFromRedisByUserId(java.lang.String)
		 */
		@Override
		public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
			
			return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		}

		/* (non-Javadoc)
		 * 修改订单状态
		 * @see com.pinyougou.seckill.service.SeckillOrderService#saveOrderFromRedisToDb(java.lang.String, java.lang.Long, java.lang.String)
		 */
		@Override
		public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
			System.out.println(userId+orderId+transactionId+"AAAAAAAAA"+"保存订单");
			TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
			System.out.println(seckillOrder+"seckillOrder");
			if(seckillOrder==null) {
				 System.out.println("订单不存在");
				 throw new RuntimeException("订单不存在");
				
			}
			if(seckillOrder.getId().longValue()!=orderId.longValue()) {
				 System.out.println("订单号错误");
				throw new RuntimeException("订单号错误");
			}
			seckillOrder.setStatus("1");//设置支付状态
			seckillOrder.setPayTime(new Date());//设置支付日期
			seckillOrder.setTransactionId(transactionId);//设置交易流水号
			//保存到数据库
			seckillOrderMapper.insert(seckillOrder);
			System.out.println("清空redis缓存");
			//清空redis缓存
			redisTemplate.boundHashOps("seckillOrder").delete(userId);
			
		}

		/* (non-Javadoc)
		 * 超时处理
		 * @see com.pinyougou.seckill.service.SeckillOrderService#deleteOrderFromRedis(java.lang.String, java.lang.Long)
		 */
		@Override
		public void deleteOrderFromRedis(String userId, Long orderId) {
			TbSeckillOrder seckillOrder = searchOrderFromRedisByUserId(userId);//从缓存中获取订单
			System.out.println("从缓存中获取订单:"+seckillOrder);
			if(seckillOrder!=null) {//&&seckillOrder.getId().longValue()==orderId.longValue()  
				//2.删除缓存中的订单 
				redisTemplate.boundHashOps("seckillOrder").delete(userId); 			
				//3.库存回退
				TbSeckillGoods  seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
				if(seckillGoods!=null){ //如果不为空
					seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
					redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
					
				}else{
					seckillGoods=new TbSeckillGoods();
					seckillGoods.setId(seckillOrder.getSeckillId());
					//属性要设置。。。。省略
					seckillGoods.setStockCount(1);//数量为1
					redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
				}			
				
				System.out.println("订单取消："+orderId);
			}
			
		}
	
}
