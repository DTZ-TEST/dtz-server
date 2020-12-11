<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String path = request.getContextPath() + "";
    String basePath = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() == 80 ? "" : (":" + request.getServerPort())) + request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1.0"/>
    <title id="title">快乐玩游戏代理后台_快乐打筒子_快乐跑得快_快乐跑胡子</title>

    <style>

    </style>
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.2">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
</head>

<body style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<table class="header_table" cellpadding="0" cellspacing="0">
    <tr>
        <td class="fix_td" onclick="window.location='<%=path%>/page/buy/cards'"><img class="header_img1" src="<%=basePath%>/image/back.png"/></td>
        <td><img src="<%=basePath%>/image/buy_cards.png" class="header_img2" style="width: 94px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div id="div_buycards" class="table" style="margin-top: 2px;text-align: center;">
    <div id="buycards" style="margin-top:10px;width:100%;font-size:18px;">
        <div  class="table1" style="width: 100%;text-align: center">
            <!--扫描代码-->
            <input type="hidden" name="out_trade_no"  value="${out_trade_no}"/>
                <table style="width: 100%;text-align: center;">
                    <tr>
                        <td>订单号</td>
                        <td>${out_trade_no}</td>
                    </tr>
                    <tr>
                        <td>商品名称</td>
                        <td>${body}</td>
                    </tr>
                    <tr>
                        <td>订单金额</td>
                        <td>${total_fee/100}元</td>
                    </tr>
                    <tr>
                        <td>支付方式</td>
                        <td>微信支付</td>
                    </tr>
                </table>
                <a href = "javascript:void(0);" target="_blank" id="pay" onclick="pay(this)"><img src='<%=basePath%>/image/pay_ok.png' style='width: 153px;height: 50px;border: none;margin-top: 20px;'/></a>
            </div>
        </div>
    </div>
</div>
<div id="div_help" style="position:fixed;top: 0px;display: none;background-image: url('<%=basePath%>/image/android_h5_help.png');background-repeat: round;background-size:100%;width: 100%;height: 100%;">
</div>
</body>

<script>
    var isOk=false;
    function pay(obj) {
        isOk=true;
        var url="${pay_info}";
        obj.href="<%=path%>/noauth/pay/proxy?url="+encodeURIComponent(encodeURIComponent(url));
        <%--window.location="<%=path%>/noauth/pay/proxy?url="+encodeURIComponent(encodeURIComponent(url));--%>
    }

    <%--var timer;--%>
    <%--var maxCount=120;--%>
    <%--$(function(){--%>
        <%--var handler = function(){--%>

            <%--if (!isOk){--%>
                <%--return;--%>
            <%--}--%>

            <%--if(maxCount--<=0){--%>
                <%--clearInterval(timer);--%>
                <%--return;--%>
            <%--}--%>

            <%--var out_trade_no = $('input[name=out_trade_no]').val();--%>
            <%--$.post("<%=basePath%>/pay/payResultQuery?out_trade_no="+out_trade_no,null,function(msg){--%>
                <%--//alert(msg);--%>
                <%--if(msg == '1'){--%>
<%--//                        $('#payResult').text('支付成功');--%>
                    <%--$('#pay').attr('href','#');--%>
                    <%--alert("充值成功！\n" +--%>
                        <%--"（如您未及时收到钻石，请您退出代理后台重新登录即可）");--%>
                    <%--clearInterval(timer);--%>
                    <%--window.location='<%=path%>/page/home';--%>
                <%--}--%>
            <%--});--%>
        <%--}--%>
        <%--timer = setInterval(handler , 5000);--%>
    <%--});--%>
</script>
</html>