package com.pinyougou.manager.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResoult;
import entity.Result;

@RestController
@RequestMapping("/brand")
public class BrandController {
	@Reference
	private BrandService brandService;

	@RequestMapping("/findAll")
	public List<TbBrand> findAll() {

		return brandService.findAll();
	}
	/**
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResoult findByPage(int pageNum,int pageSize) {
		PageResoult pageResoult = brandService.findPage(pageNum, pageSize);
		return pageResoult;
	}
	
	/**
	 * @param tbBrand 前台传入保存对象
	 * @return 响应保存成功或者失败
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand tbBrand) {
		try {
			brandService.add(tbBrand);
			return new Result(true,"保存成功");
		} catch (Exception e) {
			return new Result(false,"保存失败");
			// TODO: handle exception
		}
}
}
