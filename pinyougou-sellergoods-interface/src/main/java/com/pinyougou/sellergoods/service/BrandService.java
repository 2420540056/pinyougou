package com.pinyougou.sellergoods.service;

import java.util.List;

import com.pinyougou.pojo.TbBrand;

import entity.PageResoult;

public interface BrandService {
	
	public List<TbBrand> findAll();
	
	public PageResoult findPage(int pagenum,int pagesize);
	
	public void add(TbBrand tbBrand);
	
	public TbBrand findOne(Long id);
	
	public void update(TbBrand tbBrand);
	
	public void delete(Long[] id);
	
	public PageResoult findPage(TbBrand tbBrand, int pagenum,int pagesize);
}
