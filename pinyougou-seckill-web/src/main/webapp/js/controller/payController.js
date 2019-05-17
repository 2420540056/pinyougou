app.controller('payController' ,function($scope, $location,payService){ 
// 本地生成二维码
	$scope.createNative=function(){
	payService.createNative().success(
		function(response){
			//alert("AAAAAA"+response.out_trade_no)
			$scope.money= (response.total_fee/100).toFixed(2) ;  // 金额
			$scope.out_trade_no= response.out_trade_no;// 订单号
	// 二维码
		var qr = new QRious({
			element:document.getElementById('qrious'),
			size:250,
			level:'H',
			value:response.code_url
		});
		queryPayStatus(response.out_trade_no);//查询支付状态 
	}
		
	);
} 
	
	// 查询支付状态
	queryPayStatus=function(out_trade_no){
	//	alert("AAAAAAAAA")
		payService.queryPayStatus(out_trade_no).success(
				
				function(response){
					alert(response.mssage)
					if(response.success){
						location.href="paysuccess.html#?money="+$scope.money;
					}else{ 
						if(response.mssage=="支付超时"){
							location.href="pay.html"
							
						}else{
							location.href="payfail.html";
						}
						
					} 
		}
		);
	}
	
	//获取金额
	$scope.getMoney=function(){
		//alert($location.search()['money']+"AAAAAAAA")
		
		return $location.search()['money'];
	}
});