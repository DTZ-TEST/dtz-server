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
    <title>快乐玩游戏代理后台_快乐打筒子_快乐跑得快_快乐跑胡子</title>

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
        <td><img src="<%=basePath%>/image/dlsj.png" class="header_img2" style="width: 94px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div style="text-align: center;width: 100%;">
    <table cellpadding="0" cellspacing="15" style="width: 100%;">
        <tr align="center">
            <td>
                <input id="agencyId" class="text-input" type="text" pattern="[0-9]*" placeholder="邀请码" onblur="clearMsg()">
                <span style="display: block" id="agencyName">姓名：</span>
                <span style="display: block" id="agencyTel">手机号：</span>
            </td>
        </tr>
        <tr>
            <td>
                <input type="button" value="查询" onclick="loadInfo()">
            </td>
        </tr>
        <tr>
            <td>
                <input id="operate" style="display: none" type="button" value="升级" onclick="save()">
            </td>
        </tr>
    </table>
</div>

</body>

<script>
    $(document).ready(function () {

    });

    function clearMsg() {
        $('#agencyName').html('姓名：');
        $("#agencyTel").html('手机号：');
        $('#operate').css("display","none");
    }

    function loadInfo() {
        var agencyId = $("#agencyId").val().trim();
        if (agencyId==""||isNaN(agencyId)){
            alert("请输入邀请码");
            return;
        }

        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/agency/info",
            data: {
                agencyId:agencyId,gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if(result.code==1000){
                    var dataMsg=result.info;
                    var myName="[未填写]";
                    if(dataMsg.hasOwnProperty("userName")){
                        myName=dataMsg.userName;
                    }

                    $('#agencyName').html("姓名："+myName);

                    $('#agencyTel').html("手机号："+dataMsg.agencyPhone);
                    $("#agencyTel").css("display","block");
                    if(!dataMsg.hasOwnProperty("agencyLevel")||dataMsg.agencyLevel==""||dataMsg.agencyLevel==0){
                        $("#operate").val("升级为特级代理");
                        $("#operate").css("display","");
                    }else if(dataMsg.agencyLevel==1){
                        $("#operate").val("升级为顶级代理");
                        $("#operate").css("display","");
                    }
                }else{
                    $("#agencyTel").css("display","none");
                    $("#agencyId").focus();
                    $('#agencyName').html(result.message);
                    $('#operate').css("display","none");
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
        var agencyId = $("#agencyId").val().trim();

        if (agencyId==""||isNaN(agencyId)){
            alert("请输入邀请码");
            return;
        }

        var a=confirm("确定进行升级操作吗？");
        if (a==true)
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/manage/agency/improve",
            data: {
                agencyId:agencyId,gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json

                if(result.code==1000){
                    $("#agencyId").val("");
                    $("#agencyId").focus();
                    $('#agencyName').html("姓名:");
                    $('#agencyTel').css("display","none");
                    $('#operate').css("display","none");
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

</script>
</html>