app.controller('indexController',function($scope,loginService){
	
	$scope.showName=function(){
		//alert("AAAAAAAAAA")
		loginService.showName().success(
				function(respones){
					//alert(respones.loginName+"loginName")
					$scope.loginName=respones.loginName
					
				});
	}
	
});