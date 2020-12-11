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
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.2">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
</head>

<body style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<table class="header_table" cellpadding="0" cellspacing="0">
    <tr>
        <td class="fix_td" onclick="history.back()"><img class="header_img1" src="<%=basePath%>/image/back.png"/></td>
        <td><img src="<%=basePath%>/image/agency_info.png" class="header_img2" style="width: 141px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3"
                                                                                src="<%=basePath%>/image/home.png"/>
        </td>
    </tr>
</table>
<table cellpadding="5"
       style="text-align: center;margin-top: 10px;border-bottom: 1px solid #b5b5b5;width: 100%;padding-bottom: 10px;">
    <tr>
        <td style="text-align: center;width: 70%"><input id="agencyId" type="text" pattern="[0-9]*"
                                                         placeholder="代理商邀请码" style="width: 85%;height: 25px;"/></td>
        <td width="20%"><input onclick="myQuery()" type="button" value="查询"/></td>
    </tr>
</table>

<div class="table2" style="margin-top: 2px;">
    <table id="table_data" style="width: 100%;text-align: center;font-size: 18px;" cellspacing="0" cellpadding="10">
        <!-- <tr>
            <td>手机号</td>
            <td id="phone">--</td>
             <td></td>
        </tr> -->
       <!--  <tr>
            <td>微信号</td>
            <td id="wx">--</td>
             <td></td>
        </tr> -->
        <tr>
            <td>姓名</td>
            <td id="username">--</td>
             <td></td>
        </tr>
        <tr>
            <td>当前钻石</td>
            <td id="cards">--</td>
             <td></td>
        </tr>
        <tr>
            <td>VIP等级</td>
            <td id="vip">--</td>
             <td></td>
        </tr>
        <tr>
            <td>注册时间</td>
            <td id="regtime">--</td>
             <td></td>
        </tr>
       <tr>
            <td>上级代理</td>
            <td id="parentAgencyId">--</td>
            <td width="20%"><input onclick="agencyInfo()" type="button" value="详情" id="xq"/></td>
        </tr>
    </table>
</div>
<div id="pp1" style="margin-top: 10px;text-align: center" >
     <input onclick="palyer()" type="button" value="查看绑码玩家"/>
</div>
 <div class="table2" style="margin-top: 2px;">
    <table id="table_data2" style="width: 100%;text-align: center;font-size: 18px;" cellspacing="0" cellpadding="10">
        <tr style="color: #ee6a2a;font-size: 20px;">
            <td>ID</td>
            <td>昵称</td>
            <td>绑码时间</td>
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
    var totalPage;
    var page = 1;
    $(function(){
         $("#xq").hide();
         $("#pp").hide();
         $("#table_data2").hide();
     })
    function agencyInfo() {
       var agencyId = $("#parentAgencyId").html();
       $("#agencyId").val(agencyId);
       myQuery();
    }
    
    function uppage()
    {
      page = page - 1;
      if(page <= 0){
         page = 1;
      }
      $("#page").html(page+"/"+totalPage);
      palyer();
    }
    
    function downpage()
    {
      page = page + 1;
      if(page > totalPage){
         page = totalPage;
      }
       $("#page").html(page+"/"+totalPage);
       palyer();
    }
    function palyer() {
      $("#table_data2").show();
       $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/agency/players",
            data: {agencyId:$("#agencyId").val(),page:page,gameId:localStorage.getItem('gameId')},
            dataType: "json",
            success: function(result, status, request){
                if (result.code==1000){
                    totalPage = result.page;
                    if(totalPage <= 1){
                      $("#pp").hide();
                      page=1;
                    }else{
                      $("#pp").show();
                    }
                     $("#page").html(page+"/"+totalPage);
                    $("#table_data2").html('<tr style="color: #ee6a2a;font-size: 20px;"><td>ID</td><td>昵称</td><td>绑码时间</td></tr>');
                    $.each(result.datas,function (index,tempData){
                        var userId = "userId"+index;
                        var nameId = "nameId"+index;
                        var tmpDate = new Date();
                             tmpDate.setTime(tempData.payBindTime);
                             tmpDate = tmpDate.format("yyyy-MM-dd hh:mm:ss");
                        var newRow="<tr  onclick=selectvalue("+index+")><td id='"+userId+"'>"+tempData.userId+"</td><td id='"+nameId+"'>"+tempData.name+"</td><td>"+tmpDate+"</td></tr>";
                        $('#table_data2').append(newRow);
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
    function myQuery() {
        $("#xq").hide();
        $("#table_data2").hide();
        $("#pp").hide();
        /* $("#phone").html('--'); */
        $("#username").html('--');
        $("#cards").html('--');
        $("#vip").html('--');
        $("#regtime").html('--');

        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/agency/query",
            data: {
                agencyId: $("#agencyId").val(),gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {
                if (result.code == 1000) {
                    if (result.message) {
                        var agency = result.message;
                        /* if (agency.agencyPhone) {
                            $("#phone").html(agency.agencyPhone);
                        } */
                        /*  if (agency.agencyWechat) {
                            $("#wx").html(agency.agencyWechat);
                        } */
                        if(agency.userName){
                            $("#username").html(agency.userName);
                        }
                        if(agency.commonCard){
                            $("#cards").html(agency.commonCard);
                        }
                        if(agency.vip){
                            $("#vip").html(agency.vip);
                        }
                        if(agency.createTime){
                            var tmpDate = new Date();
                            tmpDate.setTime(agency.createTime);
                            tmpDate = tmpDate.format("yyyy-MM-dd hh:mm:ss");
                            $("#regtime").html(tmpDate)
                        }

                    }
                    if(result.parentAgencyId){
                        if(result.parentAgencyId =="无"){
                           $("#xq").hide();
                        }else{
                           $("#xq").show();
                        }
                        $("#parentAgencyId").html(result.parentAgencyId);
                    }
                } else {
	                var auth_url = req.getResponseHeader("REQUIRES_AUTH_URL");
	                if (auth_url) {
	                    window.location.href = auth_url;
	                } 
	                $("#parentAgencyId").html("");
	                $("#wx").html("");
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
</html>