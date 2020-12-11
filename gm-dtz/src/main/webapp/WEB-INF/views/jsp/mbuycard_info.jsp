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
        <td><img src="<%=basePath%>/image/buycardinfo.png" class="header_img2" style="width: 142px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<table id="con_table" cellpadding="5" style="text-align: center;margin-top: 10px;border-bottom: 1px solid #b5b5b5;width: 100%;padding-bottom: 10px;">
    <tr><td colspan="2" style="text-align: left;font-size: 20px;"><select id="type" class="text-input" style="width: 95%;height: 30px;" onblur="judge(3)"><option value="0" >玩家购钻查询</option><option value="1" selected="selected">代理售钻查询</option></select></td></tr>
    <tr>
        <td style="text-align: left"><input id="start_time" value="${requestScope.startDate}" type="date" placeholder="开始时间" style="width: 85%;height: 25px;"/></td>
        <td><input id="end_time" value="${requestScope.endDate}" type="date" placeholder="结束时间" style="width: 85%;height: 25px;"/></td>
    </tr>
     <tr id="tt1">
        <td style="text-align: left"><input id="playerId" type="text" placeholder="输入玩家ID" style="width: 85%;height: 25px;" onblur="judge(0)"/></td>
     </tr>
     <tr id="tt"><td colspan="2" style="text-align: left;font-size: 20px;"><select id="type2" class="text-input" style="width: 95%;height: 30px;" onblur="judge(3)"><option value="0" selected="selected">给玩家售钻</option><option value="2" >给代理售钻</option><option value="1" >向代理买钻</option></select></td></tr>
     <tr >
        <td style="text-align: left"  id="tt2"><input id="agencyId" type="text" placeholder="输入代理邀请码" onblur="judge(1)" style="width: 85%;height: 25px;"/></td>
        <td><input onclick="myQuery()" type="button" value="查询"/></td>
     </tr>
</table>
<div class="table2" style="margin-top: 2px;">
    <table id="table_data" style="width: 100%;text-align: center;font-size: 18px;" cellspacing="0" cellpadding="10">
        <tr style="color: #ee6a2a;font-size: 20px;">
            <td>ID</td>
            <td>昵称</td>
            <td>售钻代理</td>
            <td>数量</td>
            <td>时间</td>
        </tr>
    </table>
</div>
<div id="pp" style="margin-top: 2px;text-align: center" >
    <button  name="uppage"   value="上一页" onclick="uppage()">上一页 </button>
    <span id="page"></span>
    <button  name="downpage"    value="下一页" onclick="downpage()"> 下一页</button>
</div>
</body>
<script>
    var currentNo=0;
    var totalPage;
    var page = 1;
    var type = 0;
    $(document).ready(function () {
       $("#end_time").val(new Date().format("yyyy-MM-dd"));
       $("#start_time").val(new Date().format("yyyy-MM-dd"));
      // document.getElementById("agencyId").disabled=true;
       $("#tt").show();
       $("#tt1").hide();
       $("#tt2").show();
      // document.getElementById("playerId").disabled=false;
       
       myQuery();
    });
   function uppage()
    {
        page = page - 1;
        if(page <= 0){
            page = 1;
        }
        $("#page").html(page+"/"+totalPage);
        myQuery();
    }
    
    function judge(p){
       if(p==3){
         if($("#type").val()==0){
              $("#tt").hide();
              $("#tt1").show();
              $("#tt2").hide();
              $("#agencyId").val("");
         }else{
               $("#tt").show();
               $("#tt1").hide();
               $("#tt2").show();
              $("#playerId").val("");
         }
       }
    }
    
    function downpage()
    {
        page = page + 1;
        if(page > totalPage){
            page = totalPage;
        }
        $("#page").html(page+"/"+totalPage);
        myQuery();
    }
    function selectSub(num) {
        var temp=$("#agency_"+(0)).val();
        if(temp !=""){
           loadAgencies(1,temp.split("_")[1]);
           $("#agencyId2").css("display","");
        }else{
           $("#agencyId2").css("display","none");
        }
        
    }
    function myQuery() {
        $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/manage/buy/card/info",
            data: {gameId:localStorage.getItem('gameId'),pageNo:page,agencyId:$("#agencyId").val(),playerId:$("#playerId").val(), startDate:$("#start_time").val(),endDate:$("#end_time").val(),type:$("#type").val(),type2:$("#type2").val()},
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
                     $("#table_data").html('<tr style="color: #ee6a2a;font-size: 20px;"><td>ID</td><td>昵称</td><td>售钻代理</td><td>数量</td><td>时间</td></tr>');
                     $.each(result.datas,function (index,tempData){
                      	var name = "";
	                     if(tempData.name != null){
	                        name = tempData.name;
	                     }
                        var newRow="<tr><td>"+tempData.id+"</td><td>"+name+"</td><td>"+(tempData.agencyId)+"</td><td>"+tempData.cardNums+"</td><td>"+tempData.time+"</td></tr>";
                        $('#table_data').append(newRow);
                    });
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
</script>
</html>