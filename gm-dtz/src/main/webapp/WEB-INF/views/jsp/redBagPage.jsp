<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE HTML>

<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() == 80 ? "" : (":" + request.getServerPort())) + request.getContextPath();
%>
<html>
<head>
    <meta charset="utf-8">
    <title>快乐打筒子</title>
    <%--<meta content="initial-scale=1, user-scalable=no, minimum-scale=1, maximum-scale=1.0" name="viewport">--%>
    <%--<meta name="format-detection" content="telephone=no">--%>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
    <meta http-equiv="expires" content="0">
    <style>
        html, body, h2, p {
            margin: 0;
            padding: 0;
        }

        html {
            width: 100%;
            height: 100%;
        }

        body {
            width: 100%;
            height: 100%;
            position: relative;
            margin: 0;
            padding: 0;
            -webkit-tap-highlight-color: rgba(0, 0, 0, .9);
            -webkit-user-select: none;
            background-color: #FFF;
            -webkit-tap-highlight-color: transparent;
        }

        .container {
            width: 100%;
            height: auto;
            position: relative;
            margin: 0 auto;
            z-index: 1;
        }

        .hajuBody {
            width: 640px;
            height: auto;
            position: relative;
            margin: 0 auto;
            padding-top: 0;
        }

        .item {
            width: 640px;
            height: 300px;
            position: relative;
            background-repeat: no-repeat;
        }

        .item1 {
            background: url(http://www.0931mj.com/pdklogin/share/res/dn.png?v=2);
            z-index: 2;
        }

        .item2 {
            background: url(http://www.0931mj.com/pdklogin/share/res/dtz.png?v=2);
            z-index: 3;
        }

        .item3 {
            background: url(http://www.0931mj.com/pdklogin/share/res/pdk.png?v=2);
            z-index: 4;
        }

        .item4 {
            background: url(http://www.0931mj.com/pdklogin/share/res/phz.png?v=2);
            z-index: 5;
        }

        .item5 {
            background: url(http://www.0931mj.com/pdklogin/share/res/sdb.png);
            z-index: 6;
        }

        .item6 {
            background: url(http://www.0931mj.com/pdklogin/share/res/ddz.png);
            z-index: 7;
        }

        .itemBtn {
            width: 340px;
            height: 145px;
            position: absolute;
            background-repeat: no-repeat;
            left: 113px;
            bottom: 110px;

        }

        .itemBtn1 {
            width: 340px;
            height: 300px;
            position: absolute;
            background-repeat: no-repeat;
            left: 215px;
            bottom: 333px;
        }

        .downloadBtn {
            background: url(http://dtztest.login.52nmw.cn/pdklogin/images/lingqu.png?v=3) no-repeat;
        }

        .downloadBtn1 {
            /* background: url(http://dtztest.login.52nmw.cn/pdklogin/images/lingqu.png?v=3) no-repeat;*/
        }

        .hajuFooter {
            width: 100%;
            height: 30px;
            font: 13px 微软雅黑, Helvetica, sans-serif;
            background-color: #323333;
            color: #FFF;
            text-align: center;
            line-height: 30px;
            z-index: 10;
            position: fixed;
            bottom: 0;
        }

        a {
            text-decoration: none;
            color: #fff;
        }

        a:hover {
            cursor: pointer;
            outline: none;
            text-decoration: underline;
        }

        .hujuCode {
            width: 640px;
            margin: 0 auto;
            position: relative;
            font: 20px 微软雅黑, Helvetica, sans-serif;
            background-color: #323333;
            color: #FFF;
            padding-bottom: 60px;
            height: 80px;
            line-height: 26px;
            padding-top: 8px;
        }

        .hujuCode p {
            margin-left: 160px;
            margin-top: 20px;
        }

        .tipMask {
            width: 640px;
            height: 1008px;
            background: url(http://www.0931mj.com/pdklogin/share/res/tipMask_ad.png?v=3) no-repeat;
            z-index: 30;
            display: none;
            position: fixed;
            left: 50%;
            margin-left: -320px;
            top: 0;
            z-index: 11;
        }
    </style>

    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
</head>
<body>
<div id="container" class="container">
    <div class="hajuBody">
        <div>
            <img src="http://dtztest.login.52nmw.cn/pdklogin/images/beijing.png">
            <div id="xgniuniu-btn" class="itemBtn downloadBtn" style="opacity: 1;width: 500px;"></div>
            <p class="itemBtn1 downloadBtn1" style="opacity: 1;width: 100px;height: 100px;font-size:6.5ex;">
                ￥${requestScope.sendAccRedBagNum}
            </p>
        </div>
        <div id="tipMask" class="tipMask"></div>
    </div>
    <input type="hidden" id="appid" value="${requestScope.appid}"/>
    <input type="hidden" id="openId" value="${requestScope.openId}"/>
    <input type="hidden" id="unionId" value="${requestScope.unionId}"/>
    <input type="hidden" id="resultMsg" value="${requestScope.resultMsg}"/>
    <input type="hidden" id="resultCode" value="${requestScope.resultCode}"/>
</div>
<div id="hajuFooter" class="hajuFooter" style="font: 26px/60px 微软雅黑,Helvetica,sans-serif; height: 60px;">
    公众号：快乐玩游戏中心
</div>
<script type="text/javascript">
    $(document).ready(function () {

        var resultCode = $("#resultCode").val();
        var resultMsg = $("#resultMsg").val();
        if (resultCode == '-1') {
            // 错误消息
            alert("提示：" + resultMsg);
            closeWx();
            return;
        }
        var appid = $("#appid").val();
        if (resultCode == '1') {
            //调起微信授权
            var redirect_uri = window.location.href;
            // alert("appid = " + appid + ",redirect_uri = " + redirect_uri);
            var authUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appid + "&redirect_uri=" + encodeURIComponent(redirect_uri) + "&response_type=code&scope=snsapi_userinfo#wechat_redirect";
            window.location = authUrl;
            return;
        }

        if (resultCode == '2') {
            var openId = $("#openId").val();
            var unionId = $("#unionId").val();
            if (openId == '' || unionId == '') {
                alert("参数错误：[" + openId + "," + unionId + "]");
                closeWx();
                return;
            }
            $("#xgniuniu-btn").click(function () {
                $.ajax({
                    url: "<%=basePath%>/redBag",
                    type: "POST",
                    data: {
                        openId: openId,
                        unionId: unionId,
                    },
                    async: false,
                    success: function (res) {
                        alert(res);
                        closeWx();
                    },
                    error: function (error) {
                        alert(error);
                        closeWx();
                    }
                });
            });
        }

        function closeWx() {
            setTimeout(function () {
                //这个可以关闭安卓系统的手机
                document.addEventListener('WeixinJSBridgeReady', function () {
                    WeixinJSBridge.call('closeWindow');
                }, false);
                //这个可以关闭ios系统的手机
                WeixinJSBridge.call('closeWindow');
            }, 200)
        }
    });
</script>
</body>
</html>

