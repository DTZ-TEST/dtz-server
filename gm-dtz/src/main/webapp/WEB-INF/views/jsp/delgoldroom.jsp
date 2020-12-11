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
        <td><img src="<%=basePath%>/image/ur.png" class="header_img2" style="width: 120px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<table cellpadding="5"
       style="text-align: center;margin-top: 10px;border-bottom: 1px solid #b5b5b5;width: 100%;padding-bottom: 10px;">
    <tr>
        <td style="text-align: left"><input id="roomId" type="text" pattern="[0-9]*" placeholder="玩家ID" style="width: 85%;height: 25px;"/></td>
        <td><input onclick="loadUserInfo()" type="button" value="查询"/></td>
    </tr>
     <tr>
        <td style="text-align: left" colspan="2"><input id="time" type="text"  placeholder="桌位号" style="width: 85%;height: 25px;" disabled="disabled"/></td>
    </tr>
</table>
<div  class="table2" style="margin-top: 2px;">
    <table style="width: 100%;text-align: center;font-size: 18px;" cellspacing="0" cellpadding="10" id="info">
    </table>
</div>
<div id="operateCredit" style="text-align: center;width: 100%;display: none;">
    <table style="width: 100%;border-top:0;border-bottom:0;" cellspacing="0" cellpadding="5">
        <tr align="center">
            <td style="text-align: center;"><input id="resetScore" type="button" value="解散" onclick="save()"  style="width:70px;height:50px;color:blue;" ></td>
        </tr>
    </table>
</div> 
<input type="hidden" id="flag" value="1"/>
</body>

<script>

    $(document).ready(function () {
         $("#operateCredit").hide();
    });
    function selectvalue(index) {
        $("#playerName").val("");
        var userId = "userId"+index;
        var nameId = "name"+index;
        var userIds = $("#"+userId).html();
        var name = $("#"+nameId).html();
        $("#playerId").val(userIds);
        $("#playerName").val(name);
        $("#count").val("");
        $("#playerCards").val("");
        loadUserInfo();
    }
    
    function loadUserInfo() {

        var roomId = $("#roomId").val().trim();
        if (roomId==""||isNaN(roomId)){
            $("#count").focus();
            alert("请输入玩家ID");
            return;
        }

        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/querygoldroom",
            data: {
            	userid:roomId,
               // gameId:localStorage.getItem("gameId")
            },
            dataType: "json",
            success: function (result) {
                if(result.code==1000){
                    var dataMsg=result.info;
                /*     var flag = result.flag;
                    $('#info').html("");
                 //   $('#flag').val(flag);
                    var newRow='<tr> <td>游戏ID</td> <td >昵称</td><td >绑定码</td><td >开房时间</td>';
                    var tempTime;
                        if (!isNaN(dataMsg.createTime)){
                            var tmpDate=new Date();
                            tmpDate.setTime(dataMsg.createTime);
                            tempTime=tmpDate.format("yyyy-MM-dd hh:mm:ss");
                        }else{
                            tempTime=dataMsg.createTime;
                        } */
                    $('#time').val(dataMsg);
                  /*   $.each(dataMsg.userList,function (index,tempData){
                       newRow +='<tr> <td>'+tempData.userId+'</td> <td >'+tempData.name+'</td><td >'+tempData.payBindId+'</td><td >'+tempTime+'</td>';
                    });
                    $('#info').append(newRow); */
                    $("#operateCredit").show();
                }else{
                     alert(result.message);
                     $("#operateCredit").hide();
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

    function save() {

        var roomId = $("#time").val().trim();
        if (roomId==""||isNaN(roomId)){
            $("#count").focus();
            alert("请输入房间ID");
            return;
        }
		if(!roomId.length==8){
			alert("解散房间类型不符合");
			return;
		}
		if(roomId==0){
			alert("该玩家不在房间内,无需解散");
			return;
		}
        var a=confirm("您确认要解散房间<"+roomId+">吗?");
        if (a!=true) return;
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/delgoldroom",
            data: {
            	playingTableId:roomId
            },
            dataType: "json",
            success: function (result) {
                  alert(result.message);
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