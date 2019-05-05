app.controller('searchController',function($scope,$location,searchService){ 
	//搜索
	$scope.search=function(){
		$scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo)	//将输入的页码转换为int格式
		searchService.search($scope.searchMap).success(
		function(response){
			$scope.resultMap=response;//搜索返回的结果
			/*alert(response.brandList+"brandList")*/
			buildPageLabel()
			
		}
	);
}
	$scope.loadkeywords=function(){
		$scope.searchMap.keywords=$location.search()['keywords'];
		$scope.search();
	}
	//排序
	$scope.sortSearch=function(sort,sortField){
		$scope.searchMap.sort=sort;
		$scope.searchMap.sortField=sortField;
		$scope.search();
	}
	
	buildPageLabel=function(){
		$scope.pageLabel=[]
		var firstPage=1;
		var lastPage=$scope.resultMap.totalPages;
		$scope.firstDot=true;
		$scope.lastDot=true;
		//********[1 2 3 4 5]*********
		if(lastPage>5){
			if($scope.searchMap.pageNo<3){//如果当前页小于3 
				$scope.firstDot=false;
				lastPage=5;
			}else if($scope.searchMap.pageNo>$scope.resultMap.totalPages-2){//如果当前页小于总页码减2 
				$scope.lastDot=false;
				firstPage=$scope.resultMap.totalPages-4;
			}else {
				firstPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2;
			}
			
		}else{
			$scope.firstDot=false;
			$scope.lastDot=false;
		}
		
		for(var i=firstPage;i<=lastPage;i++){
			$scope.pageLabel.push(i)
		}
	}
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true ;
		}else{
			return false ;
		}
	}
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
			return true ;
		}else {
			return false ;
		}
	}
	
	$scope.queryPage=function(pageNo){
		if(pageNo<1||pageNo>$scope.resultMap.totalPages){
			return;
		}
		$scope.searchMap.pageNo=pageNo;//把当前页码赋值给传入后台的pageNo
		$scope.search();//调用后台查询方法
		
	}
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};//搜索对象
	//添加搜索项
	$scope.addSearchItem=function(key,value){
		if(key=='category' || key=='brand' ||key=='price'){//如果点击的是分类或者是品牌
			$scope.searchMap[key]=value;
		}else{
				$scope.searchMap.spec[key]=value;
			}
		$scope.search();//执行搜索
		}
	
	//移除复合搜索条件
	$scope.removeSearchItem=function(key){
		if(key=="category" || key=="brand" ||key=='price'){//如果是分类或品牌
			$scope.searchMap[key]="";
		}else{//否则是规格
			delete $scope.searchMap.spec[key];//移除此属性
		}
		$scope.search();//执行搜索
	}

	$scope.keywordsIsBrand=function(){
		for(var i=0;i<$scope.resultMap.brandList.length;i++){
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
				return true;
			}
		}
		return false;
	}
	
});