 //控制层 
app.controller('contentController' ,function($scope,$location,contentService){

	$scope.contentList=[];
    $scope.search=function(){
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }
	//获取某一类型的广告列表
	$scope.findByCategoryId=function (categoryId) {
        contentService.findByCategoryId(categoryId).success(
        	function (response) {
                $scope.contentList[categoryId]=response;
            }
		)
    }
});	
