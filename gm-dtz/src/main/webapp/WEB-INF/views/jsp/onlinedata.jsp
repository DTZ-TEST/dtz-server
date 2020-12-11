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
        <td><img src="<%=basePath%>/image/onlinedata.png" class="header_img2" style="width: 94px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3"
                                                                                src="<%=basePath%>/image/home.png"/>
        </td>
    </tr>
</table>
<table cellpadding="5"
       style="text-align: center;margin-top: 10px;border-bottom: 1px solid #b5b5b5;width: 100%;padding-bottom: 10px;">
    <tr>
        <td style="text-align: left"><input id="start_time" value="${requestScope.startDate}" type="date"
                                            placeholder="开始时间" style="width: 85%;height: 25px;"/></td>
        <td><input id="end_time" type="date" value="${requestScope.endDate}" placeholder="结束时间"
                   style="width: 85%;height: 25px;"/></td>
        <td><input onclick="myQuery()" type="button" value="查询"/></td>
    </tr>
</table>
<div class="table2" style="margin-top: 2px;">
    <table id="table_data" style="width: 100%;text-align: center;font-size: 16px;" cellspacing="0" cellpadding="10">
        <tr style="color: #ee6a2a;font-size: 18px;">
            <td>日期</td>
            <td>9点</td>
            <td>12点</td>
            <td>15点</td>
            <td>18点</td>
            <td>22点</td>
        </tr>
    </table>
</div>
</div>

</body>

<script>
    $(document).ready(function () {
        myQuery();
    });

    function myQuery() {
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/data/statistics/onlineData",
            data: {
                startDate: $("#start_time").val(),
                endDate: $("#end_time").val(),gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if (result.code == 1000) {
                    $("#table_data").html('<tr style="color: #ee6a2a;font-size: 18px;"><td>日期</td><td>9点</td><td>12点</td><td>15点</td><td>18点</td><td>22点</td></tr>');
                    $.each(result.datas, function (index, tempData) {
                        var newRow = "<tr onclick='displayData(this.id)' id='data" + tempData.dateTime + "'><td>" + tempData.dateTime + "</td><td>" + tempData.ninetime + "</td><td>" + tempData.tlvtime + "</td><td>" + tempData.fivettime + "</td><td>" + tempData.ettime + "</td><td>" + tempData.tytwotime + "</td></tr>";    
                        newRow += '<tr id="detail' + tempData.dateTime + '" style="display: none"><td colspan="6"><table class="table3" style="width: 100%;text-align: left;background-color: #f3ffea;" cellspacing="0" cellpadding="2">'
                            + '<tr><td>时间</td><td>在线人数</td></tr>'
                            + '<tr><td>1点</td><td>' + tempData.onetime + '</td></tr>'
                            + '<tr><td>2点</td><td>' + tempData.twotime + '</td></tr>'
                            + '<tr><td>3点</td><td>' + tempData.threetime + '</td></tr>'
                            + '<tr><td>4点</td><td>' + tempData.fourtime + '</td></tr>'
                            + '<tr><td>5点</td><td>' + tempData.fivetime + '</td></tr>'
                            + '<tr><td>6点</td><td>' + tempData.sixtime + '</td></tr>'
                            + '<tr><td>7点</td><td>' + tempData.seventime + '</td></tr>'
                            + '<tr><td>8点</td><td>' + tempData.eighttime + '</td></tr>'
                            + '<tr><td>9点</td><td>' + tempData.ninetime + '</td></tr>'
                            + '<tr><td>10点</td><td>' + tempData.tentime + '</td></tr>'
                            
                            + '<tr><td>11点</td><td>' + tempData.eleventime + '</td></tr>'
                            + '<tr><td>12点</td><td>' + tempData.tlvtime + '</td></tr>'
                            + '<tr><td>13点</td><td>' + tempData.thdtime + '</td></tr>'
                            
                            + '<tr><td>14点</td><td>' + tempData.fottime + '</td></tr>'
                            + '<tr><td>15点</td><td>' + tempData.fivettime + '</td></tr>'
                            
                            + '<tr><td>16点</td><td>' + tempData.sixttime + '</td></tr>'
                            + '<tr><td>17点</td><td>' + tempData.svnttime + '</td></tr>'
                            
                            + '<tr><td>18点</td><td>' + tempData.ettime + '</td></tr>'
                            + '<tr><td>19点</td><td>' + tempData.nttime + '</td></tr>'
                            + '<tr><td>20点</td><td>' + tempData.tytime + '</td></tr>'
                            
                            + '<tr><td>21点</td><td>' + tempData.tyonetime + '</td></tr>'
                            + '<tr><td>22点</td><td>' + tempData.tytwotime + '</td></tr>'
                            + '<tr><td>23点</td><td>' + tempData.tythreetime + '</td></tr>'
                            + '<tr><td>24点</td><td>' + tempData.tyfourtime + '</td></tr>';
                            newRow += '</table></td></tr>';

                        $('#table_data').append(newRow);
                    });
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

    function displayData(currentDate) {
        var tempId = currentDate.replace("data", "detail");
        if ($("#" + tempId).css("display") == "none") {
            $("#" + tempId).css("display", "");
        } else {
            $("#" + tempId).css("display", "none");
        }
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