package com.pinyougou.search.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout=3000)
public class ItemSearchServiceImpl implements ItemSearchService {
	
	@Autowired
	private SolrTemplate solrTemplate;
	
	
	private Map  searchlist(Map searchMap) {
		Map map=new HashMap<>();

		
		HighlightQuery query =new SimpleHighlightQuery();
		
		HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");//设置高亮的域
		highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮的前缀
		highlightOptions.setSimplePostfix("</em>");//高亮的后缀
		query.setHighlightOptions(highlightOptions);//设置高亮选项
		
		Criteria criteria =new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);
			List<HighlightEntry<TbItem>> list = highlightPage.getHighlighted();
			for (HighlightEntry<TbItem> highlightEntry : list) {
				TbItem entity = highlightEntry.getEntity();
				
				if(highlightEntry.getHighlights().size()>0&&highlightEntry.getHighlights().get(0).getSnipplets().size()>0) {
					entity.setTitle(highlightEntry.getHighlights().get(0).getSnipplets().get(0));
					//设置显示高亮
				}
			}
			map.put("rows",highlightPage.getContent());
		 
		return map;
	}

	@Override
	public Map<String, Object> search(Map searchMap) {
		Map<String,Object> map=new HashMap<>();
		Map map2 = searchlist(searchMap);
		map.putAll(map2);
		/*
		Query query=new SimpleQuery();
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		ScoredPage<TbItem>  page=solrTemplate.queryForPage(query, TbItem.class);
		map.put("rows",page.getContent());*/
		
		return map;
	}
	 	
	
}
