//控制层 
app.controller('userController', function($scope, $controller, userService) {

	$controller('baseController', {
		$scope : $scope
	});// 继承

	// 注册
	$scope.reg = function() {
		if ($scope.password != $scope.entity.password) {
			alert("两次输入的密码不一致，请重新输入")
			$scope.password = "";
			$scope.entity.password = "";
			return;
		}
		/*alert($scope.smsCode+"AAAAAAAAAAA")*/
		userService.add($scope.smsCode,$scope.entity).success(

		function(respones) {
			//alert("VVVVVVVVVVV")
			alert(respones.mssage)
		});
	}

	$scope.sendCode = function() {
		if ($scope.entity.phone == null || $scope.entity.phone == "") {
			alert("请输入手机号")
			return;
		}
		userService.sendCode($scope.entity.phone).success(function(respones) {
			alert(respones.mssage)
		});
	}
});
