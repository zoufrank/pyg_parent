app.controller("seckillGoodsController",function ($scope,$location,$interval,seckillGoodsService) {
    $scope.findList=function () {
        seckillGoodsService.findList().success(
            function (response) {
                $scope.list=response;
            }
        )
    }
    $scope.findOneFromRedis=function () {
        seckillGoodsService.findOneFromRedis($location.search()['id']).success(
            function (response) {
                $scope.entity=response;
               allseccond =  Math.floor((new Date($scope.entity.endTime).getTime()-new Date().getTime())/1000);
               var time = $interval(function () {
                   if (allseccond>0){
                       allseccond = allseccond -1;
                       $scope.timeString=convertTimeString(allseccond);

                   }else{
                       $interval.cancel(time);
                   }
               },1000)
            }
        )
    }
    convertTimeString=function(allseccond){
        var days= Math.floor(allseccond/60/60/24);
        var hours= Math.floor( (allseccond-days*60*60*24)/(60*60));
        var minutes = Math.floor((allseccond -days*60*60*24 - hours*60*60)/60  );
        var seconds= allseccond -days*60*60*24 - hours*60*60 -minutes*60; //秒数
        var timeString="";

        if(days>0){
            timeString=days+"天 ";
        }

        return timeString+hours+":"+minutes+":"+seconds;
    }
    $scope.submitOrder=function () {
        seckillGoodsService.submit($scope.entity.id).success(
            function (response) {
                if(response.success){
                    alert("下单成功，请在1分钟内完成支付");
                    location.href="pay.html";
                }else {
                    alert(response.message);
                }
            }
        )
    }
})