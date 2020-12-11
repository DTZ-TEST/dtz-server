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
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.2">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
</head>

<body style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<table class="header_table" cellpadding="0" cellspacing="0">
    <tr>
        <td class="fix_td" onclick="history.back()"><img class="header_img1" src="<%=basePath%>/image/back.png"/></td>
        <td><img src="<%=basePath%>/image/bc.png" class="header_img2" style="width: 120px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div class="table1" style="margin-top: 2px;">
   <table cellpadding="5"
       style="text-align: center;margin-top: 10px;border-bottom: 1px solid #b5b5b5;width: 100%;padding-bottom: 10px;">
    <tr>
       <select onchange="selectSub();" id="type" class="text-input" style="width: 95%;height: 30px;"><option value="0">玩家退卡给玩家</option><option value="2">玩家退卡给代理</option><option value="1">代理退卡给代理</option></select>
    </tr>
   </table>
</div>
<div class="table2" style="margin-top: 2px;">
  <table id="table_data0" style="width: 100%;text-align: center;font-size: 18px;" cellspacing="0" cellpadding="10">
        <tr>
            <td><input id="playerId" class="text-input" type="text" pattern="[0-9]*" placeholder="退卡玩家id"></td><td></td>
        </tr>
        <tr>
            <td><input id="playerName" class="text-input" type="text" pattern="[0-9]*" placeholder="玩家昵称" disabled="disabled"></td><td><input id="playercardNum" class="text-input" type="text" pattern="[0-9]*" placeholder="房卡数" disabled="disabled"></td>
        </tr>
         <tr>
            <td><input id="playerId2" class="text-input" type="text" pattern="[0-9]*" placeholder="接收玩家id"></td><td></td>
        </tr>
        <tr>
            <td><input id="playerName2" class="text-input" type="text" pattern="[0-9]*" placeholder="玩家昵称" disabled="disabled"></td><td><input id="playercardNum2" class="text-input" type="text" pattern="[0-9]*" placeholder="房卡数" disabled="disabled"></td>
        </tr>
        <tr>
           <td><input id="cardNum0" class="text-input" type="text" pattern="[0-9]*" placeholder="退卡数量"></td><td><input id="query1" onclick="myQuery(2)" type="button" value="查询"/> &nbsp &nbsp  &nbsp  &nbsp  &nbsp  <input id="back1" onclick="back(2)" type="button" value="退卡"/></td>
        </tr>
    </table>
    <table id="table_data1" style="width: 100%;text-align: center;font-size: 18px;" cellspacing="0" cellpadding="10">
        <tr>
            <td><input id="playerId3" class="text-input" type="text" pattern="[0-9]*" placeholder="玩家id"></td><td></td>
        </tr>
        <tr>
            <td><input id="playerName3" class="text-input" type="text" pattern="[0-9]*" placeholder="玩家昵称" disabled="disabled"></td><td><input id="playercardNum3" class="text-input" type="text" pattern="[0-9]*" placeholder="房卡数" disabled="disabled"></td>
        </tr>
        <tr>
           <td><input id="agencyId" class="text-input" type="text" pattern="[0-9]*" placeholder="代理账号"></td> <td></td>
        </tr>
        <tr>
           <td><input id="agencyTel" class="text-input" type="text" pattern="[0-9]*" placeholder="备注" disabled="disabled"></td><td><input id="agencyCardNum1" class="text-input" type="text" pattern="[0-9]*" placeholder="代理房卡数" disabled="disabled"></td>
        </tr>
        <tr>
           <td><input id="cardNum1" class="text-input" type="text" pattern="[0-9]*" placeholder="退卡数量"></td><td><input id="query1" onclick="myQuery(0)" type="button" value="查询"/> &nbsp &nbsp  &nbsp  &nbsp  &nbsp  <input id="back2" onclick="back(0)" type="button" value="退卡"/></td>
        </tr>
    </table>
    
     <table id="table_data2" style="width: 100%;text-align: center;font-size: 18px;" cellspacing="0" cellpadding="10">
        <tr>
           <td> <input id="agencyId1" class="text-input" type="text" pattern="[0-9]*" placeholder="退卡代理账号"></td><td></td>
        </tr>
        <tr>
            <td><input id="agencytel3" class="text-input" type="text" pattern="[0-9]*" placeholder="退卡代理备注" disabled="disabled"></td><td><input id="agencyCardNum" class="text-input" type="text" pattern="[0-9]*" placeholder="退卡代理房卡数" disabled="disabled"></td>
        </tr>
        <tr>
          <td><input id="agencyId2" class="text-input" type="text" pattern="[0-9]*" placeholder="接收代理账号"></td> <td></td>
        </tr>
        <tr>
            <td><input id="agencytel2" class="text-input" type="text" pattern="[0-9]*" placeholder="接收代理备注" disabled="disabled"></td><td><input id="agencyCardNum2" class="text-input" type="text" pattern="[0-9]*" placeholder="接收代理房卡数" disabled="disabled"></td>
        </tr>
        <tr>
           <td><input id="cardNum2" class="text-input" type="text" pattern="[0-9]*" placeholder="退卡数量"></td><td><input onclick="myQuery(1)" type="button" value="查询"/> &nbsp &nbsp  &nbsp  &nbsp  &nbsp  <input id="back3" onclick="back(1)" type="button" value="退卡"/></td>
        </tr>
    </table>
