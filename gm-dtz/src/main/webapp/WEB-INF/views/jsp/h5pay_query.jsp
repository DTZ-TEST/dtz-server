<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String path = request.getContextPath() + "";
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
        <td><img src="<%=basePath%>/image/h5pay.png" class="header_img2" style="width: 94px;"/></td>
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
        <tr style="color: #ee6a2a;font-size: 20px;">
            <td>邀请码</td>
            <td>金额</td>
            <td>类型</td>
            <td>时间</td>
        </tr>
        <!--<tr>-->
        <!--<td>1</td>-->
        <!--<td>50</td>-->
        <!--<td>2017-04-17 12:00:30</td>-->
        <!--</tr>-->
        <!--<tr>-->
        <!--<td>2</td>-->
        <!--<td>50</td>-->
        <!--<td>2017-04-17 12:00:30</td>-->
        <!--</tr>-->
    </table>
</div>
</body>
<script>
    function myQuery(append) {
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/pay/query",
            data: {
                agencyId: $("#agencyId").val(),gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {
                if (result.code == 1000) {
                    $("#table_data").html('<tr style="color: #ee6a2a;font-size: 20px;"><td>邀请码</td> <td>金额</td><td>类型</td><td>时间</td></tr>');
                    if (result.datas.length > 0) {
                        $.each(result.datas, function (index, tempData) {
                            var tempTime;
                            if (!isNaN(tempData.create_time)) {
                                var tmpDate = new Date();
                                var tempType = "";
                                var tempMoney = 0.0;
                                tmpDate.setTime(tempData.create_time);
                                tempTime = tmpDate.format("yyyy-MM-dd hh:mm:ss");
                                var type = tempData.is_sent;
                                if (type == "1") {
                                    tempType = "直充";
                                    tempMoney = tempData.item_num / 100;
                                }
                                else {
                                    tempType = "补录";
                                    tempMoney = tempData.order_amount / 100;
                                }
                            } else {
                                tempTime = tempData.createTime;
                            }
                            var newRow = "<tr><td>" + $("#agencyId").val() + "</td><td>" + tempData.order_amount / 100 + "</td><td>" + tempType + "</td><td>" + tempTime + "</td></tr>";
                            $('#table_data').append(newRow);
                        });
                    } else {
                        var newRow = "<tr><td colspan='4'>暂无记录！</td></tr>";
                        $('#table_data').append(newRow);
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