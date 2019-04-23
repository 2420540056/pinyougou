app.service("loginService",function($http){
	this.loginName=function(response){
		
		return $http.get('../login/name.do');
	}
	
});