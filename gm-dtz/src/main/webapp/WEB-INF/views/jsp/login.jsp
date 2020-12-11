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
        <span style="color: #0d6cac;font-size: 28px;top: 0px;position:fixed;left: 0px;width: 100%;margin-top: 30px;">快乐打筒子代理后台</span>
    </div>
    <table style="margin-top: 128px;width: 100%;text-align: center;" cellpadding="10">
        <tr style="width: 100%;">
            <td style="text-align: center;vertical-align: middle;width: 80%;">
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/phone.png');"></i>
                <input class="text-input" type="text" pattern="[0-9]*" id="name" name="name" placeholder="手机号码">
            </td>
        </tr>
        <tr>
            <td style="text-align: center;vertical-align: middle;width: 80%;">
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input class="text-input" type="password" id="pwd" name="pwd" placeholder="密码"></td>
        </tr>
        <tr>
            <td style="text-align: center;vertical-align: middle;width: 80%;">
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/msg.png');"></i>
                <input class="text-input" type="text" pattern="[0-9]*" id="code" name="code" placeholder="验证码">
            </td>

        </tr>
        <tr>
            <td>
                <img id="get_code" src="<%=path%>/vercode/code" onclick="this.src='<%=path%>/vercode/code?t='+new Date().getTime()" />
            </td>
        </tr>
        <tr align="center">
            <td align="center">
                <input  type="checkbox" checked="checked" id="autoPass">记住密码
            </td>
        </tr>
        <tr>
            <td><img src="<%=basePath%>/image/login.png" style="width: 153px;height: 50px;border: none;" onclick="login();"/></td>
        </tr>
        <tr>
            <td><a onclick="window.location='<%=path%>/page/register'" style="color: #0d6cac;">注册</a></td>
        </tr>
        <tr>
            <td> <a onclick="window.location='<%=path%>/page/forgot'" style="color: #0d6cac;">找回密码</a></td>
        </tr>
    </table>

    <div style="display: none;position:fixed;bottom: 115px;text-align: right;right: 15px;color: #b5b5b5;font-size: 18px;">
        <a onclick="window.location='<%=path%>/page/register'" style="color: #0d6cac;">注册</a>
    </div>
    <div style="display: none;position:fixed;bottom: 115px;text-align: right;right: 15px;color: #b5b5b5;font-size: 18px;">
        <a onclick="window.location='<%=path%>/page/forgot'" style="color: #0d6cac;">找回密码</a>
    </div>
    <%--<div style="position:fixed;bottom: 0px;background-image: url('<%=basePath%>/image/login_bottom.png');background-repeat: round;background-size:100%;width: 100%;height: 100px;">--%>

    <%--</div>--%>
</div>

</body>
<script>

    $(document).ready(function () {
        $("body").css("height",$(window).height());
        var name=localStorage.getItem("name");
        if ($.trim(name)!=""){
            $("#name").val(name);
        }

        var autoPass=localStorage.getItem("autoPass");
        if("1"==autoPass){
            $('#autoPass').attr('checked','checked');
            $('#pwd').val(localStorage.getItem("pwd"));
        }else if("0"==autoPass){
            $('#autoPass').removeAttr('checked');
        }
    });

    function login() {
        var name=$("#name").val();
        var pwd=$("#pwd").val();
        var code=$("#code").val();

        if ($.trim(code)==""){
            alert("请输入验证码");
            return;
        }
        if ($.trim(name)==""){
            alert("请输入手机号码");
            return;
        }else{
            if (!name.match(/^1[34578]\d{9}$/)){
                alert("请输入正确的手机号码");
                return;
            }
        }

        if ($.trim(pwd)==""){
            alert("请输入密码");
            return;
        }else if (pwd.length!=32){
            pwd=hex_md5(pwd);
        }


        $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/login",
            data: {name:name, pwd:pwd,code:code},
            dataType: "json",
            success: function(result){
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if (result.code==1000){
                    var gameId = localStorage.getItem("gameId");
                    if (gameId!="3"){
                        gameId="1";
                    }
                    localStorage.setItem("gameId",gameId);
                    window.location="<%=path%>/page/home?gameId="+gameId+"&t="+new Date().getTime();
                    localStorage.setItem("name",name);

                    var autoPass=$('#autoPass').is(':checked');
                    if(autoPass){
                        localStorage.setItem("autoPass","1");
                        localStorage.setItem("pwd",pwd);
                    }else{
                        localStorage.setItem("autoPass","0");
                        localStorage.removeItem("pwd");
                    }
                }else{
                    $("#get_code").attr("src","<%=path%>/vercode/code?t="+new Date().getTime());
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