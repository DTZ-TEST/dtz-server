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
            <td>总用户</td>
            <td>DAU</td>
            <td>新增用户</td>
            <td>总局数</td>
        </tr>
    </table>
</div>
</body>

<script>
    $(document).ready(function () {
        myQuery();
    });

    function myQuery() {
        var gameCode ='${gameCode}';
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/data/statistics/goldlist",
            data: {
                startDate: $("#start_time").val(),
                endDate: $("#end_time").val(),gameId:localStorage.getItem('gameId'),
                gameCode:gameCode
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if (result.code == 1000) {
                    $("#table_data").html('<tr style="color: #ee6a2a;font-size: 18px;"><td>日期</td><td>总用户</td><td>DAU</td><td>新增用户</td><td>总局数</td></tr>');
                    $.each(result.datas, function (index, tempData) {
                    	var twodaylc = "--";
                    	var threedaylc = "--";
                    	var fourdaylc = "--";
                    	var fivedaylc = "--";
                    	var sixdaylc = "--";
                    	var sevendaylc = "--";
                    	var fifteendaylc = "--";
                    	var monthdaylc = "--";
                    	if(tempData.twodaylc!=undefined){
                    		twodaylc = tempData.twodaylc+"(" + (tempData.twodaylc * 100 / tempData.addUser).toFixed(2) + "%)";
                    	}
                    	if(tempData.threedaylc!=undefined){
                    		threedaylc = tempData.threedaylc+"(" + (tempData.threedaylc * 100 / tempData.addUser).toFixed(2) + "%)";
                    	}
                    	if(tempData.fourdaylc!=undefined){
                    		fourdaylc = tempData.fourdaylc+"(" + (tempData.fourdaylc * 100 / tempData.addUser).toFixed(2) + "%)";
                    	}
                    	if(tempData.fivedaylc!=undefined){
                    		fivedaylc = tempData.fivedaylc+"(" + (tempData.fivedaylc * 100 / tempData.addUser).toFixed(2) + "%)";
                    	}
                    	if(tempData.sixdaylc!=undefined){
                    		sixdaylc = tempData.sixdaylc+"(" + (tempData.sixdaylc * 100 / tempData.addUser).toFixed(2) + "%)";
                    	}
                    	if(tempData.sevendaylc!=undefined){
                    		sevendaylc = tempData.sevendaylc+"(" + (tempData.sevendaylc * 100 / tempData.addUser).toFixed(2) + "%)";
                    	}
                    	if(tempData.fifteendaylc!=undefined){
                    		fifteendaylc = tempData.fifteendaylc;+"(" + (tempData.fifteendaylc * 100 / tempData.addUser).toFixed(2) + "%)";
                    	}
                    	if(tempData.monthdaylc!=undefined){
                    		monthdaylc = tempData.monthdaylc+"(" + (tempData.monthdaylc * 100 / tempData.addUser).toFixed(2) + "%)";
                    	}

                    	var tempTotal = tempData.totalNums+tempData.totalPdkNums+tempData.totalphzNums;
                        var newRow = "<tr onclick='displayData(this.id)' id='data" + tempData.dateTime + "'><td>" + tempData.dateTime + "</td><td>" + tempData.totalUser + "</td><td>" + tempData.dau + "</td><td>" + tempData.addUser + "</td><td>" + tempTotal + "</td></tr>";
                        newRow += '<tr id="detail' + tempData.dateTime + '" style="display: none"><td colspan="5"><table class="table3" style="width: 100%;text-align: left;background-color: #f3ffea;" cellspacing="0" cellpadding="2">';
                        // if('pdk'==gameCode){
                        //     newRow += '<tr><td>跑得快初级场数</td><td>' + tempData.cjPdkTotal + '('+((tempData.cjPdkTotal/tempData.totalPdkNums)*100).toFixed(0)+'%)</td></tr>'
                        //     + '<tr><td>跑得快中级场数</td><td>' + tempData.zjPdkTotal + '('+((tempData.zjPdkTotal/tempData.totalPdkNums)*100).toFixed(0)+'%)</td></tr>'
                        //     + '<tr><td>跑得快高级场数</td><td>' + tempData.gjPdkTotal + '('+((tempData.gjPdkTotal/tempData.totalPdkNums)*100).toFixed(0)+'%)</td></tr>'
                        //     + '<tr><td>跑得快总局数</td><td>' + tempData.totalPdkNums + '</td></tr>';
                        // }else if('dtz'==gameCode){
                        //     newRow += '<tr><td>打筒子初级场数</td><td>' + tempData.cjTotal + '('+((tempData.cjTotal/tempData.totalNums)*100).toFixed(0)+'%)</td></tr>'
                        //     + '<tr><td>打筒子中级场数</td><td>' + tempData.zjTotal + '('+((tempData.zjTotal/tempData.totalNums)*100).toFixed(0)+'%)</td></tr>'
                        //     + '<tr><td>打筒子高级场数</td><td>' + tempData.gjTotal + '('+((tempData.gjTotal/tempData.totalNums)*100).toFixed(0)+'%)</td></tr>'
                        //     + '<tr><td>打筒子总局数</td><td>' + tempData.totalNums + '</td></tr>';
                        // }else{
                            newRow += '<tr><td>打筒子初级场数</td><td>' + tempData.cjTotal + '('+((tempData.cjTotal/tempTotal)*100).toFixed(0)+'%)</td></tr>'
                            + '<tr><td>打筒子中级场数</td><td>' + tempData.zjTotal + '('+((tempData.zjTotal/tempTotal)*100).toFixed(0)+'%)</td></tr>'
                            + '<tr><td>打筒子高级场数</td><td>' + tempData.gjTotal + '('+((tempData.gjTotal/tempTotal)*100).toFixed(0)+'%)</td></tr>'
                            + '<tr><td>打筒子总局数</td><td>' + tempData.totalNums + '('+((tempData.totalNums/tempTotal)*100).toFixed(0)+'%)</td></tr>'
                            + '<tr><td>跑得快初级场数</td><td>' + tempData.cjPdkTotal + '('+((tempData.cjPdkTotal/tempTotal)*100).toFixed(0)+'%)</td></tr>'
                            + '<tr><td>跑得快中级场数</td><td>' + tempData.zjPdkTotal + '('+((tempData.zjPdkTotal/tempTotal)*100).toFixed(0)+'%)</td></tr>'
                            + '<tr><td>跑得快高级场数</td><td>' + tempData.gjPdkTotal + '('+((tempData.gjPdkTotal/tempTotal)*100).toFixed(0)+'%)</td></tr>'
                            + '<tr><td>跑得快总局数</td><td>' + tempData.totalPdkNums + '('+((tempData.totalPdkNums/tempTotal)*100).toFixed(0)+'%)</td></tr>'
                            + '<tr><td>跑胡子初级场数</td><td>' + tempData.cjphzTotal + '('+((tempData.cjphzTotal/tempTotal)*100).toFixed(0)+'%)</td></tr>'
                            + '<tr><td>跑胡子中级场数</td><td>' + tempData.zjphzTotal + '('+((tempData.zjphzTotal/tempTotal)*100).toFixed(0)+'%)</td></tr>'
                            + '<tr><td>跑胡子高级场数</td><td>' + tempData.gjphzTotal + '('+((tempData.gjphzTotal/tempTotal)*100).toFixed(0)+'%)</td></tr>'
                            + '<tr><td>跑胡子总局数</td><td>' + tempData.totalphzNums + '('+((tempData.totalphzNums/tempTotal)*100).toFixed(0)+'%)</td></tr>'
                        // }

                        newRow += '<tr><td>次日留存</td><td>' + twodaylc + '</td></tr>'
                            + '<tr><td>3日留存</td><td>' + threedaylc + '</td></tr>'
                            + '<tr><td>4日留存</td><td>' + fourdaylc + '</td></tr>'
                            + '<tr><td>5日留存</td><td>' + fivedaylc + '</td></tr>'
                            + '<tr><td>6日留存</td><td>' + sixdaylc + '</td></tr>'
                            + '<tr><td>7日留存</td><td>' + sevendaylc + '</td></tr>'
                            + '<tr><td>15日留存</td><td>' + fifteendaylc + '</td></tr>'
                            + '<tr><td>30日留存</td><td>' + monthdaylc + '</td></tr>';
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