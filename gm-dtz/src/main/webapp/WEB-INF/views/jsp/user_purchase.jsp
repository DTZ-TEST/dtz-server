<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
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
        <td><img src="<%=basePath%>/image/user_purchase.png" class="header_img2" style="width: 142px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<table id="con_table" cellpadding="5" style="text-align: center;margin-top: 10px;border-bottom: 1px solid #b5b5b5;width: 100%;padding-bottom: 10px;">
    <tr>
        <td colspan="3" style="text-align: left;width: 95%;"><input id="agency_id"  type="text" pattern="[0-9]*" placeholder="请输入邀请码" style="width: 85%;height: 25px;"/></td>
        <td><input onclick="myQuery()" type="button" value="查询"/></td>
         <td><input onclick="savePurchase()" type="button" value="添加购卡权限" id = "tt"/></td>
    </tr>
</table>
<div class="table2" style="margin-top: 2px;">
    <table id="table_data" style="width: 100%;text-align: center;font-size: 18px;" cellspacing="0" cellpadding="10">
       <tr >
        <td style="text-align: left;vertical-align: middle;color: #007cc3;font-size: 16px;padding-top: 15px;">
            &nbsp;姓名：<span id="myname" ></span><br/>
            &nbsp;手机号：<span id="phone" ></span><br/>
            &nbsp;是否有购卡权限：<span id="isHave"></span>
        </td>
        </tr>
    </table>
    
    
</div>
</body>
<script>
     $(function(){
         $("#tt").hide();
     }); 
    function myQuery() {
	        var agencyId = $("#agency_id").val().trim();
	        if(agencyId == undefined){
	          alert("请填写邀请码！");
	        }
	        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/agency/purchase",
            data: {agencyId: agencyId,
                gameId:localStorage.getItem("gameId")},
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if (result.code == 1000) {
                        $("#myname").html(result.name);
                        $("#phone").html(result.phone);
                        $("#isHave").html(result.isHavePurchase);
                        if(result.isHavePurchase=="否"){
                          $("#tt").show();
                        }else{
                          $("#tt").hide();
                        }
                  }
                  }
                });
        }
        
        function savePurchase() {
            var agencyId = $("#agency_id").val().trim();
	        if(agencyId == undefined){
	          alert("请填写邀请码！");
	        }
            $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/agency/savepurchase",
            data: {agencyId: agencyId},
            dataType: "json",
            success: function (result) {
                if (result.code == 1000) {
                         $("#isHave").html("是");
                         $("#tt").hide();
                  }
                 }
           });
        }
</script>
</html>