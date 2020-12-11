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
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.2">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
</head>

<body style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<div id="div_help" style="position:fixed;top: 0px;display: none;background-image: url('<%=basePath%>/image/android_h5_help.png');background-repeat: round;background-size:100%;width: 100%;height: 100%;">
</div>
</body>

<script>
    $(document).ready(function () {
        var browser = {
            versions: function () {
                var u = navigator.userAgent, app = navigator.appVersion;
                return {   //移动终端浏览器版本信息
                    trident: u.indexOf('Trident') > -1, //IE内核
                    presto: u.indexOf('Presto') > -1, //opera内核
                    webKit: u.indexOf('AppleWebKit') > -1, //苹果、谷歌内核
                    gecko: u.indexOf('Gecko') > -1 && u.indexOf('KHTML') == -1, //火狐内核
                    mobile: !!u.match(/AppleWebKit.*Mobile.*/), //是否为移动终端
                    ios: !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), //ios终端
                    android: u.indexOf('Android') > -1 || u.indexOf('Linux') > -1, //android终端或uc浏览器
                    iPhone: u.indexOf('iPhone') > -1, //是否为iPhone或者QQHD浏览器
                    iPad: u.indexOf('iPad') > -1, //是否iPad
                    webApp: u.indexOf('Safari') == -1 //是否web应该程序，没有头部与底部
                };
            }(),
            language: (navigator.browserLanguage || navigator.language).toLowerCase()
        }

        if (browser.versions.mobile) {//判断是否是移动设备打开。browser代码在下面
            var ua = navigator.userAgent.toLowerCase();//获取判断用的对象

            if (ua.match(/MicroMessenger/i) == "micromessenger") {
                //在微信中打开

//                $("#div_buycards").css("display","none");
//            $(".header_table").css("display","none");

                var height=document.body.scrollHeight;//-$(".header_table").height();

                $("#div_help").css("height",height);
                if (browser.versions.ios) {
                    //是否在IOS浏览器打开
                    $("#div_help").css("background-image","url('<%=basePath%>/image/ios_h5_help.png')");
                    $("#div_help").css("display","");
                }
                if(browser.versions.android){
                    //是否在安卓浏览器打开
                    $("#div_help").css("background-image","url('<%=basePath%>/image/android_h5_help.png')");
                    $("#div_help").css("display","");
                }

                var timer;
                var handler = function(){
                    clearInterval(timer);

                    var a=confirm("\n已完成付款？");
                    if(a==true){
                        window.location='<%=path%>/page/home?t='+new Date().getTime();
                    }else{
                        window.location = "<%=path%>/page/buy/cards?t="+new Date().getTime();
                    }
                }
                timer = setInterval(handler , 8000);
            }else{
                window.location="${url}";
            }
//            if (ua.match(/WeiBo/i) == "weibo") {
//                //在新浪微博客户端打开
//            }
//            if (ua.match(/QQ/i) == "qq") {
//                //在QQ空间打开
//            }
        } else {
            //否则就是PC浏览器打开
            alert("请在手机浏览器中访问");
        }
    });
</script>
</html>