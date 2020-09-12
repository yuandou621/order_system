var app = new Vue({
    el: '.center',
    data: {
        // 当前登录用户的用户名, 为空字符串表示未登录状态
        name: "",

        // 当前的菜品列表
        orders: [
        ],

        // 当前订单的详情
        curOrder: null,
    },
    methods: {
        // 判定当前是否登录
        isLogin() {
            if (this.name == "") {
                return false;
            }
            return true;
        },
        // 从服务器检测登录状态
        checkLogin() {
            // 页面最初加载时先访问服务器判定自身的登陆状态. 
            console.log("checkLogin");
            $.ajax({
                url: 'login',
                type: 'get',
                success: function (data, status) {
                    if (data.ok != 1) {
                        return;
                    }
                    // 登录成功后
                    // 1. 设置用户名
                    app.name = data.name;
                    // 2. 获取订单
                    app.showMyOrders();
                }
            });
        },
        // 获取自己的订单
        showMyOrders() {
            console.log("showMyOrders");
            $.ajax({
                url: 'order',
                type: 'get',
                success: function (data, status) {
                    app.orders = data;
                }
            });
        },
        // 获取某个订单的详细信息
        getOrderDetail(orderId) {
            console.log("getOrderDetail");
            $.ajax({
                url: 'order?orderId=' + orderId,
                type: 'get',
                success: function (data, status) {
                    app.curOrder = data;
                }
            });
        },
        // 计算当前订单的价钱, 供界面显示
        curOrderPrice() {
            let total = 0;
            this.curOrder.dishes.forEach((dish) => {
                total += dish.price;
            });
            return total;
        },
        logout() {
            console.log("logout");
            $.ajax({
                type:'get',
                url:'logout',
                success: function (data, status) {
                    if (data.ok != 1) {
                        alert("注销失败!");
                        return;
                    }
                    alert("注销成功!");
                    window.location.href = "login.html";
                }
            })
        }
    },
    computed: {
    }
});