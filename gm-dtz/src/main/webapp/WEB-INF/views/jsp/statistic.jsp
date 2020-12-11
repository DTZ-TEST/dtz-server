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

    </style>
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.1">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
</head>

<body style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<table class="header_table" cellpadding="0" cellspacing="0">
    <tr>
        <td class="fix_td" onclick="history.back()"><img class="header_img1" src="<%=basePath%>/image/back.png"/></td>
        <td><img src="<%=basePath%>/image/total_msg.png" class="header_img2" style="width: 94px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div class="table2">
    <table cellspacing="0" cellpadding="15" style="width: 100%;text-align: left;vertical-align: middle;">
        <tr onclick="window.location='<%=path%>/page/statistics/common?gameId='+localStorage.getItem('gameId')">
            <td style="border-top: 0px;font-size: 22px;">综合数据</td>
            <td style="border-top: 0px;text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr onclick="window.location='<%=path%>/page/statistics/cards?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">钻石消耗</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
          <tr onclick="window.location='<%=path%>/page/statistics/agent?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">代理统计</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
         <tr onclick="window.location='<%=path%>/page/statistics/onlineData?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">在线人数</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
         <tr id="zhsj_gold_all" onclick="window.location='<%=path%>/page/statistics/jfcommon?gameCode=all&gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">综合数据(积分场)</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
         <tr id="zsxh_gold_all" onclick="window.location='<%=path%>/page/statistics/jf/cards?gameCode=all&gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">钻石消耗(积分场)</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="zxrs_gold_dtz" onclick="window.location='<%=path%>/page/statistics/jfonlineData?gameCode=dtz&gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">在线人数(打筒子积分场)</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="zxrs_gold_pdk" onclick="window.location='<%=path%>/page/statistics/jfonlineData?gameCode=pdk&gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">在线人数(跑得快跑胡子积分场)</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
       <%--  <tr id="zxrs_gold_phz" onclick="window.location='<%=path%>/page/statistics/jfonlineData?gameCode=phz&gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">在线人数(跑胡子积分场)</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr> --%>
       <tr id="pkdata" onclick="window.location='<%=path%>/page/pkdata'">
            <td style="font-size: 22px;">对局数据</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
          <tr id="pkdata" onclick="window.location='<%=path%>/page/newDau'">
            <td style="font-size: 22px;">新DAU</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
    </table>
</div>

</body>

<script>
    $(document).ready(function () {
        if(localStorage.getItem("gameId")=="3"){
            $("#zsxh_gold_all").remove();
            $("#zhsj_gold_all").remove();
            $("#zxrs_gold_dtz").remove();
            $("#zxrs_gold_pdk").remove();
        }
    });
</script>
</html>