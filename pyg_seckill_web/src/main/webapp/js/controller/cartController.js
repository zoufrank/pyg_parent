//控制层
app.controller('cartController' ,function($scope,cartService){


    //查询购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                //总金额和总数量的计算
                $scope.total = {'totalNum':0,'totalMoney':0.0};

                for(var i=0;i<$scope.cartList.length;i++){
                      var cart =   $scope.cartList[i];
                      for(var j=0;j<cart.orderItemList.length;j++){
                          var orderItem = cart.orderItemList[j];

                          $scope.total.totalNum += orderItem.num;
                          $scope.total.totalMoney += orderItem.totalFee;

                      }
                }

            }
        );
    }

    //添加商品到购物车
    $scope.addGoodsToCartList=function (itemId,num) {
        cartService.addGoodsToCartList(itemId,num).success(
            function (response) {
                if(response.success){
                    //重新加载
                    $scope.findCartList();
                }else{
                    alert(response.message);
                }
            }
        );
    }
    $scope.findAddressList=function(){
        cartService.findAddressList().success(
            function(response){
                $scope.addressList=response;
                //设置默认地址
                for(var i=0;i< $scope.addressList.length;i++){
                    if($scope.addressList[i].isDefault=='1'){
                        $scope.address=$scope.addressList[i];
                        break;
                    }
                }
            }
        );
    }
    //选择地址
    $scope.selectAddress=function(address){
        $scope.address=address;
    }

//判断是否是当前选中的地址
    $scope.isSelectedAddress=function(address){
        if(address==$scope.address){
            return true;
        }else{
            return false;
        }
    }
    $scope.order={paymentType:'1'};
//选择支付方式
    $scope.selectPayType=function(type){
        $scope.order.paymentType= type;
    }
    $scope.submitOrder=function(){
        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiverMobile=$scope.address.mobile;//手机
        $scope.order.receiver=$scope.address.contact;//联系人
        cartService.submitOrder( $scope.order ).success(
            function(response){
                if(response.success){
                    //页面跳转
                    if($scope.order.paymentType=='1'){//如果是微信支付，跳转到支付页面
                        location.href="pay.html";
                    }else{//如果货到付款，跳转到提示页面
                        location.href="paysuccess.html";
                    }
                }else{
                    alert(response.message);	//也可以跳转到提示页面
                }
            }
        );
    }
});