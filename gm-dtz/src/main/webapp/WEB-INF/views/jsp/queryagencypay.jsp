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
        <td><img src="<%=basePath%>/image/select.png" class="header_img2" style="width: 142px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<table id="con_table" cellpadding="5" style="text-align: center;margin-top: 10px;border-bottom: 1px solid #b5b5b5;width: 100%;padding-bottom: 10px;">
  <tr>
        <td style="text-align: left"><input id="start_time" value="${requestScope.startDate}" type="date" placeholder="开始时间" style="width: 85%;height: 25px;"/></td>
        <td><input id="end_time" value="${requestScope.endDate}" type="date" placeholder="结束时间" style="width: 85%;height: 25px;"/></td>
    </tr>
 <tr id="agencyId3"><td colspan="2" style="text-align: left;font-size: 20px;"><select id="type" class="text-input" style="width: 95%;height: 30px;"><option value="1" selected="selected">给玩家充钻7000-15000</option><option value="2" >给玩家充钻15000以上</option><option value="3" >给代理充钻20000-100000</option><option value="4" >给代理充钻100000以上</option></select></td>
     <td><input onclick="myQuery('0')" type="button" value="查询"/></td>
 </tr>
</table>
<div class="table2" style="margin-top: 2px;">

    <table id="table_data" style="width: 100%;text-align: center;font-size: 18px;" cellspacing="0" cellpadding="10">
        <tr style="color: #ee6a2a;font-size: 20px;">
            <td>邀请码</td>
            <td>玩家ID</td>
            <td>数量</td>
            <td>时间</td>
        </tr>
    </table>
</div>
</body>
<script>
    var currentNo=0;
    var isEnd=false;
    var loading=false;
    var totalPage;
    var page = 1;
    var type = 0;
    $(document).ready(function () {
    	$("#end_time").val(new Date().format("yyyy-MM-dd"));
        $("#start_time").val(new Date().format("yyyy-MM-dd"));
        myQuery("1");
    });
    function selectSub(num) {
        var temp=$("#agency_"+(0)).val();
        if(temp !=""){
           loadAgencies(1,temp.split("_")[1]);
           $("#agencyId2").css("display","");
        }else{
           $("#agencyId2").css("display","none");
        }
        
    }
    function myQuery(append) {
        $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/queryAgencyPay",
            data: {type:$("#type").val(), startDate:$("#start_time").val(),endDate:$("#end_time").val(),gameId:localStorage.getItem('gameId')},
            dataType: "json",
            success: function(result){
                if (result.code==1000){
                	if($("#type").val() == 3 || $("#type").val() == 4){
                		 $("#table_data").html('<tr style="color: #ee6a2a;font-size: 20px;"><td>邀请码</td><td>邀请码</td><td>数量</td><td>时间</td></tr>');
                	}else{
                		 $("#table_data").html('<tr style="color: #ee6a2a;font-size: 20px;"><td>邀请码</td><td>玩家ID</td><td>数量</td><td>时间</td></tr>');
                	}
                     $.each(result.datas,function (index,tempData){
                        var tempTime;
                        if (!isNaN(tempData.createTime)){
                            var tmpDate=new Date();
                            tmpDate.setTime(tempData.createTime);
                            tempTime=tmpDate.format("yyyy-MM-dd hh:mm:ss");
                        }else{
                            tempTime=tempData.createTime;
                        }
                        var newRow="<tr><td>"+tempData.agencyId+"</td><td>"+tempData.reciveId+"</td><td>"+tempData.nums+"</td><td>"+tempTime+"</td></tr>";
                        $('#table_data').append(newRow);
                    });

                    loading=false;
                }else{
                    alert(result.message);
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
</script>
</html>