<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() == 80 ? "" : (":" + request.getServerPort())) + request.getContextPath();
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
        <td><img src="<%=basePath%>/image/pkdata.png" class="header_img2" style="width: 94px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home'"><img class="header_img3"
                                                                                src="<%=basePath%>/image/home.png"/>
        </td>
    </tr>
</table>
<%@include file="./switchGame.html"%>
<table cellpadding="5"
       style="text-align: center;margin-top: 10px;border-bottom: 1px solid #b5b5b5;width: 100%;padding-bottom: 10px;">
    <tr>
        <td style="text-align: left"><input id="start_time" value="${requestScope.startDate}" type="date"
                                            placeholder="开始时间" style="width: 85%;height: 25px;"/></td>
        <td><input id="end_time" type="date" value="${requestScope.endDate}" placeholder="结束时间"
                   style="width: 85%;height: 25px;"/></td>
        <td><input onclick="myQuery()" type="button" value="查询"/></td>
    </tr>
</table>
<div class="table2" style="margin-top: 2px;">
    <table id="table_data" style="width: 100%;text-align: center;font-size: 16px;" cellspacing="0" cellpadding="10">
        <tr style="color: #ee6a2a;font-size: 18px;">
            <td>日期</td>
            <td>日玩牌用户数</td>
            <td>日玩牌总大局数</td>
            <td>日玩牌总小局数</td>
        </tr>
    </table>
</div>

</body>

