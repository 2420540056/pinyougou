//购物车控制层
app.controller('cartController', function($scope, cartService) {
	// 查询购物车列表
	$scope.findCartList = function() {
		cartService.findCartList().success(function(response) {
			$scope.cartList = response;
			$scope.totalValue=cartService.sum($scope.cartList);//求合计数
			
		});
	}
	$scope.addGoodsToCartList = function(itemId,num) {
		cartService.addGoodsToCartList(itemId,num).success(
				function(respones) {
				if(respones.success){
					$scope.findCartList();
				}else{
					alert(respones.mssage)
				}
		});
	}
});