var sys = "android", isLoaded = false, isWeixin = false, $btn = $('#btns>a'), $tip = $('#weixin-tip'),
    $close = $('#close');
var sy_radioPayType = 1;
function sy_post(url, params, succb) {
    $.ajax({
        url: url,
        data: params,
        type: 'post',
        cache: false,
        dataType: 'json',
        success: succb,
        error: function () {
            alert("获取数据失败！");
        }
    });
}
function getQuery(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if (r != null)return unescape(r[2]);
    return null;
}
function onLoadComplete() {
    var ua = navigator.userAgent.toLowerCase();
    if (/iphone|ipad|ipod/.test(ua)) {
        sys = "ios";
    }
    //微信弹提示
    if (/MicroMessenger/i.test(ua)) {
        isWeixin = true;
        $close.click(function () {
            $tip.hide();
        });
        $tip.click(function () {
            $tip.hide();
        });
        $tip.show();
    } else {
        openApp();
    }
}
function onClickOpen() {
    window.location.href = "/";
}
function onClickPay() {
    if (sy_radioPayType == 1) {//为自己充值
        userInfo = mineInfo;
        if (userInfo.userId <= 0) {
            alert("您尚未在游戏中注册，请先登录游戏！");
            return;
        }
        if (userInfo.payBindId <= 0) {//绑定邀请码
            $("#forBind").css("display", "block");
            $("#fade").css("display", "block");
            return;
        }
    } else {//为他人充值
        var url = baseURL + "authorizationAction!getUserInfoById.guajilogin";
        var params = {};
        params.payTime = mineInfo.payTime;
        params.paySign = mineInfo.paySign;
        params.userId = mineInfo.userId;
        params.flatId = mineInfo.flatId;
        params.viewId = $("#pay_user_id").val();
        sy_post(url, params, function (data) {
            console.log("getUserInfoById:" + JSON.stringify(data));
            if (data.hasOwnProperty("code") && data.code == 0) {
                userInfo = data;
                if (userInfo.payBindId <= 0) {
                    alert("该用户未绑定邀请码");
                    return;
                }
                showPayUserInfo();
            } else {
                alert("找不到该玩家！请重新输入！");
            }
        });
        return;
    }
    showPayUserInfo();
}

function showPayUserInfo() {
    if (userInfo.headimgurl)
        $("#pay_user_headimg").attr("src", userInfo.headimgurl);
    $("#user_info_name").html("用户名:" + userInfo.name);
    $("#user_info_id").html("id:" + userInfo.userId);
    $("#user_info_bid").html("邀请码:" + userInfo.payBindId);
    $("#forPay").css("display", "block");
    $("#fade").css("display", "block");
    $("#user_info_pay_val").html(default_pay_cny);
    console.log("onClickPay:" + default_pay_cny);
}

function onClickConfirm(action) {
    if (action == 'pay') {//微信支付
        var url = baseURL + "support!ovali_com.guajilogin";
        var params = {};
        params.flat_id = userInfo.flatId;
        params.openid = mineInfo.openid;
        params.server_id = 1;
        params.p = "weixin";
        params.itemid = 1;
        params.total_fee = default_pay_cny * 100;
        params.payType = "weixin";
        params.k = userInfo.k;
        params.c = "";
        params.visitor = 0;
        params.trade_type = "JSAPI";
        sy_post(url, params, function (data) {
            onClickCancel();
            console.log("ovali_com:" + JSON.stringify(data));
            if (data.hasOwnProperty("code") && data.code == 0) {
                var weixinPayObject = data.url;
                WeixinJSBridge.invoke(
                    'getBrandWCPayRequest', {
                        "appId": weixinPayObject.appId + "",
                        "timeStamp": weixinPayObject.timeStamp + "",
                        "nonceStr": weixinPayObject.nonceStr + "",
                        "package": weixinPayObject.package + "",
                        "signType": "MD5",
                        "paySign": weixinPayObject.paySign + ""
                    },
                    function (res) {
                        if (res.err_msg == "get_brand_wcpay_request:ok") {
                            alert("充值成功");
                        } else if (res.err_msg == "get_brand_wcpay_request:cancel") {
                            alert("充值取消");
                        } else {
                            alert("充值失败");
                        }
                        //console.log("WeixinJSBridge:"+JSON.stringify(res));
                    }
                );
            } else {
                alert("下单失败");
            }
        });
    } else if (action == 'bind') {//绑定邀请码
        var url = baseURL + "qipai!bindPayAgencyId.guajilogin";
        var params = {};
        params.payTime = mineInfo.payTime;
        params.paySign = mineInfo.paySign;
        params.userId = mineInfo.userId;
        params.flatId = mineInfo.flatId;
        params.payBindId = $("#pay_bind_id").val();
        sy_post(url, params, function (data) {
            console.log("bindPayAgencyId:" + JSON.stringify(data));
            if (data.hasOwnProperty("code") && data.code == 0) {
                mineInfo.payBindId = params.payBindId;
                onClickCancel();
                alert("绑定成功，请继续支付");
            } else {
                alert("绑定失败");
            }
        });
    }
}

function onClickPayItem(payItem, seq) {
    default_pay_cny = payItem;
    $("#pay_num").html(default_pay_cny);
    for (var i = 0; i < mineInfo.payItems.length; i++) {
        $("#pay_info_" + (i + 1)).attr("class", "");
    }
    $("#pay_info_" + seq).attr("class", "li_selected");
}

function onClickCancel() {
    $("#pay_user_headimg").attr("src", "http://res168.23yly.com/pdklogin/res/default_m_big.png");
    $("#forPay").css("display", "none");
    $("#forBind").css("display", "none");
    $("#fade").css("display", "none");
}

function onClickRadio(id) {
    $("#input_text1").css("display", "none");
    $("#input_text2").css("display", "none");
    $("#pay_radio_1").attr("checked", false);
    $("#pay_radio_2").attr("checked", false);
    $("#input_text" + id).css("display", "block");
    $("#pay_radio_" + id).attr("checked", 'checked');
    sy_radioPayType = id;
}