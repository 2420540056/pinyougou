package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.instrument.classloading.SimpleLoadTimeWeaver;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout = 3000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;
	@Autowired
	private RedisTemplate redisTemplate;

	private Map searchlist(Map searchMap) {
		Map map = new HashMap<>();
		// 构建高亮显示*******************
		HighlightQuery query = new SimpleHighlightQuery();
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");// 设置高亮的域
		highlightOptions.setSimplePrefix("<em style='color:red'>");// 高亮的前缀
		highlightOptions.setSimplePostfix("</em>");// 高亮的后缀
		query.setHighlightOptions(highlightOptions);// 设置高亮选项

		// 关键字查询************************
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);

		// 1.2 按分类筛选
		if (!"".equals(searchMap.get("category"))) {
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		// 1.3 按品牌筛选
		if (!"".equals(searchMap.get("brand"))) {
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}

		if (searchMap.get("spec") != null) {
			Map<String, String> specMap = (Map) searchMap.get("spec");
			for (String key : specMap.keySet()) {
				Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
				FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}

		if (!"".equals(searchMap.get("price"))) {
			String priceStr = (String) searchMap.get("price");// 价格0-500
			String[] price = priceStr.split("-");

			if (!price[0].equals("0")) {// 如果最小值不等于0
				Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);// 查询条件 大于等于最低价格
				FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
			if (!price[1].equals("*")) {// 如果查询条件最大值不是* 就按
				Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);// 查询条件 小于等于最高价格
				FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}

		// 获取分页*********************
		Integer pageNo = (Integer) searchMap.get("pageNo");// 当前页码
		if (pageNo == null) {
			pageNo = 1;// 设置默认值
		}
		Integer pageSize = (Integer) searchMap.get("pageSize");
		if (pageSize == null) {
			pageSize = 20;
		}
		// 获取当前页第一条记录的页码
		query.setOffset((pageNo - 1) * pageSize);// 搜索当前页的起始页页码
		query.setRows(pageSize);// 搜索的每页的记录数
		
		//排序**********************
		String sortValue = (String) searchMap.get("sort");//排序的方式
		String sortField=(String) searchMap.get("sortField");//排序的字段
		if(sortField!=null&&!"".equals(sortField)) {
			if("DESC".equals(sortValue)) {//升序
				Sort sort=new Sort(Sort.Direction.DESC,"item_"+sortField);
				query.addSort(sort);
			}
			if("ASC".equals(sortValue)) {//降序
				Sort sort=new Sort(Sort.Direction.ASC,"item_"+sortField);
				query.addSort(sort);
			}
			
		}
		// 获取高亮结果集******************
		HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);
		List<HighlightEntry<TbItem>> list = highlightPage.getHighlighted();
		for (HighlightEntry<TbItem> highlightEntry : list) {
			TbItem entity = highlightEntry.getEntity();

			if (highlightEntry.getHighlights().size() > 0
					&& highlightEntry.getHighlights().get(0).getSnipplets().size() > 0) {
				entity.setTitle(highlightEntry.getHighlights().get(0).getSnipplets().get(0));
				// 设置显示高亮
			}
		}
		map.put("totalPages", highlightPage.getTotalPages());// 返回总页数
		map.put("total", highlightPage.getTotalElements());// 返回总记录数
		map.put("rows", highlightPage.getContent());

		return map;
	}

	private List<String> searchCategoryList(Map searchMap) {

		List<String> list = new ArrayList<>();
		// 按关键字查询
		Query query = new SimpleQuery();
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		// 设置分组选项
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions);
		// 得到分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);

		// 根据列得到分组的结果集
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");

		// 得到分组的结果集入口
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();

		List<GroupEntry<TbItem>> content = groupEntries.getContent();

		// 得到分组的入口集集合
		for (GroupEntry<TbItem> entry : content) {

			list.add(entry.getGroupValue());
		}

		return list;
	}

	@Override
	public Map<String, Object> search(Map searchMap) {
		Map<String, Object> map = new HashMap<>();
		String keyWordsStr = (String) searchMap.get("keywords");
		searchMap.put("keywords", keyWordsStr.replace(" ", ""));
		/*
		 * System.out.println(searchMap.get("category") + "category");
		 * System.out.println(searchMap.get("brand") + "brand");
		 */
		Map map2 = searchlist(searchMap);

		List<String> list = searchCategoryList(searchMap);// list存入的是品牌名称和规格名称
		/* System.out.println(list+"品牌规格"); */
		if (list.size() > 0) {
			// 按关键字查询
			map.putAll(searchBrandAndSpec(list.get(0)));
		}
		// 3.查询品牌和规格列表
		String categoryName = (String) searchMap.get("category");
		if (!"".equals(categoryName)) {// 如果有分类名称

			map.putAll(searchBrandAndSpec(categoryName));
		} else {// 如果没有分类名称，按照第一个查询
			if (list.size() > 0) {
				map.putAll(searchBrandAndSpec(list.get(0)));
			}
		}
		map.putAll(map2);
		map.put("categoryList", list);

		/*
		 * Query query=new SimpleQuery(); Criteria criteria=new
		 * Criteria("item_keywords").is(searchMap.get("keywords"));
		 * query.addCriteria(criteria); ScoredPage<TbItem>
		 * page=solrTemplate.queryForPage(query, TbItem.class);
		 * map.put("rows",page.getContent());
		 */

		return map;
	}

	private Map searchBrandAndSpec(String category) {
		Map map = new HashMap<>();
		// redisTemplate.boundHashOps("itemCat").put(tbItemCat.getName(),tbItemCat.getTypeId());存入的是规格名称和模板id
		// [{"id":1,"text":"联想"},{"id":3,"text":"三星"}]
		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);// 根据规格名称获取模板id

		/* System.out.println(typeId + "模板id"); */
		if (typeId != null) {
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);// 根据模板id获取品牌列表
			//System.out.println(brandList + "品牌列表");
			map.put("brandList", brandList);
			// List<Map> specs = JSON.parseArray(tbTypeTemplate.getSpecIds(),Map.class);
			// redisTemplate.boundHashOps("specList").put(tbTypeTemplate.getId(),specs);存入的是品牌信息和规格信息与模板id
			List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);// 根据模板id取出规格信息
			 //System.out.println(specList + "规格列表");
			map.put("specList", specList);
		}
		return map;
	}

	/* (non-Javadoc)导入到索引库
	 * @see com.pinyougou.search.service.ItemSearchService#importList(java.util.List)
	 */
	@Override
	public void importList(List list) {
		System.out.println("solr:------"+list);
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
		
	}

	@Override
	public void deleteByGoodsIds(List goodsIdList) {
		System.out.println("删除商品 ID"+goodsIdList);
		Query query=new SimpleQuery(); 
		Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
		
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
		
	}

}
