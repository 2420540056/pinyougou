//购物车控制层
app.controller('cartController', function($scope, cartService) {
	// 查询购物车列表
	$scope.findCartList = function() {
		cartService.findCartList().success(function(response) {
			$scope.cartList = response;
			$scope.totalValue = cartService.sum($scope.cartList);// 求合计数

		});
	}
	$scope.addGoodsToCartList = function(itemId, num) {
		cartService.addGoodsToCartList(itemId, num).success(function(respones) {
			if (respones.success) {
				$scope.findCartList();
			} else {
				alert(respones.mssage)
			}
		});
	}
	// 查询用户地址信息
	$scope.findListByLoginUser = function() {
		cartService.findListByLoginUser().success(function(respones) {
			$scope.addressList = respones;
			for (var i = 0; $scope.addressList.length; i++) {// 显示默认收货地址
				if ($scope.addressList[i].isDefault == '1') {
					$scope.address = $scope.addressList[i];
					break;
				}
			}
		})
	}

	$scope.selectAddress = function(address) {
		$scope.address = address;
	}

	$scope.isSelectedAddress = function(address) {
		if ($scope.address == address) {
			return true;
		} else {
			return false;
		}

	}

	// 保存
	$scope.add = function(entity) {
		cartService.add(entity).success(function(response) {
			if (response.success) {
				// 重新查询
				$scope.findListByLoginUser();
			} else {
				alert(response.message);
			}
		});
	}

	// 选择支付方式
	$scope.order = {
		paymentType : '1'
	}

	$scope.selectPayType = function(type) {
		$scope.order.paymentType = type;
	}
	
	//提交订单
	$scope.submitOrder = function() {

		$scope.order.receiverAreaName = $scope.address.address;// 地址
		$scope.order.receiverMobile = $scope.address.mobile;// 手机
		$scope.order.receiver=$scope.address.contact;//联系人
		cartService.submitOrder($scope.order).success(
				function(respones){
					if(respones.success){
						//页面跳转
						if($scope.order.paymentType=='1'){//如果是微信支付，跳转到支付页面
								location.href="pay.html";
							}else{//如果货到付款，跳转到提示页面
								location.href="paysuccess.html";
						} 
					}else{
						alert(respones.mssage)
					}
					
				}
		)
	}
});