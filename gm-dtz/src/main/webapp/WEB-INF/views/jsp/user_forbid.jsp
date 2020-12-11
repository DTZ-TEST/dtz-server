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
<div style="text-align: center;width: 100%;">
    <table cellpadding="0" cellspacing="15" style="width: 100%;">
        <%--<tr align="center">--%>
            <%--<td>--%>
            <%--<select id="gameId" style="width: 66%;font-size: 16px;height: 32px;" onchange="clearMsg()">--%>
                <%--<option value="" selected="selected">请选择游戏</option>--%>
                <%--<option value="1">小甘牛牛</option>--%>
                <%--&lt;%&ndash;<option value="2">牛魔王</option>&ndash;%&gt;--%>
                <%--<option value="3">小甘十点半</option>--%>
                <%--&lt;%&ndash;<option value="4">扬沙子</option>&ndash;%&gt;--%>
                <%--&lt;%&ndash;<option value="5">挖坑</option>&ndash;%&gt;--%>
            <%--</select>--%>
            <%--</td>--%>
         <%--</tr>--%>
        <tr align="center">
            <td>
                <input id="playerId" class="text-input" type="text" pattern="[0-9]*" placeholder="玩家ID" onblur="clearMsg()">
                <span style="display: block" id="playerName"></span>
                <img id="myIco" style="width: 90px;height: 90px;display: none;"/>
            </td>
        </tr>
        <tr>
            <td>
                <input type="button" value="查询" onclick="loadUserInfo()">
            </td>
        </tr>
        <tr>
            <td>
                <input id="operate" style="display: none" type="button" value="添加" onclick="save()">
            </td>
        </tr>
    </table>
</div>

</body>

<script>
    var myOperate=1;//1添加红名2解除红名
    $(document).ready(function () {

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
        var playerId = $("#playerId").val().trim();
        if (playerId==""||isNaN(playerId)){
            alert("请输入玩家ID");
            return;
        }

        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/player/info",
            data: {
                userId:playerId,
                gameId:localStorage.getItem("gameId")
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if(result.code==1000){
                    var dataMsg=result.info;
                    var myName;
                    if(dataMsg.hasOwnProperty("name")){
                        myName=dataMsg.name;
                    }else{
                        myName=dataMsg.userId;
                    }
                    if(dataMsg.hasOwnProperty("headimgurl")&&$.trim(dataMsg.headimgurl).length>0){
                        $("#myIco").attr("src",dataMsg.headimgurl);
                        $("#myIco").css("display","block");
                    }else{
                        $("#myIco").css("display","none");
                    }

                    if(dataMsg.hasOwnProperty("userState")){
                        if(dataMsg.userState>0){
                            myOperate=1;
                        }else{
                            myOperate=2;
                        }
                    }else{
                        myOperate=1;
                    }

                    if (myOperate==1){
                        $("#operate").val("添加");
                    }else{
                        $("#operate").val("解除");
                    }

                    $('#playerName').html("微信昵称:"+myName);
                    $('#operate').css("display","");
                }else{
                    $("#myIco").css("display","none");
                    $("#playerId").focus();
                    $('#playerName').html(result.message);
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

        var playerId = $("#playerId").val().trim();

        if (playerId==""||isNaN(playerId)){
            alert("请输入充值ID");
            return;
        }

        var a=myOperate!=2?confirm("确认禁止该玩家登录吗?"):confirm("确定将该玩家移出吗?");
        if (a==true)
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/state",
            data: {
                playerId:playerId,
                type:"forbid",
                userState:myOperate,
                gameId:localStorage.getItem("gameId")
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json

                if(result.code==1000){
                    $("#myIco").css("display","none");
                    $("#playerId").val("");
                    $("#playerId").focus();
                    $('#playerName').html("");
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