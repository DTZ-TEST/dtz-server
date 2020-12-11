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
        <td><img src="<%=basePath%>/image/total_msg.png" class="header_img2" style="width: 94px;"/></td>
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
            <td>DAU</td>
            <td>新增用户</td>
            <td>充值总额</td>
            <td>代理充值</td>
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
            url: "<%=path%>/data/statistics/list",
            data: {
                startDate: $("#start_time").val(),
                endDate: $("#end_time").val(),
                gameId:localStorage.getItem("gameId")
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if (result.code == 1000) {
                    $("#table_data").html('<tr style="color: #ee6a2a;font-size: 18px;"><td>日期</td><td>DAU</td><td>新增用户</td><td>充值总额</td><td>代理充值</td></tr>');
                    $.each(result.datas, function (index, tempData) {
                        var newRow = "<tr onclick='displayData(this.id)' id='data" + tempData.dateTime + "'><td>" + tempData.dateTime + "</td><td>" + tempData.active1day + "</td><td>" + tempData.regCount + "</td><td>" + tempData.paySum + "</td><td>" + tempData.pfPaySum + "</td></tr>";
                        newRow += '<tr id="detail' + tempData.dateTime + '" style="display: none"><td colspan="4"><table class="table3" style="width: 100%;text-align: left;background-color: #f3ffea;" cellspacing="0" cellpadding="2">'
                            + '<tr><td>日期</td><td>' + tempData.dateTime + '</td></tr>'
                            + '<tr><td>总用户数</td><td>' + tempData.regTotalCount + '</td></tr>'
                            + '<tr><td>新增用户数</td><td>' + tempData.regCount + '</td></tr>'
                            + '<tr><td>日活跃</td><td>' + tempData.active1day + '</td></tr>'
                            + '<tr><td>周活跃</td><td>' + tempData.active7day + '</td></tr>'
                            + '<tr><td>月活跃</td><td>' + tempData.active30day + '</td></tr>'
                            + '<tr><td>新增付费人数</td><td>' + tempData.firstRegPayCount + '</td></tr>'
                            + '<tr><td>新增付费金额</td><td>' + tempData.firstRegPaySum + '</td></tr>'
                            + '<tr><td>新增付费率</td><td>' + (tempData.regCount == 0 ? 0 : (tempData.firstRegPayCount * 100 / tempData.regCount).toFixed(2)) + '%</td></tr>'
                            + '<tr><td>总付费人数</td><td>' + tempData.payCount + '</td></tr>'
                            + '<tr><td>总付费金额</td><td>' + tempData.paySum + '</td></tr>'
                            + '<tr><td>总付费率</td><td>' + (tempData.payRate * 100).toFixed(2) + '%</td></tr>'
                            + '<tr><td>首充用户数</td><td>' + tempData.firstCount + '</td></tr>'
                            + '<tr><td>首充总金额</td><td>' + tempData.firstSum + '</td></tr>'
                            + '<tr><td>ARPU</td><td>' + tempData.arpu.toFixed(2) + '</td></tr>';

                        var retention1 = "--";
                        var retention2 = "--";
                        var retention3 = "--";
                        var retention4 = "--";
                        var retention5 = "--";
                        var retention6 = "--";
                        var retention7 = "--";
                        var retention14 = "--";
                        var retention30 = "--";

                        if (tempData.extend) {
                            var extMsg = tempData.extend.trim();
                            if (extMsg.length > 0) {
                                var extJson = JSON.parse(extMsg);
                                if (extJson.hasOwnProperty("r1")) {
                                    if (extJson.r1 > 0) {
                                        retention1 = tempData.regTotalAlive1day + "(" + (tempData.regTotalAlive1day * 100 / extJson.r1).toFixed(2) + "%)";
                                    } else {
                                        retention1 = "0(0)";
                                    }
                                }
                                if (extJson.hasOwnProperty("r2")) {
                                    if (extJson.r2 > 0) {
                                        retention2 = tempData.regTotalAlive2day + "(" + (tempData.regTotalAlive2day * 100 / extJson.r2).toFixed(2) + "%)";
                                    } else {
                                        retention2 = "0(0)";
                                    }
                                }
                                if (extJson.hasOwnProperty("r3")) {
                                    if (extJson.r3 > 0) {
                                        retention3 = tempData.regTotalAlive3day + "(" + (tempData.regTotalAlive3day * 100 / extJson.r3).toFixed(2) + "%)";
                                    } else {
                                        retention3 = "0(0)";
                                    }
                                }
                                if (extJson.hasOwnProperty("r4")) {
                                    if (extJson.r4 > 0) {
                                        retention4 = tempData.regTotalAlive4day + "(" + (tempData.regTotalAlive4day * 100 / extJson.r4).toFixed(2) + "%)";
                                    } else {
                                        retention4 = "0(0)";
                                    }
                                }
                                if (extJson.hasOwnProperty("r5")) {
                                    if (extJson.r5 > 0) {
                                        retention5 = tempData.regTotalAlive5day + "(" + (tempData.regTotalAlive5day * 100 / extJson.r5).toFixed(2) + "%)";
                                    } else {
                                        retention5 = "0(0)";
                                    }
                                }
                                if (extJson.hasOwnProperty("r6")) {
                                    if (extJson.r6 > 0) {
                                        retention6 = tempData.regTotalAlive6day + "(" + (tempData.regTotalAlive6day * 100 / extJson.r6).toFixed(2) + "%)";
                                    } else {
                                        retention6 = "0(0)";
                                    }
                                }
                                if (extJson.hasOwnProperty("r7")) {
                                    if (extJson.r7 > 0) {
                                        retention7 = tempData.regTotalAlive7day + "(" + (tempData.regTotalAlive7day * 100 / extJson.r7).toFixed(2) + "%)";
                                    } else {
                                        retention7 = "0(0)";
                                    }
                                }
                                if (extJson.hasOwnProperty("r14")) {
                                    if (extJson.r14 > 0) {
                                        retention14 = tempData.regTotalAlive14day + "(" + (tempData.regTotalAlive14day * 100 / extJson.r14).toFixed(2) + "%)";
                                    } else {
                                        retention14 = "0(0)";
                                    }
                                }
                                if (extJson.hasOwnProperty("r30")) {
                                    if (extJson.r30 > 0) {
                                        retention30 = tempData.regTotalAlive30day + "(" + (tempData.regTotalAlive30day * 100 / extJson.r30).toFixed(2) + "%)";
                                    } else {
                                        retention30 = "0(0)";
                                    }
                                }
                            }
                        }

                        newRow += ''
                            + '<tr><td>次日留存</td><td>' + retention1 + '</td></tr>'
                            //                            +'<tr><td>2日留存</td><td>'+retention2+'</td></tr>'
                            + '<tr><td>3日留存</td><td>' + retention2 + '</td></tr>'
                            + '<tr><td>4日留存</td><td>' + retention3 + '</td></tr>'
                            + '<tr><td>5日留存</td><td>' + retention4 + '</td></tr>'
                            + '<tr><td>6日留存</td><td>' + retention5 + '</td></tr>'
                            + '<tr><td>7日留存</td><td>' + retention6 + '</td></tr>'
                            + '<tr><td>14日留存</td><td>' + retention14 + '</td></tr>'
                            + '<tr><td>30日留存</td><td>' + retention30 + '</td></tr>';

                        var itemCount = 0, item10 = 0, item30 = 0, item50 = 0, item100 = 0, item150 = 0, item200 = 0;

                        if (tempData.extend) {
                            var extMsg = tempData.extend.trim();
                            if (extMsg.length > 0) {
                                var extJson = JSON.parse(extMsg);
                                //10,30,50,100,150,200
                                if (extJson.hasOwnProperty("item10")) {
                                    item10 = extJson.item10;
                                    itemCount += item10;
                                }
                                if (extJson.hasOwnProperty("item30")) {
                                    item30 = extJson.item30;
                                    itemCount += item30;
                                }
                                if (extJson.hasOwnProperty("item50")) {
                                    item50 = extJson.item50;
                                    itemCount += item50;
                                }
                                if (extJson.hasOwnProperty("item100")) {
                                    item100 = extJson.item100;
                                    itemCount += item100;
                                }
                                if (extJson.hasOwnProperty("item150")) {
                                    item150 = extJson.item150;
                                    itemCount += item150;
                                }
                                if (extJson.hasOwnProperty("item200")) {
                                    item200 = extJson.item200;
                                    itemCount += item200;
                                }
                            }
                        }

                        if (item10 > 0) {
                            newRow += '<tr><td>计费点（10元）</td><td>' + item10 + '(' + (item10 * 100 / itemCount).toFixed(2) + '%)</td></tr>';
                        }
                        if (item30 > 0) {
                            newRow += '<tr><td>计费点（30元）</td><td>' + item30 + '(' + (item30 * 100 / itemCount).toFixed(2) + '%)</td></tr>';
                        }
                        if (item50 > 0) {
                            newRow += '<tr><td>计费点（50元）</td><td>' + item50 + '(' + (item50 * 100 / itemCount).toFixed(2) + '%)</td></tr>';
                        }
                        if (item100 > 0) {
                            newRow += '<tr><td>计费点（100元）</td><td>' + item100 + '(' + (item100 * 100 / itemCount).toFixed(2) + '%)</td></tr>';
                        }
                        if (item150 > 0) {
                            newRow += '<tr><td>计费点（150元）</td><td>' + item150 + '(' + (item150 * 100 / itemCount).toFixed(2) + '%)</td></tr>';
                        }
                        if (item200 > 0) {
                            newRow += '<tr><td>计费点（200元）</td><td>' + item200 + '(' + (item200 * 100 / itemCount).toFixed(2) + '%)</td></tr>';
                        }

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