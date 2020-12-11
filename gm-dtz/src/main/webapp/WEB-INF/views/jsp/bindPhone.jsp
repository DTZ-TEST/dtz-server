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
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.1">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
</head>

<body style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<table class="header_table" cellpadding="0" cellspacing="0">
    <tr>
        <td class="fix_td" onclick="history.back()"><img class="header_img1" src="<%=basePath%>/image/back.png"/></td>
        <td><img src="<%=basePath%>/image/chaxun.png" class="header_img2" style="width: 142px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img
                class="header_img3"
                src="<%=basePath%>/image/home.png"/>
        </td>
    </tr>
</table>
<table cellpadding="5"
       style="text-align: center;margin-top: 10px;border-bottom: 1px solid #b5b5b5;width: 100%;padding-bottom: 10px;">
    <tr>
        <td style="text-align: left"><input id="player_id" type="text" pattern="[0-9]*" placeholder="UID"
                                            style="width: 85%;height: 25px;"/></td>
        <td><input onclick="myQuery()" type="button" value="查询"/></td>
    </tr>
</table>
<div class="table2" style="margin-top: 2px;">
    <table id="table_data" style="width: 100%;text-align: center;font-size: 18px;" cellspacing="0" cellpadding="10">
        <tr>
            <td>UID</td>
            <td id="msg_uid">--</td>
        </tr>
        <tr>
            <td>昵称</td>
            <td id="msg_name">--</td>
        </tr>
        <tr>
            <td>钻石数</td>
            <td id="msg_cards">--</td>
        </tr>
        <tr>
            <td>邀请码</td>
            <td id="msg_agency">--</td>
        </tr>
        <tr>
            <td>注册时间</td>
            <td id="msg_regtime">--</td>
        </tr>
        <tr>
            <td>最后登出时间</td>
            <td id="msg_logout_lime">--</td>
        </tr>
        <tr>
            <td>绑码时间</td>
            <td id="msg_bind_time">--</td>
        </tr>
        <tr>
            <td>IP</td>
            <td id="msg_ip">--</td>
        </tr>
        <tr>
            <td>手机号</td>
            <td id="phone_num">--</td>
        </tr>

        <tr id="modify_phone_num" style="display: none">
            <td>手机号码：</td>
            <td><input id="new_phone_num" type="text" pattern="[0-9]*" placeholder="手机号码"
                       style="width: 50%;height: 25px;"/></td>
        </tr>
        <tr id="modify_phone_pw1" style="display: none">
            <td>密码：</td>
            <td><input id="new_phone_pw1" type="text" pattern="[0-9]*" placeholder="新密码"
                       style="width: 50%;height: 25px;"/></td>
        </tr>
        <tr id="modify_phone_pw2" style="display: none">
            <td>密码：</td>
            <td><input id="new_phone_pw2" type="text" pattern="[0-9]*" placeholder="确认新密码"
                       style="width: 50%;height: 25px;"/></td>
        </tr>
        <tr id="modify_phone_btn" style="display: none">
            <td><input onclick="modifyPhoneNum()" type="button" value="确定"/></td>
            <td><input onclick="hideModifyPhoneNum()" type="button" value="取消"/></td>
        </tr>
    </table>
</div>

<div class="table3" style="display: none;margin-top: 5px;" id="ipDiv">
    <table id="ipTable" style="width: 100%;text-align: center;font-size: 16px;" cellspacing="0" cellpadding="8">
    </table>
</div>
</body>
<script>
    var tmpUserMsg = null;

    function myQuery() {
        hideModifyPhoneNum();
        tmpUserMsg = null;
        $("#ipDiv").css("display", "none");

        var userId = $("#player_id").val().trim();
        if (userId == "") {
            alert("请输入玩家UID");
            return;
        }


        $("#msg_name").html("--");

        $("#msg_agency").html("--");

        $("#msg_cards").html("--");

        $("#msg_bind_time").html("--");

        $("#msg_regtime").html("--");

        $("#msg_logout_lime").html("--");

        $("#msg_uid").html("--");

        $("#msg_ip").html("--");

        $("#phone_num").html("--");
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/player/info",
            data: {userId: userId, gameId: localStorage.getItem('gameId')},
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if (result.code == 1000) {
                    var tmpDate = new Date();
                    var totalCards = 0;
                    var info = result.info;
                    if (info.name) {
                        $("#msg_name").html(info.name);
                    }
                    if (info.payBindId) {
                        $("#msg_agency").html(info.payBindId);
                    }
                    if (info.cards) {
                        totalCards += info.cards;
                    }
                    if (info.freeCards) {
                        totalCards += info.freeCards;
                    }
                    $("#msg_cards").html(totalCards);
                    if (info.payBindTime) {
                        tmpDate.setTime(info.payBindTime);
                        $("#msg_bind_time").html(tmpDate.format("yyyy-MM-dd hh:mm:ss"));
                    }
                    if (info.regTime) {
                        tmpDate.setTime(info.regTime);
                        $("#msg_regtime").html(tmpDate.format("yyyy-MM-dd hh:mm:ss"));
                    }
                    if (info.logoutTime) {
                        tmpDate.setTime(info.logoutTime);
                        $("#msg_logout_lime").html(tmpDate.format("yyyy-MM-dd hh:mm:ss"));
                    }
                    if (info.userId) {
                        $("#msg_uid").html(info.userId);
                    }
                    if (info.ip) {
                        tmpUserMsg = info;
                    }
                    if (info.phoneNum) {
                        $("#phone_num").html(info.phoneNum + ' <input type="button" value="修改" onclick="showModifyPhoneNum()" >' + ' <input type="button" value="解绑" onclick="delPhoneNum()">');
                    } else {
                        $("#phone_num").html(' <input type="button" value="设置" onclick="showModifyPhoneNum()"/>');
                    }
                    tmpUserMsg = info;

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

    function showModifyPhoneNum() {
        $("#modify_phone_num").show();
        $("#modify_phone_pw1").show();
        $("#modify_phone_pw2").show();
        $("#modify_phone_btn").show();
    }

    function hideModifyPhoneNum() {
        $("#modify_phone_num").hide();
        $("#modify_phone_pw1").hide();
        $("#modify_phone_pw2").hide();
        $("#modify_phone_btn").hide();
        $("#new_phone_num").val("");
        $("#new_phone_pw1").val("");
        $("#new_phone_pw2").val("");
    }

    function modifyPhoneNum() {
        var phoneNum = $("#new_phone_num").val();
        var phonePw1 = $("#new_phone_pw1").val();
        var phonePw2 = $("#new_phone_pw2").val();
        if (phonePw1 != phonePw2) {
            alert("两次密码不一至")
            return;
        }
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/modifyPhoneNum",
            data: {
                userId:tmpUserMsg.userId,
                phoneNum: phoneNum,
                phonePw: phonePw1
            },
            dataType: "json",
            success: function (result) {
                if (result.code == 1000) {
                    alert(result.message);
                    hideModifyPhoneNum();
                    myQuery();
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

    function delPhoneNum() {
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/delPhoneNum",
            data: {
                userId: tmpUserMsg.userId
            },
            dataType: "json",
            success: function (result) {
                if (result.code == 1000) {
                    alert(result.message);
                    hideModifyPhoneNum();
                    myQuery();
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