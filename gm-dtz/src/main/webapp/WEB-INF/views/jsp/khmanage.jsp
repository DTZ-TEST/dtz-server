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
    <title>快乐玩游戏代理后台_快乐打筒子_快乐跑得快_快乐跑胡子</title>

    <style>
    </style>
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.2">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="<%=basePath%>/js/md5.js"></script>
</head>

<body style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<table class="header_table" cellpadding="0" cellspacing="0">
    <tr>
        <td class="fix_td" onclick="history.back()"><img class="header_img1" src="<%=basePath%>/image/back.png"/></td>
        <td><img src="<%=basePath%>/image/manager.png" class="header_img2" style="width: 94px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div class="table2">
    <table cellspacing="0" cellpadding="15" style="width: 100%;text-align: left;vertical-align: middle;">
      <tr  id="tr1" onclick="window.location='<%=path%>/page/agency/query?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">代理信息查询</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr2" onclick="window.location='<%=path%>/page/manage/buy/card/info?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">售钻查询</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
         <tr id="tr3" onclick="window.location='<%=path%>/page/queryAgencyPay?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">充钻查询</td>
           
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
         <tr id="tr4" onclick="window.location='<%=path%>/page/queryAgencyPaySource?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">代理充值关联查询</td>
           
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr5" id="t4" onclick="window.location='<%=path%>/page/blacks/groupmanage?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">更换群主</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr  onclick="window.location='<%=path%>/page/whitemenu?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">白名单</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
    </table>
    <input type="hidden" id="purchase" value="${sessionScope.user.isHavePurchase}">
    <input type="hidden" id="roleId" value="${sessionScope.user.roleId}">
    <input type="hidden" id="agencyId" value="${sessionScope.roomCard.agencyId}">
     <input type="hidden" id="agencyLevel" value="${sessionScope.roomCard.agencyLevel}"/>
</div>
</body>
<script>
   $(function(){
       if ("2"==$("#roleId").val()) {
           var agencyId = $("#agencyId").val();
           $("tr[id^='tr']").each(function () {
               if (this.id!="tr1"){
                   if (agencyId=="771399") {
                   }else{
                       $("#"+this.id).remove();
                   }
               }
           });
       }
         
     }); 
     </script>
</html>
