app.controller("brandController",function ($scope,$controller,brandService) {
    $controller("baseController",{$scope:$scope});
    $scope.findAll=function(){
        brandService.findAll().success(
            function (response) {
                $scope.list=response;
            }
        );
    };


//分页
    $scope.findPage=function(page,rows){
        brandService.findPage(page,rows).success(
            function(response){
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    };
    $scope.save=function(){
        var methodObject;//方法名称
        if($scope.entity.id!=null){//如果有ID
            methodObject=brandService.update($scope.entity)//则执行修改方法
        }else {
            methodObject=brandService.add($scope.entity);
        }
        methodObject.success(
            function(response){
                if(response.success){
                    //重新查询
                    $scope.reloadList();//重新加载
                }else{
                    alert(response.message);
                }
            }
        );
    };
    $scope.findOne=function (id) {
        brandService.findOne(id).success(
            function(response){
                $scope.entity= response;
            }
        )
    };

    $scope.dele=function () {
        brandService.dele($scope.selectIds).success(
            function (response) {
                if(response.success){
                    //重新查询
                    $scope.reloadList();//重新加载
                    $scope.selectIds=[];
                }else{
                    alert(response.message);
                }

            }
        )
    };
    $scope.searchEntity={};
    $scope.search=function (page,rows) {
        brandService.search(page,rows,$scope.searchEntity).success(
            function (response) {
                $scope.paginationConf.totalItems=response.total;//总记录数
                $scope.list=response.rows;//给列表变量赋值
            }
        )
    }

});