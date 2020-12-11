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
        <td><img src="<%=basePath%>/image/sq.png" class="header_img2" style="width: 94px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div style="text-align: center;width: 100%;">
    <table cellpadding="0" cellspacing="15" style="width: 100%;">
        <tr>
            <td>
                <input id="groupId" class="text-input" type="text" pattern="[0-9]*" placeholder="俱乐部ID">
            </td>
            <td><input onclick="loadUserInfo()" type="button" value="查询"/></td>
        </tr>
         <tr>
            <td >
                <input id="playerName" class="text-input" type="text"  placeholder="俱乐部名称" disabled="disabled">
            </td>
            <td></td>
        </tr>
        <tr>
            <td >
                <input id="groupState" class="text-input" type="text"  placeholder="当前群主ID" disabled="disabled">
            </td>
            <td></td>
        </tr>
         <tr>
            <td >
                <input id="descMsg" class="text-input" type="text"  placeholder="当前群主名称" disabled="disabled">
            </td>
            <td></td>
        </tr>
         <tr>
            <td >
                <input id="NowPlayerId" class="text-input" type="text"  placeholder="创建者ID" disabled="disabled">
            </td>
            <td></td>
        </tr>
         <tr>
            <td >
                <input id="NowPlayerName" class="text-input" type="text"  placeholder="创建者名称" disabled="disabled">
            </td>
            <td></td>
        </tr>
        <tr id="t1">
            <td >
                <input id="playerId" class="text-input" type="text" pattern="[0-9]*" placeholder="群主ID">
            </td>
            <td></td>
        </tr>
        <tr id="t2">
            <td >
                <input id=""  type="button" value="设置" onclick="save(0)">
            </td>
            <td></td>
        </tr>
    </table>
</div>
<!-- <div id="tt" style="margin-top: 2px;text-align: left" >
        <span id="xs" style="color: red;font-size: 15px;"> &nbsp&nbsp 请注意：<br/><br/>&nbsp&nbsp 1、俱乐部上限200人。<br/>&nbsp&nbsp 2、请谨慎添加成员，当成员出现纠纷时，客服将联系该群主。
   </div> -->
</body>

<script>
    var myOperate=1;//1添加红名2解除红名
    $(document).ready(function () {
    	$("#t1").hide();
        $("#t2").hide();
    });

    function switchGame() {
        clearMsg();
    }

    function clearMsg() {
        $('#playerName').html('');
        $("#myIco").css("display","none");
        $('#operate').css("display","none");
    }

    function loadUserInfo() {
        var groupId = $("#groupId").val().trim();
        if (groupId==""||isNaN(groupId)){
            alert("请输入俱乐部ID");
            return;
        }
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/querygroupbyId",
            data: {
                groupId:$("#groupId").val(),gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if(result.code==1000){
                	$('#playerName').val(result.data.groupName);
                	$('#NowPlayerName').val(result.data.lastGroupName);
                	$('#NowPlayerId').val(result.data.createdUser);
                	$('#descMsg').val(result.data.descMsg);
                	$('#groupState').val(result.data.groupState);
                	$("#t1").show();
                    $("#t2").show();
                }else{
                    $("#groupId").focus();
                    $("#t1").hide();
                    $("#t2").hide();
                    $("#groupId").focus();
                    $('#playerName').html(result.message);
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

    function save(type) {

        var playerId = $("#playerId").val().trim();

        if (playerId==""||isNaN(playerId)){
            alert("请输入玩家ID");
            return;
        }
        var a;
         a=confirm("确定更换【"+playerId+"】为群主吗?");
        //var a=confirm("确定设置【"+playerId+"】为群主吗?");
        if (a==true)
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/changeGroupManage",
            data: {
                playerId:playerId,
                groupId:$("#groupId").val(),
                gameId:$("#gameId").val(),gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                  $("#t1").hide();
                  $("#t2").hide();
                /* if(result.code==1000){
                    $("#myIco").css("display","none");
                    $("#cx").show();
                    $("#playerId").val("");
                    $("#playerId").focus();
                    $('#playerName').html("");
                    $('#operate').css("display","none");
                    $('#operate2').css("display","none");
                    
                } */
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

</script>
</html>