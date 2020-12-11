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
        .icon-input{
            position:absolute;
            /*left:0;*/
            z-index:5;
            /*background-image:url("<%=basePath%>/image/phone.png"); !*引入图片图片*!*/
            background-repeat:round; /*设置图片不重复*/
            background-position:0px 0px; /*图片显示的位置*/
            width:20px; /*设置图片显示的宽*/
            height:20px; /*图片显示的高*/
            margin-top: 8px;
            margin-left: 3px;
        }
        .text-input{
            padding-left:25px;height:30px;width: 70%;color: #0d6cac;font-size: 16px;
        }
    </style>
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.0">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="<%=basePath%>/js/md5.js"></script>
</head>

<body onresize="resize()" style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<table class="header_table" cellpadding="0" cellspacing="0">
    <tr>
        <td class="fix_td" onclick="history.back()"><img class="header_img1" src="<%=basePath%>/image/back.png"/></td>
        <td><img src="<%=basePath%>/image/wx.png" class="header_img2" style="width: 120px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div style="text-align: center;width: 100%;">
    <table cellpadding="0" cellspacing="15" style="width: 100%;">
        <%--<tr>--%>
        <%--<td>--%>
        <%--<input id="pwd_old" class="text-input" type="password" placeholder="密码">--%>
        <%--</td>--%>
        <%--</tr>--%>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="wx1" class="text-input" type="text" placeholder="微信号1">
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="wx2" class="text-input" type="text" placeholder="微信号2">
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="wx3" class="text-input" type="text" placeholder="微信号3">
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="wx4" class="text-input" type="text" placeholder="微信号4">
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="wx5" class="text-input" type="text" placeholder="微信号5">
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="wx6" class="text-input" type="text" placeholder="微信号6">
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="wx7" class="text-input" type="text" placeholder="微信号7">
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="wx8" class="text-input" type="text" placeholder="微信号8">
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="wx9" class="text-input" type="text" placeholder="微信号9">
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="wx10" class="text-input" type="text" placeholder="微信号10">
            </td>
        </tr>
    <%--     <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="wx11" class="text-input" type="text" placeholder="微信号11">
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="wx12" class="text-input" type="text" placeholder="微信号12">
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="wx13" class="text-input" type="text" placeholder="微信号13">
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="wx14" class="text-input" type="text" placeholder="微信号14">
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="wx15" class="text-input" type="text" placeholder="微信号15">
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="wx16" class="text-input" type="text" placeholder="微信号16">
            </td>
        </tr>
        <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/lock.png');"></i>
                <input id="wx17" class="text-input" type="text" placeholder="微信号17">
            </td>
        </tr> --%>
        <tr>
            <td>
                <img src="<%=basePath%>/image/submit.png" style="width: 153px;height: 50px;border: none;margin-top: 25px;"  onclick="save()"/>
            </td>
        </tr>
    </table>
</div>

</body>

<script>

    $(document).ready(function () {
        loadTelCode();
    });
    function loadTelCode() {
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/query/vip/wx",
            data: {gameId:localStorage.getItem("gameId")},
            dataType: "json",
            success: function (result) {
                if (result.code==1000){
                    $.each(result.data,function (index,tempData){
                        $("#wx"+tempData.keyId).val(tempData.weixinName);
                    })
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
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/vip/wx",
            data: {
                wx1:$("#wx1").val(),
                 wx2:$("#wx2").val(),
                  wx3:$("#wx3").val(),
                   wx4:$("#wx4").val(),
                    wx5:$("#wx5").val(),
                     wx6:$("#wx6").val(),
                      wx7:$("#wx7").val(),
                       wx8:$("#wx8").val(),
                        wx9:$("#wx9").val(),
                         wx10:$("#wx10").val(),
                        /*   wx11:$("#wx11").val(),
                           wx12:$("#wx12").val(),
                            wx13:$("#wx13").val(),
                             wx14:$("#wx14").val(),
                              wx15:$("#wx15").val(),
                               wx16:$("#wx16").val(),
                                wx17:$("#wx17").val(), */
//                pwd:pwd_old
            },
            dataType: "json",
            success: function (result) {
                if (result.code == 1000) {
                    alert(result.message);
                } else {
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

</script>
</html>