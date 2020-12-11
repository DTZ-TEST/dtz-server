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
        <td><img src="<%=basePath%>/image/hdcx.png" class="header_img2" style="width: 142px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<table id="con_table" cellpadding="5" style="text-align: center;margin-top: 10px;border-bottom: 1px solid #b5b5b5;width: 100%;padding-bottom: 10px;">
   <tr>
        <td style="text-align: left"><input id="playerId"  type="text" placeholder="玩家id" style="width: 85%;height: 25px;"/></td>
        <td><input onclick="myQuery()" type="button" value="查询"/></td>
    </tr>
</table>
<div class="table2" style="margin-top: 2px;">
    <table id="table_data" style="width: 100%;text-align: center;font-size: 18px;" cellspacing="0" cellpadding="10">
        <tr style="color: #ee6a2a;font-size: 20px;">
            <td>游戏ID</td>
            <td>昵称</td>
            <td>奖励</td>
            <td>操作</td>
        </tr>
    </table>
</div>
<div id="pp" style="margin-top: 2px;text-align: center" >
    <button  name="uppage"   value="上一页" onclick="uppage()">上一页 </button>
    <span id="page"></span>
    <button  name="downpage"    value="下一页" onclick="downpage()"> 下一页</button>
   <input id="type" value="${requestScope.type}" type="hidden" />
      <input id="start" value="${requestScope.start}" type="hidden" />
   <input id="end" value="${requestScope.end}" type="hidden" />
   <input type="hidden" id="agencyId" value="${sessionScope.roomCard.agencyId}"/>
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
        if(temp !="" && document.getElementById("agency_0").value !="cd"){
           loadAgencies(1,temp.split("_")[1]);
              $("#agencyId2").css("display","");
        }else{
           $("#agencyId2").css("display","none");
        }
        
    }

    function myQuery() {
        var agencyId=$("#playerId").val();
        $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/activityreward",
            data: {pageNo:page,playerId:agencyId,gameId:localStorage.getItem('gameId')},
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
                     $("#table_data").html('<tr style="color: #ee6a2a;font-size: 20px;"><td>游戏ID</td><td>昵称</td><td>奖励</td><td>操作</td></tr>');
                     $.each(result.datas,function (index,tempData){
                       var s = "已领取";
                       /*  if(tempData.type==2 && tempData.state==1){
                           s = "bi'jia";
                        } */
                        var newRow;
                        if(tempData.type==1){
                            newRow="<tr><td>"+tempData.userId+"</td><td>"+tempData.userName+"</td><td>"+tempData.rewardNum+"钻石</td><td></td></tr>";
                        }else if(tempData.state==2){
                            newRow="<tr><td>"+tempData.userId+"</td><td>"+tempData.userName+"</td><td>"+tempData.rewardNum+"元红包</td><td></td></tr>";
                        }else{
                            newRow="<tr><td>"+tempData.userId+"</td><td>"+tempData.userName+"</td><td>"+tempData.rewardNum+"元红包</td><td><a onclick=fn("+tempData.keyId+")><span style='color: blue;font-size: 15px;'>标记已领</span></a></td></tr>";
                        }
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
       <%--  $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/activityreward",
            data: {pageNo:page,playerId:playerId},
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
                     $("#table_data").html('<tr style="color: #ee6a2a;font-size: 20px;"><td>游戏ID</td><td>昵称</td><td>奖励</td><td>状态</td><td>操作</td></tr>');
                     $.each(result.datas,function (index,tempData){
                        var s = "已领取";
                        if(tempData.type==2 && tempData.state==2){
                           s = "未领取";
                        }
                        if(tempData.type==1){
                            var newRow="<tr><td>"+tempData.userId+"</td><td>"+tempData.userName+"</td><td>"+tempData.rewardNum+"钻石</td><td>"+s+"</td><td></td></tr>";
                        }else if(tempData.state==2){
                            var newRow="<tr><td>"+tempData.userId+"</td><td>"+tempData.userName+"</td><td>"+tempData.rewardNum+"元红包</td><td>"+s+"</td><td></td></tr>";
                        }else{
                            var newRow="<tr><td>"+tempData.userId+"</td><td>"+tempData.userName+"</td><td>"+tempData.rewardNum+"元红包</td><td>"+s+"</td><td><a onclick=fn("+tempData.keyId+")><span style='color: blue;font-size: 15px;'>领取</span></a></td></tr>";
                        }
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
        }); --%>
    }
   function fn(data){
      $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/updateactivityreward",
            data: {id:data,gameId:localStorage.getItem('gameId')},
            dataType: "json",
            success: function(result){
                if (result.code==1000){
                    alert("领取成功");
                    myQuery();
                }else{
                    alert("领取成功");
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