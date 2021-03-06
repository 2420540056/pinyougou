package com.pinyougou.page.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;


import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;
@Service
public class ItemPageServiceImpl implements ItemPageService {
	
	@Value("${pagedir}")
	private String pagedir;
	@Autowired
	private FreeMarkerConfig freeMarkerConfig;
	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbItemMapper itemMapper;
	@Override
	public boolean genItemHtml(Long goodsId) {

		try {
			Configuration configuration = freeMarkerConfig.getConfiguration();

			Template template = configuration.getTemplate("item.ftl");
			Map dataModel = new HashMap();
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
			dataModel.put("goods", tbGoods);
			TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
			//3.商品分类
			String itemCat1 =
			itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id()).getName();
			String itemCat2 =
			itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id()).getName();
			String itemCat3 =
			itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName();
			dataModel.put("goodsDesc", goodsDesc);
			dataModel.put("itemCat1",itemCat1);
			dataModel.put("itemCat2",itemCat2);
			dataModel.put("itemCat3",itemCat3);
			
			//4.SKU 列表
			TbItemExample example=new TbItemExample();
			Criteria criteria = example.createCriteria();
			criteria.andStatusEqualTo("1");//状态为有效
			
			criteria.andGoodsIdEqualTo(goodsId);//指定 SPU ID
			example.setOrderByClause("is_default desc");//按照状态降序，保证第一个为默认
			List<TbItem> itemList = itemMapper.selectByExample(example); 
			dataModel.put("itemList", itemList);
			
			Writer out = new FileWriter(pagedir + goodsId + ".html");
			template.process(dataModel, out);
			out.close();
			return true;
		} catch (Exception e) {

			e.printStackTrace();
			return false;
		}

	}
	@Override
	public boolean deleteItemHtml(Long[] goodsIds) {
		try {
				for(Long goodsId:goodsIds){
					
				new File(pagedir+goodsId+".html").delete();
				
				}
			return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			} 
	}

}
