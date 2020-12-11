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
        .text-input{
            height:25px;width: 65%;color: #0d6cac;font-size: 16px;
        }
    </style>
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.1">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="<%=basePath%>/js/md5.js"></script>
</head>

<body style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<table class="header_table" cellpadding="0" cellspacing="0">
    <tr>
        <td class="fix_td" onclick="history.back()"><img class="header_img1" src="<%=basePath%>/image/back.png"/></td>
        <td><img src="<%=basePath%>/image/my_msg.png" class="header_img2" style="width: 94px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div class="table2">
    <table cellspacing="0" cellpadding="15" style="width: 100%;text-align: left;vertical-align: middle;">
        <tr onclick="window.location='<%=path%>/page/user/pwd?gameId='+localStorage.getItem('gameId')">
            <td style="border-top: 0px;font-size: 22px;">修改密码</td>
            <td style="border-top: 0px;text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr onclick="window.location='<%=path%>/page/user/detail?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">完善资料</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
    </table>
</div>
</body>
</html>