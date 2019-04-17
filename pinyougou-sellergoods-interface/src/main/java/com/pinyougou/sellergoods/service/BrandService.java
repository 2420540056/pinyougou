package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

public interface BrandService {
	
	public List<TbBrand> findAll();
	
	public PageResult findPage(int pagenum,int pagesize);
	
	public void add(TbBrand tbBrand);
	
	public TbBrand findOne(Long id);
	
	public void update(TbBrand tbBrand);
	
	public void delete(Long[] id);
	
	public PageResult findPage(TbBrand tbBrand, int pagenum,int pagesize);
	
	/**
	* 品牌下拉框数据
	*/
	List<Map> selectOptionList();
	
}
