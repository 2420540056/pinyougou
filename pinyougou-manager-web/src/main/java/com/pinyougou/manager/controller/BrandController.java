package com.pinyougou.manager.controller;

import java.util.List;

import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.service.EchoService;
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
	/** 根据id查询产品信息
	 * @param id 前台传入id
	 * @return
	 */
	@RequestMapping("/findOne")
	public TbBrand findOne(Long id) {
		TbBrand tbBrand = brandService.findOne(id);
		return tbBrand;
	}
	/**修改信息
	 * @param tbBrand 前台传入
	 * @return 返回修改成功或者失败
	 */
	
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand tbBrand) {
		try {
			brandService.update(tbBrand);
			return new Result(true,"修改成功");
		} catch (Exception e) {
			// TODO: handle exception
		return new Result(false,"修改失败");
		}
	
	}
	/**根据传入的id数组批量删除信息
	 * @param ids 传入的id数组
	 * @return 返回删除信息
	 */
	@RequestMapping("/delete")
	public Result delete(Long[] ids) {
		try {
			brandService.delete(ids);
			return new Result(true ,"删除成功");
		} catch (Exception e) {
			return new Result(false,"删除失败");
			// TODO: handle exception
		}
		
	}

	/**模糊查询
	 * @param tbBrand 传入的查询条件
	 * @param page 当前页码
	 * @param size 每页显示条数
	 * @return
	 */
	@RequestMapping("/search")
	public PageResoult search(@RequestBody TbBrand tbBrand,int page,int size) {
		
		return brandService.findPage(tbBrand,page,size);
   }
}