</div>
<div class="table3" style="margin-top: 2px;">
    <table id="table_data" style="width: 100%;text-align: center;font-size: 10px;" cellspacing="0" cellpadding="5">
        <tr style="color: #ee6a2a;font-size: 20px;">
            <td>退卡id</td>
            <td>昵称</td>
            <td>接收id</td>
            <td>昵称</td>
            <td>退卡数量</td>
            <td>时间</td>
        </tr>
    </table>
</div>
<input type="hidden" id="admin" value="${sessionScope.roomCard.partAdmin}"/>
<input type="hidden" id="me" value="${sessionScope.roomCard.agencyId}"/>
<input type="hidden" id="agencyLevel" value="${sessionScope.roomCard.agencyLevel}"/>
</body>

<script>
    var type = 0;
    $(document).ready(function () {
     $("#table_data2").hide();
     $("#table_data1").hide();
     $("#back1").hide();
    /*  my(0); */
    });
    
   function selectSub(){
      type = $("#type").val();
      if(type == 0){
         $("#back1").hide();
         $("#table_data0").show();
         $("#table_data2").hide();
         $("#table_data1").hide();
         my(0);
      }else if(type == 2){
         $("#back2").hide();
         $("#table_data2").hide();
         $("#table_data0").hide();
         $("#table_data1").show();
         my(2);
      }else{
         $("#back3").hide();
         $("#table_data2").show();
         $("#table_data0").hide();
         $("#table_data1").hide();
         my(1);
      }
   }
   
   function my(append) {
	   if(append==0){
	       $.ajax({
	           timeout:60000,
	           async:true,
	           type: "POST",
	           url: "<%=path%>/user/player/agency/cardbackinfo",
	           data: {num:$("#num").val(),gameId:localStorage.getItem('gameId')},
	           dataType: "json",
	           success: function(result){
	               if (result.code==1000){
	                   totalPage = result.page;
	                   if(totalPage <= 1){
	                     $("#pp").hide();
	                     page=1; 
	                   }else{
	                     $("#pp").show();
	                   }
	                 //   $("#page").html(page+"/"+totalPage);
	                //    $("#span_total").html('共'+result.count+'名,'+(result.total)+'钻');
	                   $("#table_data").html('<tr style="color: #ee6a2a;font-size: 20px;"><td>退卡id</td><td>昵称</td><td>接收id</td><td>昵称</td><td>退卡数量</td><td>时间</td></tr>');
	                    $.each(result.datas,function (index,tempData){
	                    	 var tmpDate = new Date();
	                            tmpDate.setTime(tempData.createTime);
	                            tmpDate = tmpDate.format("yyyy-MM-dd hh:mm:ss");
	                       var newRow="<tr><td>"+tempData.sendUserId+"</td><td>"+tempData.sendName+"</td><td>"+tempData.reciaveUserId+"</td><td>"+tempData.reciaveName+"</td><td>"+tempData.cardNum+"</td><td>"+tmpDate+"</td></tr>";
	                       $('#table_data').append(newRow);
	                   });

	                   loading=false;
	               }else{
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
	   }else{
	       $.ajax({
	           timeout:60000,
	           async:true,
	           type: "POST",
	           url: "<%=path%>/user/queryAgencyCard",
	           data: {num:$("#num").val(),gameId:localStorage.getItem('gameId')},
	           dataType: "json",
	           success: function(result){
	               if (result.code==1000){
	                   totalPage = result.page;
	                   if(totalPage <= 1){
	                     $("#pp").hide();
	                     page=1; 
	                   }else{
	                     $("#pp").show();
	                   }
	                    $("#page").html(page+"/"+totalPage);
	                    $("#span_total").html('共'+result.count+'名,'+(result.total)+'钻');
	                   $("#table_data").html('<tr style="color: #ee6a2a;font-size: 20px;"><td>邀请码</td><td>用户名</td><td>数量</td></tr>');
	                    $.each(result.datas,function (index,tempData){
	                   	 var name = "";
	                        if(tempData.userName != null){
	                           name = tempData.userName;
	                        }
	                       var newRow="<tr><td>"+tempData.agencyId+"</td><td>"+name+"</td><td>"+tempData.commonCard+"</td></tr>";
	                       $('#table_data').append(newRow);
	                   });

	                   loading=false;
	               }else{
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

   }
   
   function cardbackinfoOne(type) {
       var url ="<%=path%>/user/player/agency/cardbackinfoOne";
       $.ajax({
           timeout:60000,
           async:true,
           type: "POST",
           url: url,
           data: {agencyId: $("#agencyId").val(),gameId:localStorage.getItem('gameId'),playerId:$("#playerId").val(),playerId3:$("#playerId3").val(),playerId2:$("#playerId2").val(), agencyId1: $("#agencyId1").val(),agencyId2:$("#agencyId2").val(),type:type},
           dataType: "json",
           success: function(result){
               if (result.code==1000){
                   totalPage = result.page;
                   if(totalPage <= 1){
                     $("#pp").hide();
                     page=1; 
                   }else{
                     $("#pp").show();
                   }
                 //   $("#page").html(page+"/"+totalPage);
                //    $("#span_total").html('共'+result.count+'名,'+(result.total)+'钻');
                   $("#table_data").html('<tr style="color: #ee6a2a;font-size: 20px;"><td>退卡id</td><td>昵称</td><td>接收id</td><td>昵称</td><td>退卡数量</td><td>时间</td></tr>');
                    $.each(result.datas,function (index,tempData){
                    	 var tmpDate = new Date();
                            tmpDate.setTime(tempData.createTime);
                            tmpDate = tmpDate.format("yyyy-MM-dd hh:mm:ss");
                       var newRow="<tr><td>"+tempData.sendUserId+"</td><td>"+tempData.sendName+"</td><td>"+tempData.reciaveUserId+"</td><td>"+tempData.reciaveName+"</td><td>"+tempData.cardNum+"</td><td>"+tmpDate+"</td></tr>";
                       $('#table_data').append(newRow);
                   });

                   loading=false;
               }else{
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
   
   function myQuery(type) {
	   if(type==2){
		   cardbackinfoOne();
	   }
        var url ="<%=path%>/user/player/agency";
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: url,
            data: {
                agencyId: $("#agencyId").val(),gameId:localStorage.getItem('gameId'),playerId:$("#playerId").val(),playerId3:$("#playerId3").val(),playerId2:$("#playerId2").val(), agencyId1: $("#agencyId1").val(),agencyId2:$("#agencyId2").val(),type:type
            },
            dataType: "json",
            success: function (result) {
                if (result.code == 1000) {
                    if(type == 0){
                        $("#playerName3").val(result.player.name);
	                    $("#playercardNum3").val(result.player.cards+result.player.freeCards);
	                    $("#agencyName").val(result.agency.userName);
	                    $("#agencyTel").val(result.agency.remark);
	                    $("#agencyCardNum1").val(result.agency.commonCard);
	                    $("#back2").show();
                    }else if(type == 2){
                        $("#playerName").val(result.player.name);
	                    $("#playercardNum").val(result.player.cards+result.player.freeCards);
	                    $("#playerName2").val(result.player2.name);
	                    $("#playercardNum2").val(result.player2.cards+result.player2.freeCards);
	                    $("#back1").show();
                    }else{
                        $("#agencyName1").val(result.roomCard1.userName);
	                    $("#agencyName2").val(result.roomCard2.userName);
	                    $("#agencyCardNum").val(result.roomCard1.commonCard);
	                    $("#agencyCardNum2").val(result.roomCard2.commonCard);
	                    $("#agencytel2").val(result.roomCard2.remark);
	                    $("#agencytel3").val(result.roomCard1.remark);
	                    if(result.roomCard1.agencyLevel==99){
	                        alert("不能从管理员退卡");
	                        return;
                        }
	                    $("#back3").show();
                    }
                } else {
                    alert(result.message);
                }
            },
            error: function (req, status, err) {
                var auth_url = req.getResponseHeader("REQUIRES_AUTH_URL");
                if (auth_url) {
                    window.location.href = auth_url;
                } else {
                    alert("请稍后再试");
                }
            }
        });
    }

    function back(type) {
        var cardNum = 0; 
        var cardNum1 = $("#cardNum1").val();
        var cardNum0 = $("#cardNum0").val();
        var playercardNum = $("#playercardNum").val();
        var playerId = $("#playerId").val();
        var playerId2 = $("#playerId2").val();
        var playerId3 = $("#playerId3").val();
        var agencyId = $("#agencyId").val();
        
        var agencyId1 = $("#agencyId1").val();
        var agencyId2 = $("#agencyId2").val();
        var cardNum2 = $("#cardNum2").val();
        var agencyCardNum = $("#agencyCardNum").val();
        if(type == 0){
           cardNum = cardNum1;
           if(cardNum1<0){
             alert("退的房卡数不能小于0");
             return;
           }
           if(playerId3=""){
             alert("请输入玩家id");
             return;
           }
           if(agencyId=""){
             alert("请输入代理邀请码");
             return;
           }
        }else if(type == 2){
           cardNum = cardNum0;
           if(cardNum0<0){
             alert("退的房卡数不能小于0");
             return;
           }
           if(playerId=""){
             alert("请输入退卡玩家id");
             return;
           }
           if(playerId2=""){
             alert("请输入接收玩家id");
             return;
           }
        }else{
            cardNum = cardNum2;
           if(cardNum2<0){
             alert("退的房卡数不能小于0");
             return;
           }
           if(agencyId1=""){
             alert("请输入退卡代理邀请码");
             return;
           }
           if(agencyId2=""){
             alert("请输入接收代理邀请码");
             return;
           }
        }
        var a = confirm("确定要退卡吗?"); 
        if(!a){
           return;
        }
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/player/agency/cardback",
            data: {
                agencyId: $("#agencyId").val(),gameId:localStorage.getItem('gameId'),playerId2:$("#playerId2").val(),playerId3:$("#playerId3").val(),playerId:$("#playerId").val(),type:type,cardNum:cardNum, agencyId1: $("#agencyId1").val(),agencyId2:$("#agencyId2").val(),gameId:$("#gameId").val()
            },
            dataType: "json",
            success: function (result) {
                if (result.code!=1000){
                   // $("#table_data2").hide();
                    //$("#back1").hide();
                    alert(result.message);
                    //myQuery(type);
                }else{
                    alert(result.message);
                    myQuery(type);
                }
            },
            error : function( req, status, err) {
                console.info(status+","+err);
                var auth_url = req.getResponseHeader("REQUIRES_AUTH_URL");
                if(auth_url){
                    window.location.href = auth_url;
                }else{
                    alert("请稍后再试");
                }
            }
        });
    }
 Date.prototype.format = function (format) {
        var date = {
            "M+": this.getMonth() + 1,
            "d+": this.getDate(),
            "h+": this.getHours(),
            "m+": this.getMinutes(),
            "s+": this.getSeconds(),
            "q+": Math.floor((this.getMonth() + 3) / 3),
            "S+": this.getMilliseconds()
        };
        if (/(y+)/i.test(format)) {
            format = format.replace(RegExp.$1, (this.getFullYear() + '').substr(4 - RegExp.$1.length));
        }
        for (var k in date) {
            if (new RegExp("(" + k + ")").test(format)) {
                format = format.replace(RegExp.$1, RegExp.$1.length == 1
                    ? date[k] : ("00" + date[k]).substr(("" + date[k]).length));
            }
        }
        return format;
    }

</script>
