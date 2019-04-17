app.controller('brandController',function($scope,$controller,brandService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    	$scope.findAll=function(){
    		brandService.findAll.success(function(response){
    			$scope.list=response;
    		});
    	}
    	
    	
    	//分页
    		$scope.findPage=function(page,size){
    		brandService.findPage(page,size).success(
	    		 function(response){
	    			$scope.list=response.rows;
	    			$scope.paginationConf.totalItems=response.total;//更新总记录数
    		});
    	}
    		
    	
  //新增
	 $scope.save=function(){
	  var object=null;//方法名 根据id是否为空判断调用哪个方法
	  if($scope.entity.id!=null){
		 object=brandService.update($scope.entity)
	  }else{
		  object=brandService.add($scope.entity)
	  }
	 
	  object.success(
			  function(response){
				if(response){
					$scope.reloadList()//刷新
				}else{
					alert(response.message)//弹出错误信息
				}
		});
	}
	  //修改 根据id查询一条数据
	  $scope.findOne=function(id){
		  brandService.findOne(id).success(
			 function(response){
			 	$scope.entity=response
		  });
	  }
	  
	  //删除数据
	 
	  $scope.del=function(){
		  if(confirm("确定要删除吗")){
			  brandService.del($scope.selectIds).success(
				  function(respones){
					  if(respones.success){
						  $scope.reloadList();//刷新列表
					  }
				  }
		  )}
	  }
	  
	  //模糊查询
	  $scope.searchEntity={};
	  
	  $scope.search=function(page,size){
		  brandService.search(page,size,$scope.searchEntity).success(
  		 function(response){
  			$scope.list=response.rows;
  			$scope.paginationConf.totalItems=response.total;//更新总记录数
		});
	}
});