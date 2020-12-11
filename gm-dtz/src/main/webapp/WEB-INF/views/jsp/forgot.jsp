<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+(request.getServerPort()==80?"":(":"+request.getServerPort()))+request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1.0"/>
    <title id="title">快乐玩游戏代理后台_快乐打筒子_快乐跑得快_快乐跑胡子</title>

    <style>
        .icon-input{
            position:absolute;
            /*left:0;*/
            z-index:5;
            /*background-image:url("<%=basePath%>/image/phone.png"); !*引入图片图片*!*/
            background-repeat:round; /*设置图片不重复*/
            background-position:0px 0px; /*图片显示的位置*/
            width:20px; /*设置图片显示的宽*/
            height:20px; /*图片显示的高*/
            margin-top: 8px;
            margin-left: 3px;
        }
        .text-input{
            padding-left:25px;height:30px;width: 70%;color: #0d6cac;font-size: 16px;
        }
    </style>
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.0">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="<%=basePath%>/js/md5.js"></script>
</head>

<body onresize="resize()" style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<table class="header_table" cellpadding="0" cellspacing="0">
    <tr>
        <td class="fix_td" onclick="history.back()"><img class="header_img1" src="<%=basePath%>/image/back.png"/></td>
        <td><img src="<%=basePath%>/image/forgot.png" class="header_img2" style="width: 94px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div style="text-align: center;width: 100%;">
    <table cellpadding="0" cellspacing="15" style="width: 100%;">
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/phone.png');"></i>
                <input id="tel" class="text-input" type="text" pattern="[0-9]*" placeholder="手机号码">
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="pwd" class="text-input" type="password" placeholder="新密码">
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="pwd1" class="text-input" type="password" placeholder="确认密码">
            </td>
        </tr>
        <tr>
            <td align="center">
                <div  id="get_code" style="height: 32px;width: 80%;background-color: #36a756;border:0px red outset;border-radius: 5px;color: white;font-size: 16px;text-align: center;padding-top: 12px;" onclick="loadTelCode()">获取手机验证码</div>
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/msg.png');"></i>
                <input class="text-input" style="" type="text" pattern="[0-9]*" id="tel_code" placeholder="手机验证码">
            </td>
        </tr>
        <tr>
            <td>
                <img src="<%=basePath%>/image/submit.png" style="width: 153px;height: 50px;border: none;margin-top: 25px;"  onclick="save()"/>
            </td>
        </tr>
    </table>
</div>

</body>

<script>

    $(document).ready(function () {
        $("#get_code").width($("#pwd").width()+29);
    });

    function resize() {
        $("#get_code").width($("#pwd").width()+29);
    }

    var isReturn=false;
    function loadTelCode() {
        var tel=$("#tel").val();

        if ($.trim(tel)==""||!tel.match(/^1[34578]\d{9}$/)){
            alert("请输入正确的手机号码");
            return;
        }

        if (isReturn){
            return;
        }else{
            isReturn=true;
        }
        $("#get_code").html("60秒后重发");
        var current=60;
        var timeId=setInterval(function(){
            current--
            $("#get_code").html(current+"秒后重发");

            if (current<=0){
                clearInterval(timeId);
                isReturn=false;
                $("#get_code").html("获取手机验证码");
            }

        },1000);

        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/vercode/telcode",
            data: {tel:tel,check:"1",gameId:localStorage.getItem('gameId')},
            dataType: "json",
            success: function (result) {
                if (result.code!=1000){
                    alert(result.message);
                }
            },
            error : function( req, status, err) {
                console.info(status+","+err)
                var auth_url = req.getResponseHeader("REQUIRES_AUTH_URL");
                if(auth_url){
                    window.location.href = auth_url;
                }else{
                    alert("请稍后再试");
                }
            }
        });
    }

    function save() {

        var tel=$("#tel").val();

        if ($.trim(tel)==""||!tel.match(/^1[34578]\d{9}$/)){
            alert("请输入正确的手机号码");
            return;
        }

        var pwd = $("#pwd").val();
        var pwd1 = $("#pwd1").val();

//        var pwd_old=$("#pwd_old").val();

//        if ($.trim(pwd_old)==""){
//            alert("请输入密码");
//            return;
//        }else{
//            pwd_old=hex_md5(pwd_old);
//        }

        if (pwd==""||!pwd.match(/^\w{6,16}$/)){
            alert("请输入正确的密码(字母和数字6-16位)");
            return;
        }

        if (pwd!=pwd1){
            alert("确认密码不匹配");
            return;
        }else{
            pwd=hex_md5(pwd);
        }

//        if (pwd==pwd_old){
//            alert("新密码和旧密码不能相同");
//            return;
//        }

        var tel_code=$("#tel_code").val().trim();
        if (tel_code==""){
            alert("请输入手机验证码");
            return;
        }

        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/forgot",
            data: {
                pwdNew:pwd,
                telCode:tel_code,
                tel:tel,gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if (result.code == 1000) {
                    alert(result.message);
                    window.location = "<%=path%>/page/index?t=" + new Date().getTime();
                    localStorage.setItem("name", tel);
                } else {
                    alert(result.message);
                }
            },
            error : function( req, status, err) {
                console.info(status+","+err)
                var auth_url = req.getResponseHeader("REQUIRES_AUTH_URL");
                if(auth_url){
                    window.location.href = auth_url;
                }else{
                    alert("请稍后再试");
                }
            }
        });
    }

</script>
</html>