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
      <tr id="tr1" onclick="window.location='<%=path%>/page/black/forbid?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">玩家禁止登录</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
         <tr id="tr2" onclick="window.location='<%=path%>/page/agency/forbid?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">代理禁止登录</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr3" onclick="window.location='<%=path%>/page/manage/transfer?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">代理转移</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr4" onclick="window.location='<%=path%>/page/manage/improve?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">代理升级</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr5" onclick="window.location='<%=path%>/page/manage/player_pay_bind_agency/rest?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">解绑邀请码</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr6" onclick="window.location='<%=path%>/page/manage/agency/rest?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">重置代理信息</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr7" onclick="window.location='<%=path%>/page/pay/offlinePay?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">手动售钻补录</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr8" onclick="window.location='<%=path%>/page/manage/buy/card/info?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">售钻查询</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr9" onclick="window.location='<%=path%>/page/d/pay/history/agency?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">代充查询</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr10" onclick="window.location='<%=path%>/page/manage/back/cards?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">退卡</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr11" onclick="window.location='<%=path%>/page/manage/vip/wx?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">VIP代理微信号修改</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr12" onclick="window.location='<%=path%>/page/agency/query?gameId='+localStorage.getItem('gameId')" id="agencyInfo">
            <td style="font-size: 22px;">代理信息查询</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr13" onclick="window.location='<%=path%>/page/unroom?gameId='+localStorage.getItem('gameId')" id="unroom">
            <td style="font-size: 22px;">解散房间</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr14" onclick="window.location='<%=path%>/page/manage/cashresend?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">提现补发</td>
           
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr15" onclick="window.location='<%=path%>/page/manage/activityreward?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">邀请红包活动查询</td>
           
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr16" onclick="window.location='<%=path%>/page/queryGroupByplayerId?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">玩家俱乐部查询</td>
           
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr17" onclick="window.location='<%=path%>/page/queryAgencyPay?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">充钻查询</td>
           
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr18" onclick="window.location='<%=path%>/page/queryAgencyPaySource?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">代理充值关联查询</td>
           
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
         <tr id="tr19" onclick="window.location='<%=path%>/page/agencyCard?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">代理余钻查询</td>
           
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr20" onclick="window.location='<%=path%>/page/blacks/groupmanage?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">更换群主</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr20" onclick="window.location='<%=path%>/page/jl?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">比赛奖励查询</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr21" onclick="window.location='<%=path%>/page/whitemenu?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">白名单</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr22" onclick="window.location='<%=path%>/page/delgoldroom?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">解散金币场房间</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr23" onclick="window.location='<%=path%>/page/transferLeader?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">亲友圈转移下级组长</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
        <tr id="tr24" onclick="window.location='<%=path%>/page/bindPhone?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">绑定手机号</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
       <%--  <tr onclick="window.location='<%=path%>/page/blacks/groupmanage'">
            <td style="font-size: 22px;">更换群主</td>
           
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr> --%>
        <%-- <tr  id="tt" onclick="window.location='<%=path%>/page/agencyPurchase'">
            <td style="font-size: 22px;">代理购卡</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr> --%>
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
                 if (this.id!="tr12"&&this.id!="tr20"){
                     if (agencyId=="771399") {
                     }else{
                         $("#"+this.id).remove();
                     }
                 }
             });
         }
     });
   var agencyLevel = $("#agencyLevel").val();
   if(agencyLevel!=99){
       	$("#tr21").hide();
   }
     </script>
</html>
