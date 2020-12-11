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
        <td><img src="<%=basePath%>/image/user_forbid.png" class="header_img2" style="width: 94px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<table cellpadding="5"
       style="text-align: center;margin-top: 10px;border-bottom: 1px solid #b5b5b5;width: 100%;padding-bottom: 10px;">
    <tr>
        <td style="text-align: center;width: 70%"><input id="agencyId" type="text" pattern="[0-9]*"
                                                         placeholder="代理商邀请码" style="width: 85%;height: 25px;"/></td>
        <td width="20%"><input onclick="loadUserInfo()" type="button" value="查询"/></td>
    </tr>
</table>
<div class="table2" style="margin-top: 2px;">
    <table id="table_data" style="width: 100%;text-align: center;font-size: 18px;" cellspacing="0" cellpadding="10">
        <tr>
            <td>手机号</td>
            <td id="phone">--</td>
        </tr>
        <tr>
            <td>微信号</td>
            <td id="wx">--</td>
        </tr>
        <tr>
            <td>姓名</td>
            <td id="username">--</td>
        </tr>
        <tr>
            <td>当前钻石</td>
            <td id="cards">--</td>
        </tr>
        <tr>
            <td>VIP等级</td>
            <td id="vip">--</td>
        </tr>
        <tr>
            <td>注册时间</td>
            <td id="regtime">--</td>
        </tr>

    </table>
</div>
<div id="pp" style="margin-top: 10px;text-align: center" >
     <input id="jz" onclick="save(1)" type="button" value="禁止登录"/>
      <input id="jc" onclick="save(0)" type="button" value="禁止解除"/>
</div>
</body>

<script>
    var myOperate=1;//1添加红名2解除红名
    
    $(document).ready(function () {
       $("#pp").hide();
    });
    function loadUserInfo() {
         $("#phone").html('--');
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
                        $("#pp").show();
                        var agency = result.message;
                        if (agency.agencyPhone) {
                            $("#phone").html(agency.agencyPhone);
                        }
                        if (agency.agencyWechat) {
                            $("#wx").html(agency.agencyWechat);
                        }
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
                      var type = result.type;
                    if(type==0){
	                   $("#jz").show();
	                   $("#jc").hide();
                    }else{
	                   $("#jc").show();
	                   $("#jz").hide();
                    }
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

    function save(type) {
        var agencyId = $("#agencyId").val().trim();

        if (agencyId==""||isNaN(agencyId)){
            alert("请输入代理邀请码");
            return;
        }

        var a;
        if(type == 1){
          a=confirm("确认禁止该代理【"+agencyId+"】登录吗");
        }else{
          a=confirm("确认解除禁止代理【"+agencyId+"】吗");
        }
        if (a==true)
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/agency/forbid",
            data: {
                agencyId:agencyId,type:type,gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {
                 if(type==0){
                   $("#jz").show();
                   $("#jc").hide();
                 }else{
                   $("#jc").show();
                   $("#jz").hide();
                 }
                 alert(result.message);

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