//控制层
app.controller('loginController' ,function($scope,loginService){


    //获取当前你登录名
    $scope.getLoginUser = function () {
        loginService.getLoginUser().success(
            function (response) {
                $scope.username = response.username;
            }
        );
    }
});