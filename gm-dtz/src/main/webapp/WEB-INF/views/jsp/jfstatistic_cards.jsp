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
            <td>总积分</td>
            <td>服务费</td>
            <td>兑换差额</td>
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
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/cards/jf/statistics",
            data: { gameCode:gameCode,startDate:$("#start_time").val(),endDate:$("#end_time").val(),gameId:localStorage.getItem('gameId')},
            dataType: "json",
            success: function(result){
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if (result.code==1000){
                    $("#table_data").html('<tr style="color: #ee6a2a;font-size: 20px;"><td>日期</td><td>总积分</td><td>服务费</td><td>兑换差额</td></tr>');
                    $.each(result.datas,function (index,tempData){
                        var tempTotal = tempData.totalService+tempData.totalPdkService+tempData.totalphzService;
                        var newRow="<tr onclick='displayData(this.id)' id='data"+tempData.dateTime+"'><td>"+tempData.dateTime+"</td><td>"+(tempData.totalGold/10000).toFixed(0)+"万</td><td style='width: 200px'>"+tempTotal+"<br/>("+tempTotal/200+"钻)</td><td>"+tempData.cardce+"</td></tr>";
                        newRow+= '<tr id="detail'+tempData.dateTime+'" style="display: none"><td colspan="4"><table class="table3" style="width: 100%;text-align: left;background-color: #f3ffea;" cellspacing="0" cellpadding="2">';
                            // if('pdk'==gameCode){
                            //     newRow+='<tr><td>跑得快初级场服务费</td><td>'+(tempData.cjPdkGold/10000).toFixed(0)+'万('+tempData.cjPdkGold/200+'钻)</td><td>'+((tempData.cjPdkGold/tempData.totalPdkService)*100).toFixed(0)+'%</td></tr>'
                            //     +'<tr><td>跑得快中级场服务费</td><td>'+(tempData.zjPdkGold/10000).toFixed(0)+'万('+tempData.zjPdkGold/200+'钻)</td><td>'+((tempData.zjPdkGold/tempData.totalPdkService)*100).toFixed(0)+'%</td></tr>'
                            //     +'<tr><td>跑得快高级场服务费</td><td>'+(tempData.gjPdkGold/10000).toFixed(0)+'万('+tempData.gjPdkGold/200+'钻)</td><td>'+((tempData.gjPdkGold/tempData.totalPdkService)*100).toFixed(0)+'%</td></tr>'
                            //     +'<tr><td>跑得快总服务费</td><td colspan="2">'+(tempData.totalPdkService/10000).toFixed(0)+'万('+tempData.totalPdkService/200+'钻)</td></tr>';
                            // }else if('dtz'==gameCode){
                            //     newRow+='<tr><td>打筒子初级场服务费</td><td>'+(tempData.cjGold/10000).toFixed(0)+'万('+tempData.cjGold/200+'钻)</td><td>'+((tempData.cjGold/tempData.totalService)*100).toFixed(0)+'%</td></tr>'
                            //     +'<tr><td>打筒子中级场服务费</td><td>'+(tempData.zjGold/10000).toFixed(0)+'万('+tempData.zjGold/200+'钻)</td><td>'+((tempData.zjGold/tempData.totalService)*100).toFixed(0)+'%</td></tr>'
                            //     +'<tr><td>打筒子高级场服务费</td><td>'+(tempData.gjGold/10000).toFixed(0)+'万('+tempData.gjGold/200+'钻)</td><td>'+((tempData.gjGold/tempData.totalService)*100).toFixed(0)+'%</td></tr>'
                            //     +'<tr><td>打筒子总服务费</td><td colspan="2">'+(tempData.totalService/10000).toFixed(0)+'万('+tempData.totalService/200+'钻)</td></tr>';
                            // }else{
                                newRow+='<tr><td>打筒子初级场服务费</td><td>'+(tempData.cjGold/10000).toFixed(0)+'万('+tempData.cjGold/200+'钻)</td><td>'+((tempData.cjGold/tempTotal)*100).toFixed(0)+'%</td></tr>'
                                +'<tr><td>打筒子中级场服务费</td><td>'+(tempData.zjGold/10000).toFixed(0)+'万('+tempData.zjGold/200+'钻)</td><td>'+((tempData.zjGold/tempTotal)*100).toFixed(0)+'%</td></tr>'
                                +'<tr><td>打筒子高级场服务费</td><td>'+(tempData.gjGold/10000).toFixed(0)+'万('+tempData.gjGold/200+'钻)</td><td>'+((tempData.gjGold/tempTotal)*100).toFixed(0)+'%</td></tr>'
                                +'<tr><td>打筒子总服务费</td><td>'+(tempData.totalService/10000).toFixed(0)+'万('+tempData.totalService/200+'钻)</td><td>'+((tempData.totalService/tempTotal)*100).toFixed(0)+'%</td></tr>'
                                +'<tr><td>跑得快初级场服务费</td><td>'+(tempData.cjPdkGold/10000).toFixed(0)+'万('+tempData.cjPdkGold/200+'钻)</td><td>'+((tempData.cjPdkGold/tempTotal)*100).toFixed(0)+'%</td></tr>'
                                +'<tr><td>跑得快中级场服务费</td><td>'+(tempData.zjPdkGold/10000).toFixed(0)+'万('+tempData.zjPdkGold/200+'钻)</td><td>'+((tempData.zjPdkGold/tempTotal)*100).toFixed(0)+'%</td></tr>'
                                +'<tr><td>跑得快高级场服务费</td><td>'+(tempData.gjPdkGold/10000).toFixed(0)+'万('+tempData.gjPdkGold/200+'钻)</td><td>'+((tempData.gjPdkGold/tempTotal)*100).toFixed(0)+'%</td></tr>'
                                +'<tr><td>跑得快总服务费</td><td>'+(tempData.totalPdkService/10000).toFixed(0)+'万('+tempData.totalPdkService/200+'钻)</td><td>'+((tempData.totalPdkService/tempTotal)*100).toFixed(0)+'%</td></tr>'
                                +'<tr><td>跑胡子初级场服务费</td><td>'+(tempData.cjphzGold/10000).toFixed(0)+'万('+tempData.cjphzGold/200+'钻)</td><td>'+((tempData.cjphzGold/tempTotal)*100).toFixed(0)+'%</td></tr>'
                                +'<tr><td>跑胡子中级场服务费</td><td>'+(tempData.zjphzGold/10000).toFixed(0)+'万('+tempData.zjphzGold/200+'钻)</td><td>'+((tempData.zjphzGold/tempTotal)*100).toFixed(0)+'%</td></tr>'
                                +'<tr><td>跑胡子高级场服务费</td><td>'+(tempData.gjphzGold/10000).toFixed(0)+'万('+tempData.gjphzGold/200+'钻)</td><td>'+((tempData.gjphzGold/tempTotal)*100).toFixed(0)+'%</td></tr>'
                                +'<tr><td>跑胡子总服务费</td><td>'+(tempData.totalphzService/10000).toFixed(0)+'万('+tempData.totalphzService/200+'钻)</td><td>'+((tempData.totalphzService/tempTotal)*100).toFixed(0)+'%</td></tr>'
                            // }

                            newRow+='<tr><td>兑换积分</td><td>'+tempData.exchargeGold+'</td><td></td></tr>'
                            +'<tr><td>回兑钻石</td><td>'+tempData.exchargeCard+'</td><td></td></tr>'
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