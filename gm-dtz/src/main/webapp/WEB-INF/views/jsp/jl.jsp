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
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.2">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
</head>

<body style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<table class="header_table" cellpadding="0" cellspacing="0">
    <tr>
        <td class="fix_td" onclick="history.back()"><img class="header_img1" src="<%=basePath%>/image/back.png"/></td>
        <td><img src="<%=basePath%>/image/select_prize.png" class="header_img2" style="width: 94px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<table cellpadding="5" style="text-align: center;margin-top: 10px;border-bottom: 1px solid #b5b5b5;width: 100%;padding-bottom: 10px;">
    <tr>
        <td style="text-align: center;width: 70%"><input id="player_id" type="text" pattern="[0-9]*" placeholder="玩家id" style="width: 85%;height: 25px;"/></td>
        <td width="20%"><input onclick="myQuery()" type="button" value="查询"/></td>
    </tr>
</table>

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
    <table id="table_data" style="width: 100%;text-align: center;font-size: 18px;" cellspacing="0" cellpadding="10">
        <tr style="color: #ee6a2a;font-size: 20px;">
            <td>玩家</td>
            <td>昵称</td>
            <td>奖品名称</td>
            <td>抽奖时间</td>
            <td>是否发放</td>
        </tr>
        <!--<tr>-->
        <!--<td>1</td>-->
        <!--<td>50</td>-->
        <!--<td>2017-04-17 12:00:30</td>-->
        <!--</tr>-->
        <!--<tr>-->
        <!--<td>2</td>-->
        <!--<td>50</td>-->
        <!--<td>2017-04-17 12:00:30</td>-->
        <!--</tr>-->
    </table>
</div>
</body>
<script>

    $(document).ready(function () {
    	myQuery();
    });

    window.onscroll= function(){
        if ($(document).scrollTop()>=$(document).height()-$(window).height()){
//            myQuery("1");
        }
    }

    function myQuery() {
        myQuery("0");
    }

    function myQuery(append) {
        $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/jl",
            data: {userId:$("#player_id").val(), startDate:$("#start_time").val(),endDate:$("#end_time").val(),gameId:localStorage.getItem('gameId')},
            dataType: "json",
            success: function(result){
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if (result.code==1000){
                    $("#table_data").html('<tr style="color: #ee6a2a;font-size: 20px;"><td>玩家</td> <td>比赛名</td><td>奖品名称</td><td>中奖时间</td><td>是否发放</td></tr>');
                    if(result.datas.length>0){
                    $.each(result.datas,function (index,tempData){
                        var tempTime;
                        if (!isNaN(tempData.createdTime)){
                            var tmpDate=new Date();
                            tmpDate.setTime(tempData.createdTime);
                            tempTime=tmpDate.format("yyyy-MM-dd hh:mm:ss");
                        }else{
                            tempTime=tempData.createTime;
                        }
                        var State;
                        if(tempData.awardState==1){
                        	State="发放";
                        }else{
                        	State="已发放";
                        }
                        var newRow="<tr><td>"+tempData.userId+"</td><td>"+tempData.matchName+"</td><td>" +tempData.userAward+"</td><td>"+tempTime+"</td><td><button type='button' onclick='ff("+tempData.userId+","+tempData.matchId+")'>"+State+"</button></td></tr>";
                        $('#table_data').append(newRow);
                    });}else{
                        var newRow="<tr><td colspan='4'>暂无记录！</td></tr>";
                        $('#table_data').append(newRow);
                    }
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

    Date.prototype.format = function(format) {
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
    
    function ff (userId,matchId){
    	$.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/ff",
            data: {
            	userId:userId,matchId:matchId
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if (result.code == 1000) {
                	/*alert("发放成功！");  */
                	 location.reload();
                } else {
                    alert(result.message);
                }
            },
            error: function (req, status, err) {
                console.info(status + "," + err)
                var auth_url = req.getResponseHeader("REQUIRES_AUTH_URL");
                if (auth_url) {
                    window.location.href = auth_url;
                } else {
                    alert("请稍后再试");
                }
            }
        });
    }
</script>
</html>