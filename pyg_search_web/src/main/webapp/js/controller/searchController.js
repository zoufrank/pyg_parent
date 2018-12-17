app.controller("searchController",function ($scope,$location,searchService) {
    //定义搜索对象
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':20,'sortField':'','sort':''};
    $scope.resultMap={totalPages:''};
    $scope.search=function () {
        $scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo) ;
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap=response;
                buildPageLabel();
            }
        )
    }
    //添加搜索项
    $scope.addSearchItem=function (key,value) {
        if(key == 'category'|| key == 'brand'|| key=='price'){
            $scope.searchMap[key]=value;
        }else{
            $scope.searchMap.spec[key]=value;
        }
        $scope.search();
    }
    //移除搜索项
    $scope.removeSearchItem=function (key) {
        if(key == 'category'|| key == 'brand'|| key=='price'){
            $scope.searchMap[key]='';
        }else{
            delete $scope.searchMap.spec[key];
        }
        $scope.search();//执行搜索
    }
    buildPageLabel=function () {
        $scope.pageLabel=[];//新增分页栏属性
        var maxPageNo=$scope.resultMap.totalPages;//得到最后页码
        var firstPage=1;//开始页码
        var lastPage=maxPageNo;
        $scope.firstDot=true;//前面有点
        $scope.lastDot=true;//后面有点
        if($scope.resultMap.totalPages>5){
            if($scope.searchMap.pageNo<=3){
                lastPage=5;
                $scope.firstDot=false;
            }else if($scope.searchMap.pageNo>=lastPage-2){
                firstPage=maxPageNo-4;
                $scope.lastDot=false;
            }else {
                firstPage=$scope.searchMap.pageNo-2;
                lastPage=$scope.searchMap.pageNo+2
            }
        }else{
            $scope.firstDot=false;
            $scope.lastDot=false;
        }

        for (var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }

    $scope.queryByPage=function (pageNo) {
        if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo= pageNo;
        $scope.search()
    }

    //判断当前页为第一页
    $scope.isTopPage=function(){
        if($scope.searchMap.pageNo==1){
            return true;
        }else{
            return false;
        }
    }

//判断当前页是否未最后一页
    $scope.isEndPage=function(){
        if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else{
            return false;
        }
    }

    $scope.sortSearch=function(sortField,sort){
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;
        $scope.search();
    }

    $scope.keywordsIsBrand=function () {
        for(var i=0;i<$scope.resultMap.brandList.length;i++){
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                return true;
            }
        }
        return false;
    }

    //加载查询字符串
    $scope.loadkeywords=function(){
        $scope.searchMap.keywords=  $location.search()['keywords'];
        $scope.search();
    }



































})