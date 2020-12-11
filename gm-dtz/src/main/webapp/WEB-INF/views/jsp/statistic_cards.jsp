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

    </style>
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.1">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
</head>

<body style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<table class="header_table" cellpadding="0" cellspacing="0">
    <tr>
        <td class="fix_td" onclick="history.back()"><img class="header_img1" src="<%=basePath%>/image/back.png"/></td>
        <td><img src="<%=basePath%>/image/cards.png" class="header_img2" style="width: 94px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<table cellpadding="5" style="text-align: center;margin-top: 10px;border-bottom: 1px solid #b5b5b5;width: 100%;padding-bottom: 10px;">
    <tr>
        <td style="text-align: left"><input id="start_time" value="${requestScope.startDate}" type="date" placeholder="开始时间" style="width: 85%;height: 25px;"/></td>
        <td><input id="end_time" type="date" value="${requestScope.endDate}" placeholder="结束时间" style="width: 85%;height: 25px;"/></td>
        <td><input onclick="myQuery()" type="button" value="查询"/></td>
    </tr>
</table>
<div class="table2" style="margin-top: 2px;">
    <table id="table_data" style="width: 100%;text-align: center;font-size: 18px;" cellspacing="0" cellpadding="10">
        <tr style="color: #ee6a2a;font-size: 20px;">
            <td>日期</td>
            <td>总钻石剩余</td>
            <td>总钻石消耗</td>
        </tr>
    </table>
</div>
</body>
<script>

    $(document).ready(function () {
        myQuery();
    });

    function myQuery() {
        $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/cards/statistics",
            data: {startDate:$("#start_time").val(),endDate:$("#end_time").val(),
                gameId:localStorage.getItem("gameId")},
            dataType: "json",
            success: function(result){
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if (result.code==1000){
                    $("#table_data").html('<tr style="color: #ee6a2a;font-size: 20px;"><td>日期</td><td>总钻石剩余</td><td>总钻石消耗</td></tr>');
                    $.each(result.datas,function (index,tempData){
                        var tempTime;
                        if (!isNaN(tempData.consumeDate)){
                            var tmpDate=new Date();
                            tmpDate.setTime(tempData.consumeDate);
                            tempTime=tmpDate.format("yyyy-MM-dd");
                        }else{
                            tempTime=tempData.consumeDate;
                        }

                        var total1=0,total2=0;
                        var freeCardSum=0,commonCardSum=0,commonCards=0,freeCards=0;
                        if(tempData.freeCardSum){
                            freeCardSum=tempData.freeCardSum;
                            total1+=freeCardSum;
                        }
                        if(tempData.commonCardSum){
                            commonCardSum=tempData.commonCardSum;
                            total1+=commonCardSum;
                        }

                        if(tempData.commonCards){
                            commonCards=tempData.commonCards;
                            total2+=commonCards;
                        }
                        if(tempData.freeCards){
                            freeCards=tempData.freeCards;
                            total2+=freeCards;
                        }
                        total2=-total2;

                        var newRow="<tr onclick='displayData(this.id)' id='data"+tempTime+"'><td>"+tempTime+"</td><td>"+total1+"</td><td>"+total2+"</td></tr>";
                        newRow+= '<tr id="detail'+tempTime+'" style="display: none"><td colspan="3"><table class="table3" style="width: 100%;text-align: left;background-color: #f3ffea;" cellspacing="0" cellpadding="2">'
                        +'<tr><td>付费房卡剩余数</td><td>'+commonCardSum+'</td></tr>'
                        +'<tr><td>免费房卡剩余数</td><td>'+freeCardSum+'</td></tr>'
                            +'<tr><td>付费钻石消耗</td><td>'+(-commonCards)+'</td></tr>'
                            +'<tr><td>免费钻石消耗</td><td>'+(-freeCards)+'</td></tr>'
                            +'<tr><td>4人打筒子</td><td>'+(-tempData.playType114-tempData.playType113-tempData.playType212)+'('+((-tempData.playType114-tempData.playType113-tempData.playType212)*100/total2).toFixed(2)+'%)</td></tr>'
                            +'<tr><td>3人打筒子</td><td>'+(-tempData.playType115-tempData.playType116-tempData.playType211)+'('+((-tempData.playType115-tempData.playType116-tempData.playType211)*100/total2).toFixed(2)+'%)</td></tr>'
                            +'<tr><td>2人打筒子</td><td>'+(-tempData.playType117-tempData.playType118-tempData.playType210)+'('+((-tempData.playType117-tempData.playType118-tempData.playType210)*100/total2).toFixed(2)+'%)</td></tr>'
                            +'<tr><td>跑得快(15张)</td><td>'+(-tempData.playType15)+'('+(-tempData.playType15*100/total2).toFixed(2)+'%)</td></tr>'
                            +'<tr><td>跑得快(16张)</td><td>'+(-tempData.playType16)+'('+(-tempData.playType16*100/total2).toFixed(2)+'%)</td></tr>'
                            +'<tr><td>邵阳剥皮</td><td>'+(-tempData.playType33)+'('+(-tempData.playType33*100/total2).toFixed(2)+'%)</td></tr>'
                            +'<tr><td>邵阳字牌</td><td>'+(-tempData.playType32)+'('+(-tempData.playType32*100/total2).toFixed(2)+'%)</td></tr>'
                            +'<tr><td>半边天炸</td><td>'+(-tempData.playType131)+'('+(-tempData.playType131*100/total2).toFixed(2)+'%)</td></tr>'
                            +'<tr><td>红中麻将</td><td>'+(-tempData.playType221)+'('+(-tempData.playType221*100/total2).toFixed(2)+'%)</td></tr>'
                            +'<tr><td>娄底放炮罚</td><td>'+(-tempData.playType199)+'('+(-tempData.playType199*100/total2).toFixed(2)+'%)</td></tr>'
                            +'</table></td></tr>';
                        $('#table_data').append(newRow);
                    });
                }else{
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

    function displayData(currentDate) {
        var tempId=currentDate.replace("data","detail");
        if ($("#"+tempId).css("display")=="none"){
            $("#"+tempId).css("display","");
        }else{
            $("#"+tempId).css("display","none");
        }
    }

    Date.prototype.format = function(format) {
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