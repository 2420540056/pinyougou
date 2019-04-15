package com.pinyougou.sellergoods.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
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


	/* (non-Javadoc)根据id查询用户
	 * @see com.pinyougou.sellergoods.service.BrandService#findOne(java.lang.Long)
	 */
	@Override
	public TbBrand findOne(Long id) { 
		
		return tb.selectByPrimaryKey(id);
	
	}


	/* (non-Javadoc)根据传入的对象修改用户信息
	 * @see com.pinyougou.sellergoods.service.BrandService#update(com.pinyougou.pojo.TbBrand)
	 */
	@Override
	public void update(TbBrand tbBrand) {
		
		tb.updateByPrimaryKey(tbBrand);
		
	}


	/* (non-Javadoc)
	 * 传入id数组
	 * 根据id删除数据
	 * @see com.pinyougou.sellergoods.service.BrandService#delete(java.lang.Long)
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id: ids) {
			tb.deleteByPrimaryKey(id);
		}
		
	}


	@Override
	public PageResoult findPage(TbBrand tbBrand, int pagenum, int pagesize) {
		 PageHelper.startPage(pagenum, pagesize);//调用分页插件进行分页
		 
		 TbBrandExample example=new TbBrandExample();
		 Criteria criteria = example.createCriteria();
		 if(tbBrand!=null) {
		 if(tbBrand.getName()!=null && tbBrand.getName().length()>0) {
			 criteria.andNameLike("%"+tbBrand.getName()+"%");
			
		 	}
		 if(tbBrand.getFirstChar()!=null&&tbBrand.getFirstChar().length()>0) { 
			 criteria.andFirstCharEqualTo(tbBrand.getFirstChar());
		    }
		 }
		 
		 Page<TbBrand> page = (Page<TbBrand>) tb.selectByExample(example);
		 
		// TODO Auto-generated method stub
		 return new PageResoult(page.getTotal(), page.getResult());
	}

}
