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
        <td><img src="<%=basePath%>/image/dlzy.png" class="header_img2" style="width: 94px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div style="text-align: center;width: 100%;">
    <table cellpadding="0" cellspacing="15" style="width: 100%;">
        <tr align="center">
            <td>
                <input id="agencyId" class="text-input" style="width: 100%;" type="text" pattern="[0-9]*" placeholder="待转移邀请码" onblur="clearMsg()">
                <span style="display: block" id="agencyName">姓名：</span>
                <span style="display: block" id="agencyTel">手机号：</span>
            </td>
            <td>
                <input id="parentId" class="text-input" style="width: 100%;" type="text" pattern="[0-9]*" placeholder="转移后归属邀请码" onblur="clearMsg()">
                <span style="display: block" id="parentName">姓名：</span>
                <span style="display: block" id="parentTel">手机号：</span>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <input type="button" value="查询" onclick="loadInfo()">
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <input id="operate" style="display: none" type="button" value="转移" onclick="save()">
            </td>
        </tr>
    </table>
</div>

</body>

<script>

    var currentCount=0;
    var currentAgencyMessage,parentAgencyMessage;

    $(document).ready(function () {

    });

    function clearMsg() {
        $('#agencyName').html('姓名：');
        $("#agencyTel").html('手机号：');
        $('#parentName').html('姓名：');
        $("#parentTel").html('手机号：');
        $('#operate').css("display","none");
    }

    function loadInfo(){
        currentCount=0;
        var agencyId = $("#agencyId").val().trim();
        if (agencyId==""||isNaN(agencyId)){
            alert("请输入待转移邀请码");
            return;
        }
        var parentId = $("#parentId").val().trim();
        if (parentId==""||isNaN(parentId)){
            alert("请输入转移后归属邀请码");
            return;
        }

        if (agencyId==parentId){
            alert("错误的邀请码");
            return;
        }

        loadAgencyInfo("agency",agencyId);
        loadAgencyInfo("parent",parentId);
    }

    function loadAgencyInfo(pre,id) {
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/agency/info",
            data: {
                agencyId:id,gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if(result.code==1000){
                    currentCount++;
                    var dataMsg=result.info;

                    if ("agency"==pre){
                        currentAgencyMessage=dataMsg;
                    }else if ("parent"==pre){
                        parentAgencyMessage=dataMsg;
                    }

                    var myName="[未填写]";
                    if(dataMsg.hasOwnProperty("userName")){
                        myName=dataMsg.userName;
                    }

                    $('#'+pre+'Name').html("姓名："+myName);

                    $('#'+pre+'Tel').html("手机号："+dataMsg.agencyPhone);
                    $('#'+pre+'Tel').css("display","block");

                    if (currentCount>=2&&(!currentAgencyMessage.hasOwnProperty("parentId")||currentAgencyMessage.parentId!=parentAgencyMessage.userId)&&currentAgencyMessage.userId!=parentAgencyMessage.userId){
                        $("#operate").css("display","");
                    }
                }else{
                    $('#'+pre+'Tel').html("手机号：无");
                    $('#'+pre+'Id').focus();
                    $('#'+pre+'Name').html(result.message);
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
            alert("请输入待转移邀请码");
            return;
        }
        var parentId = $("#parentId").val().trim();
        if (parentId==""||isNaN(parentId)){
            alert("请输入转移后归属邀请码");
            return;
        }

        var temp1="";
        if(currentAgencyMessage.hasOwnProperty("userName")){
            temp1="("+currentAgencyMessage.userName+")";
        }

        var temp2="";
        if(parentAgencyMessage.hasOwnProperty("userName")){
            temp2="("+parentAgencyMessage.userName+")";
        }

        var a=confirm("确定要将"+agencyId+temp1+"的上级变更为"+parentId+temp2+"吗？");
        if (a==true)
            $.ajax({
                timeout: 60000,
                async: true,
                type: "POST",
                url: "<%=path%>/manage/agency/transfer",
                data: {
                    agencyId:agencyId,
                    parentId:parentId,gameId:localStorage.getItem('gameId')
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
                        $("#parentId").val("");
                        $('#parentName').html("姓名:");
                        $('#parentTel').css("display","none");
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