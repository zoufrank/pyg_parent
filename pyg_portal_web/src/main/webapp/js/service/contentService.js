//服务层
app.service('contentService',function($http){
	    	
	//获取某一类型的广告列表
	this.findByCategoryId=function(categoryId){
		return $http.get('../content/findByCategoryId.do?categoryId='+categoryId);
	}

});
