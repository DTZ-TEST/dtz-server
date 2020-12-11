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
        <td><img src="<%=basePath%>/image/whitemenu.png" class="header_img2" style="width: 120px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div style="text-align: center;width: 100%;">

    <table cellpadding="0" cellspacing="15" style="width: 100%;">
     	<!-- <tr>
            	<td>
                	<input id="groupId" class="text-input" type="text" pattern="[0-9]*" placeholder="俱乐部ID">
            	</td>
            	<td><input onclick="loadUserInfo()" type="button" value="查询"/></td>
       	</tr> -->
       	 <tr>
            <td>
                <span style="display: block"/>
                <input id="groupId" class="text-input" style="margin-top: 15px;" type="text" pattern="[0-9]*" placeholder="添加白名单">
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
                <input id="descMsg" class="text-input" type="text"  placeholder="当前群主名称" disabled="disabled">
            </td>
            <td></td>
        </tr>
       
        <tr>
            <td>
                <input type="button" value="保存" onclick="save()">
            </td>
        </tr>
    </table>
</div>
<input type="hidden" id="admin" value="${sessionScope.roomCard.partAdmin}"/>
<input type="hidden" id="me" value="${sessionScope.roomCard.agencyId}"/>
<input type="hidden" id="agencyLevel" value="${sessionScope.roomCard.agencyLevel}"/>
</body>

<script>

    $(document).ready(function () {
        loadAgencies();
    });

    function changeAgencyId() {
        var agencyId = $("#myAgency").val().trim();

        if (agencyId==""||isNaN(agencyId)){
            $("#agencyId").val("");
            $("#agencyId").removeAttr("disabled");
        }else{
            $("#agencyId").val(agencyId);
            $("#agencyId").attr("disabled","disabled");
        }
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
    
    function loadAgencies() {
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/agency/info/list",
            data: {
                gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {
                if(result.code==1000){
                    $("#myAgency").html('<option value="">请选择代理商</option>');
                    $.each(result.datas,function (index,tempData){
                        var temp;
                        if (tempData.userName){
                            temp=tempData.userName;
                        }else{
                            temp=tempData.agencyPhone;
                        }
                        var newRow='<option value="'+tempData.agencyId+'">'+tempData.agencyId+'('+temp+')</option>';
                        $('#myAgency').append(newRow);
                    });
                }else
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

    function save() {
     var a=confirm("您确定要添加白名单为【"+$("#groupId").val()+"】的信息吗？");
       if (a!=true)return;
       
		var   b=   $("#playerName").val();
		if(b.length==0){
			alert("俱乐部不存在");
			return;
		}
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/whitemenu",
            data: {
                 agencyId:$("#groupId").val(),
            },
            dataType: "json",
            success: function (result) {
                if (result.code!=1000){
                    alert(result.message);
                }else{
                    alert(result.message);
                    window.location.reload();
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

</script>
</html>