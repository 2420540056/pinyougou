package com.pinyougou.sellergoods.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResoult;

@Service
public class BrandServiceImpl implements BrandService {
	@Autowired
	private TbBrandMapper tb;
	@Override
	public List<TbBrand> findAll() {
		// TODO Auto-generated method stub
		
		return tb.selectByExample(null);
	}
	
	
	/* (non-Javadoc)
	 * @see com.pinyougou.sellergoods.service.BrandService#findPage(int, int)
	 * 根据传入的每页显示条数和当前页码进行分页
	 */
	@Override
	public PageResoult findPage(int pagenum, int pagesize) {
		// TODO Auto-generated method stub
		 PageHelper.startPage(pagenum, pagesize);//调用分页插件进行分页
		Page<TbBrand> page = (Page<TbBrand>) tb.selectByExample(null);
		return new PageResoult(page.getTotal(), page.getResult());
	}


	/* (non-Javadoc)
	 * @see com.pinyougou.sellergoods.service.BrandService#add(com.pinyougou.pojo.TbBrand)
	 * 添加用户
	 */
	@Override
	public void add(TbBrand tbBrand) { 
		
		tb.insert(tbBrand);
		
	}

}
