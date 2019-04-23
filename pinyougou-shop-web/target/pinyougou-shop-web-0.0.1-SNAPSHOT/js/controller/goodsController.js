 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,typeTemplateService,itemCatService ,uploadService ,goodsService){	

	
	$controller('baseController',{$scope:$scope});//继承
	
	$scope.entity={goodsDesc:{itemImages:[],specificationItems:[]}};//定义页面实体结构
	
	$scope.updateSpecAttribute=function($event,name,value){
		var object=$scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems ,'attributeName', name);
		if(object!=null){//判断当前的specification_items是否与有值
			if($event.target.checked ){//判断当前复选框是否选中
				object.attributeValue.push(value); //如果已经选中就将复选框的value属性加入到specification_items中的attributeValue集合中
			}else{//如果没有勾选就删除specification_items中的attributeValue集合中的value属性
				object.attributeValue.splice( object.attributeValue.indexOf(value ) ,1);//移除选项
				//如果选项都取消了，将specification_items记录移除
				if(object.attributeValue.length==0){
				
					$scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1);
					
				} 
			}
		}else{//如果当前的specification_items没有值
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
		
	}
	//创建 SKU 列表
	$scope.createItemList=function(){
		$scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' } ];//设置默认值
		//[{"attributeName":"网络制式","attributeValue":["移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["5.5寸","4.5寸"]}]
			var items= $scope.entity.goodsDesc.specificationItems;
			for(var i=0;i< items.length;i++){
				
				$scope.entity.itemList = addColumn( $scope.entity.itemList,items[i].attributeName,items[i].attributeValue );
				
			} 
	}
	
	
	//添加列值
	addColumn=function(list,columnName,conlumnValues){//克隆集合
		var newList=[];//新的集合用于存入克隆后的值
		for(var i=0;i<list.length;i++){//遍历需要克隆的属性的集合
			var oldRow= list[i];
			for(var j=0;j<conlumnValues.length;j++){
				var newRow= JSON.parse( JSON.stringify( oldRow ));//深克隆
				newRow.spec[columnName]=conlumnValues[j];
				newList.push(newRow);
			}
	}
		return newList;
}
	
	//读取一级分类
	$scope.selectItemCat1List=function(){
		itemCatService.findByParentId(0).success(
				function(response){
						$scope.itemCat1List=response;
				}
		);
	}
	
	//读取二级分类
	$scope.$watch('entity.goods.category1Id', function(newValue, oldValue) {
		//根据选择的值，查询二级分类
		itemCatService.findByParentId(newValue).success(
			function(response){
				$scope.itemCat2List=response;
			}
		);
	});
	//读取三级分类
	$scope.$watch('entity.goods.category2Id', function(newValue, oldValue) {
		//根据选择的值，查询二级分类
		itemCatService.findByParentId(newValue).success(
			function(response){
				$scope.itemCat3List=response;
			}
		);
	});
	//读取模板id
	$scope.$watch('entity.goods.category3Id', function(newValue, oldValue) {
		itemCatService.findOne(newValue).success(
				function(response){
					$scope.entity.goods.typeTemplateId=response.typeId; //更新模板ID
				}
		)
	});
	//模板 ID 选择后 更新品牌列表 //查询规格列表
	$scope.$watch('entity.goods.typeTemplateId', function(newValue, oldValue) {
		typeTemplateService.findOne(newValue).success(
			function(response){
				$scope.typeTemplate=response;//获取类型模板
				$scope.typeTemplate.brandIds=
				JSON.parse( $scope.typeTemplate.brandIds);//品牌列表
				if($location.search()['id']==null){
					$scope.entity.goodsDesc.customAttributeItems=JSON.parse( $scope.typeTemplate.customAttributeItems);//扩展属性
				}
			}
		);
		
		//查询规格列表
		typeTemplateService.findSpecList(newValue).success(
			function(response){
				$scope.specList=response;
			}
		);
	});
	
	//根据规格名称和选项名称返回是否被勾选
	$scope.checkAttributeValue=function(specName,optionName){
		var items= $scope.entity.goodsDesc.specificationItems;
		var object= $scope.searchObjectByKey(items,'attributeName',specName);
		if(object==null){
				return false;
		}else{
				if(object.attributeValue.indexOf(optionName)>=0){
					return true;
				}else{
					return false;
		}
	} 
}
	/**
	* 上传图片
	*/
	$scope.uploadFile=function(){
		uploadService.uploadFile().success(function(response) {
				if(response.success){//如果上传成功，取出 url
					$scope.image_entity.url=response.mssage;//设置文件地址
				
				}else{
					alert(response.message);
				}
			}).error(function() {
			alert("上传发生错误");
			});
		}
		
		
		
			//添加图片列表
			$scope.add_image_entity=function(){
				$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
					}
			
			//列表中移除图片
		$scope.remove_image_entity=function(index){
			$scope.entity.goodsDesc.itemImages.splice(index,1);
			
			}
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		var id= $location.search()['id'];//获取参数值
		if(id==null){
			return ;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;	
				//向富文本编辑器添加商品介绍
				editor.html($scope.entity.goodsDesc.introduction);
				
				//显示图片列表
				$scope.entity.goodsDesc.itemImages=
				JSON.parse($scope.entity.goodsDesc.itemImages);
				
				// 显示扩展属性
				$scope.entity.goodsDesc.customAttributeItems=
				JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//规格
				alert($scope.entity.goodsDesc.specificationItems)
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
				
				//SKU 列表规格列转换 
				for( var i=0;i<$scope.entity.itemList.length;i++ ){
					$scope.entity.itemList[i].spec = JSON.parse( $scope.entity.itemList[i].spec);
				} 
			}
		);				
	}
	
	//保存 
	$scope.save=function(){		
		alert(editor.html())
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			
			serviceObject=goodsService.update($scope.entity ); //修改  
		}else{
		
			serviceObject=goodsService.add($scope.entity);
			
		}
		
		serviceObject.success(
			function(response){
				if(response.success){
					location.href="goods.html";//跳转到商品列表页
					alert("保存成功");
					$scope.entity={};
					editor.html('');//清空富文本编辑器
					//重新查询 
		        	$scope.reloadList();//重新加载
		        	
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	$scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态
	
	$scope.itemCatList=[];//商品分类列表
	
	$scope.findItemCatList=function(){ 
		itemCatService.findAll().success(
				function(resonpes){
					for(var i=0;i<resonpes.length;i++){
						$scope.itemCatList[resonpes[i].id]=resonpes[i].name
						
					}
				}
				
		
		);
		
	}
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	$scope.updatemarketables=["已下架","已上架"]
	/*$scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态
*/	//更改商品状态
	$scope.updatemarketable=function(marketable){
		goodsService.updatemarketable($scope.selectIds,marketable).success(
		function(response){
			
			if(response.success){//成功
				$scope.reloadList();//刷新列表
				$scope.selectIds=[];//清空 ID 集合
			}else{
				alert(response.message);
			}
		}
		);
}
    
});	
