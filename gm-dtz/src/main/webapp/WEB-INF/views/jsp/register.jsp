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
            padding-left:25px;height:30px;width: 65%;color: #0d6cac;font-size: 16px;
        }
    </style>
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.1">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="<%=basePath%>/js/md5.js"></script>
</head>

<body style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<div style="width: 100%;text-align: center;height: 100%;vertical-align: middle;">
    <div style="text-align: center;vertical-align:middle;position:fixed;top: 0px;background-image: url('<%=basePath%>/image/login_top.png');background-repeat: round;background-size:100%;width: 100%;height: 100px;">
        <span style="color: #0d6cac;font-size: 28px;top: 0px;position:fixed;left: 0px;width: 100%;margin-top: 30px;">快乐玩游戏代理后台</span>
    </div>
</div>

<div style="width: 100%;text-align: center">
    <table id="r" style="margin-top: 128px;width: 100%;text-align: center;" cellpadding="10">
        <tr>
            <td><i class="icon-input" style="background-image:url('<%=basePath%>/image/user.png');"></i>
                <input class="text-input" value="${requestScope.agencyId}" type="text" pattern="[0-9]*" id="inviterId" placeholder="上级代理的邀请码"></td>
        </tr>
        <tr>
            <td><i class="icon-input" style="background-image:url('<%=basePath%>/image/phone.png');"></i>
                <input class="text-input" type="text" pattern="[0-9]*" id="tel" placeholder="手机号"></td>
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
            <td><i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input class="text-input" type="password" id="pwd" placeholder="密码"></td>
        </tr>
        <tr>
            <td><i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input class="text-input" type="password" id="pwd1" placeholder="确认密码"></td>
        </tr>
        <tr>
            <td><img src="<%=basePath%>/image/register.png" style="width: 153px;height: 50px;border: none;" onclick="register();"></td>
        </tr>
        <tr>
            <td><a onclick="window.location='<%=path%>/page/index'" style="color: #0d6cac;font-size: 18px;">登陆</a></td>
        </tr>
    </table>
</div>
<input type="hidden" id="agencyId0" value="${requestScope.agencyId0}">
</body>
<script>
	var agencyId0 =  $("#agencyId0").val();
    var current=0;
    var max=60;
    var regp =/^[A-Za-z0-9]+$/;

    if(!regp.test(this.agencyId0) ){
    	alert("请联系客服！");
    	window.location.href='<%=basePath%>';
    }
    $(document).ready(function () {
        $("#get_code").width($("#tel").width()+29);

        if($("#inviterId").val().trim()!=""){
            $("#inviterId").attr("disabled","disabled");
        }
    });

    function resize() {
        $("#get_code").width($("#tel").width()+29);
    }

    var isReturn=false;
    function loadTelCode() {
        if (isReturn){
            return;
        }else{
            isReturn=true;
        }
        var tel = $("#tel").val();

        if ($.trim(tel)==""||!tel.match(/^1[34578]\d{9}$/)){
            alert("请输入正确的手机号码");
            return;
        }

        $("#get_code").html(max+"秒后重发");
        current=max;
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
            data: {
                tel: tel
            },
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

    function register() {
        var inviterId = $("#inviterId").val();
        var tel = $("#tel").val();
        var tel_code = $("#tel_code").val();
        var pwd = $("#pwd").val();
        var pwd1 = $("#pwd1").val();

        if ($.trim(inviterId) == "") {
            alert("请输入邀请人的邀请码");
            return;
        }

        if ($.trim(tel) == "") {
            alert("请输入手机号码");
            return;
        }else{
            if (!tel.match(/^1[34578]\d{9}$/)){
                alert("请输入正确的手机号码");
                return;
            }
        }

        if ($.trim(tel_code) == "") {
            alert("请输入手机验证码");
            return;
        }

        if ($.trim(pwd) == "") {
            alert("请输入密码");
            return;
        }else{
            if (!pwd.match(/^\w{6,16}$/)){
                alert("请输入正确的密码(字母和数字6-16位)");
                return;
            }
        }
        if ($.trim(pwd1) == "") {
            alert("请输入确认密码");
            return;
        }

        if (pwd != pwd1) {
            alert("两次输入的密码不一致");
            return;
        }
        pwd = hex_md5(pwd);

        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/register",
            data: {
                telCode: tel_code,
                tel: tel,
                inviterId: inviterId,
                pwd: pwd,
                agencyId0: $("#agencyId0").val()
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