<script>
    var gameId;
    $(document).ready(function () {
        gameId=localStorage.getItem("gameId");
        if ($.trim(gameId)!=""){
            checkTab(gameId);
        }
        myQuery();
    });

    function myQuery() {
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/data/statistics/pkdata",
            data: {
                startDate: $("#start_time").val(),
                endDate: $("#end_time").val(),
                gameId: gameId
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if (result.code == 1000) {
                    $("#table_data").html('<tr style="color: #ee6a2a;font-size: 18px;"><td>日期</td><td>日玩牌用户数</td><td>日玩牌总大局数</td><td>日玩牌总小局数</td></tr>');
                    $.each(result.datas, function (index, tempData) {
                    	var commonuserNum =0;
                    	 var groupuserNum=0;
                    	 var golduserNum=0;
                    	 var countUserNum=0;
                    	 var goldxiaoplayNum=0;
                    	if (  typeof tempData.common == "undefined") { 
                    	this.commonuserNum=0;
                          }else{
                        	  this.commonuserNum=tempData.common.userNum;
                          }
                    	 if (  typeof tempData.group == "undefined") { 
                    		 this.groupuserNum=0;
                           }else{
                        	   this.groupuserNum= tempData.group.userNum;
                           }
                    	 if (  typeof tempData.gold == "undefined") { 
                    		 this.golduserNum=0;
                    		 this.goldxiaoplayNum=0;
                           }else{
                        	   this.golduserNum= tempData.gold.userNum;
                        	  this.goldxiaoplayNum =tempData.gold.xiaoplayNum
                           }
                    	 
                    	 if(typeof tempData.common == "undefined"&&typeof tempData.group == "undefined"){
                    		 this.countUserNum=0;
                    	 }else if(typeof tempData.common != "undefined"){
                    		 this.countUserNum=tempData.common.countuserNum;
                    	 }else if(typeof tempData.group != "undefined"){
                    		 this.countUserNum=tempData.group.countuserNum;
                    	 }
                    	 var countplayNum1=tempData.playNum-this.goldxiaoplayNum;
                        var newRow = "<tr onclick='displayData(this.id)' id='data" + tempData.currentDate + "'><td>" + tempData.currentDate + "</td><td>" + this.countUserNum + "</td><td>" + countplayNum1 + "</td><td>" + tempData.xiaoplayNum + "</td></tr>";
                        newRow += '<tr id="detail' + tempData.currentDate + '" style="display: none"><td colspan="5"><table class="table3" style="width: 100%;text-align: left;background-color: #f3ffea;" cellspacing="0" cellpadding="2">'
                            + '<tr><td>日期</td><td>' + tempData.currentDate + '</td></tr>'
                            if (  typeof tempData.common != "undefined") { 
                            	newRow += 	'<tr><td>普通场玩牌用户数</td><td>'+ tempData.common.userNum +'('+ (tempData.common.userNum *100 /this.countUserNum).toFixed(2) +'%)'+ '</td></tr>'
                                + '<tr><td>普通场总大局数</td><td>' + tempData.common.playNum + '('+ (tempData.common.playNum *100 /tempData.playNum).toFixed(2) +'%)'+'</td></tr>'
                                + '<tr><td>普通场总小局数</td><td>' + tempData.common.xiaoplayNum + '('+ (tempData.common.xiaoplayNum *100 /tempData.xiaoplayNum).toFixed(2) +'%)'+'</td></tr>'
                              }else{
                            	  newRow +=    '<tr><td>普通场玩牌用户数</td><td>0(0%)</td></tr>'
                                  + '<tr><td>普通场总大局数</td><td>0(0%)</td></tr>'
                                  + '<tr><td>普通场总小局数</td><td>0(0%)</td></tr>'
                              }
                            if (  typeof tempData.group != "undefined") { 
                            	newRow += 	  '<tr><td>亲友场玩牌用户数</td><td>' + tempData.group.userNum + '('+ (tempData.group.userNum *100 /this.countUserNum).toFixed(2) +'%)'+ '</td></tr>'
                                 + '<tr><td>亲友圈总大局数</td><td>' + tempData.group.playNum +'('+ (tempData.group.playNum *100 /tempData.playNum).toFixed(2)+'%)'+ '</td></tr>'
                                 + '<tr><td>亲友圈总小局数</td><td>' + tempData.group.xiaoplayNum +'('+ (tempData.group.xiaoplayNum *100 /tempData.xiaoplayNum).toFixed(2)+'%)'+ '</td></tr>'
                              }else{
                            	  newRow +=    '<tr><td>亲友场玩牌用户数</td><td>0(0%)</td></tr>'
                                  + '<tr><td>亲友圈总大局数</td><td>0(0%)</td></tr>'
                                  + '<tr><td>亲友圈总小局数</td><td>0(0%)</td></tr>'
                              }
                            if (  typeof tempData.gold != "undefined") { 
                            	newRow += 	  '<tr><td>金币场玩牌用户数</td><td>' + tempData.gold.userNum + '('+ (tempData.gold.userNum *100 /this.countUserNum).toFixed(2) +'%)'+ '</td></tr>'
                                 + '<tr><td>金币场总小局数</td><td>' + tempData.gold.xiaoplayNum +'('+ (tempData.gold.xiaoplayNum *100 /tempData.xiaoplayNum).toFixed(2)+'%)'+ '</td></tr>'
                              }else{
                            	  newRow +=    '<tr><td>金币场玩牌用户数</td><td>0(0%)</td></tr>'
                                  + '<tr><td>金币场总小局数</td><td>0(0%)</td></tr>'
                              }


                        newRow += '</table></td></tr>';

                        $('#table_data').append(newRow);
                    });
                } else {
                    alert(result.message);
                }
            },
            error: function (req, status, err) {
                console.info(status + "," + err);
                var auth_url = req.getResponseHeader("REQUIRES_AUTH_URL");
                if (auth_url) {
                    window.location.href = auth_url;
                } else {
                    alert("请稍后再试");
                }
            }
        });
    }

    function displayData(currentDate) {
        var tempId = currentDate.replace("data", "detail");
        if ($("#" + tempId).css("display") == "none") {
            $("#" + tempId).css("display", "");
        } else {
            $("#" + tempId).css("display", "none");
        }
    }
  function switchGame() {
        gameId = $("#gameId").val();
        if(gameId!=localStorage.getItem("gameId")){
            localStorage.setItem("gameId",gameId);
            window.location.reload();
        }
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
