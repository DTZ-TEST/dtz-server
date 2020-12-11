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
        <td><img src="<%=basePath%>/image/agency_pay.png" class="header_img2" style="width: 142px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<table cellpadding="5" style="text-align: center;margin-top: 10px;border-bottom: 1px solid #b5b5b5;width: 100%;padding-bottom: 10px;">
    <tr>
        <td colspan="1" style="text-align: left">
            <%--<input id="agency_id"  type="text" pattern="[0-9]*" placeholder="代理id" style="width: 85%;height: 25px;"/>--%>
                <select id="agency_id" class="text-input" style="width: 95%;height: 30px;">
                    <option value="" selected="selected">全部</option>
                </select>
        </td>
        <td colspan="2" style="font-size: 20px;">默认查询所有</td>
    </tr>
    <tr>
        <td style="text-align: left"><input id="start_time" value="${requestScope.startDate}" type="date" placeholder="开始时间" style="width: 85%;height: 25px;"/></td>
        <td><input id="end_time" type="date" value="${requestScope.endDate}" placeholder="结束时间" style="width: 85%;height: 25px;"/></td>
        <td><input onclick="myQuery()" type="button" value="查询"/></td>
    </tr>
</table>
<div class="table2" style="margin-top: 2px;">
    <img id="loading" style="display: none;" src="<%=basePath%>/image/loading.gif">
    <table id="table_data" style="width: 100%;text-align: center;font-size: 18px;" cellspacing="0" cellpadding="10">
        <tr style="color: #ee6a2a;font-size: 20px;">
            <td>邀请码</td>
            <td>充值金额</td>
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

    $(document).ready(function () {
        loadAgencies();
    });

    function loadAgencies() {
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/agencies/detail",
            data: {
                gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if(result.code==1000){
                    $("#agency_id").html('<option value="">全部</option>');
                    $.each(result.datas,function (index,tempData){
                        var temp;
                        if (tempData.userName){
                            temp=tempData.userName;
                        }else{
                            temp=tempData.agencyPhone;
                        }
                        var newRow='<option value="'+tempData.agencyId+'">'+tempData.agencyId+'('+temp+')</option>';
                        $('#agency_id').append(newRow);
                    });
                }else
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

    function loadRow(datas,color) {
        var rowStr="";
        $.each(datas,function (index,tempData){
            var tempId=tempData.userId + "_"+index;

            var newRow="";

            if(tempData.hasOwnProperty("subList")){
                if (tempData.subList.length>0){
                    var tempRowSub='<tr id="detail'+tempId+'" style="display: none"><td colspan="3"><table class="table3" style="width: 100%;text-align: left;background-color: '+color+'" cellspacing="0" cellpadding="2">';

                    tempRowSub+='<tr><td>'+tempData.agencyId+'</td><td colspan="2">'+(tempData.minePay/10)+'</td></tr>';

                    tempRowSub+=loadRow(tempData.subList,color=="#f3ffea;"?"#F5FF82;":"#f3ffea;");

                    tempRowSub+='</table></td></tr>';

                    newRow="<tr onclick='displayData(this.id)' id='data"+tempId+"'><td>"+tempData.agencyId+"</td><td>"+(tempData.totalPay/10)+"</td><td style='width:20px;'><img style='width:20px;height:12px;' id='img"+tempId+"' src='<%=basePath%>/image/a.png'/></td></tr>";
                    newRow+=tempRowSub;
                }
            }
            if (newRow==""){
                newRow="<tr onclick='displayData(this.id)' id='data"+tempId+"'><td>"+tempData.agencyId+"</td><td colspan='2'>"+(tempData.totalPay/10)+"</td></tr>";
            }

            rowStr+=newRow;
        });
        return rowStr;
    }

    var loading=false;
    function myQuery() {

        if (loading){
            return;
        }else{
            loading=true;
        }

        $("#table_data").css("display","none");
        $("#loading").css("display","");

        $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/agency/pay/detail",
            data: {gameId:localStorage.getItem('gameId'),userId:$("#agency_id").val(), startDate:$("#start_time").val(),endDate:$("#end_time").val()},
            dataType: "json",
            success: function(result){
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json

                $("#loading").css("display","none");
                $("#table_data").css("display","");

                loading=false;

                if (result.code==1000){
                    $("#start_time").val(result.startDate);
                    $("#end_time").val(result.endDate);

                    $("#table_data").html('<tr style="color: #ee6a2a;font-size: 20px;"><td>邀请码</td><td colspan="2">充值金额</td></tr>');
                    var rowStr="";
                    $.each(result.datas,function (index,tempData){
                        var tempId=tempData.userId + "_"+index;

                        var newRow="";

                        if(tempData.hasOwnProperty("subList")){
                            if (tempData.subList.length>0){
                                var tempRowSub='<tr id="detail'+tempId+'" style="display: none"><td colspan="3"><table class="table3" style="width: 100%;text-align: left;background-color: #f3ffea;" cellspacing="0" cellpadding="2">';

                                tempRowSub+='<tr><td>'+tempData.agencyId+'</td><td  colspan="2">'+(tempData.minePay/10)+'</td></tr>';

                                tempRowSub+=loadRow(tempData.subList,"#F5FF82;");

                                tempRowSub+='</table></td></tr>';

                                newRow="<tr onclick='displayData(this.id)' id='data"+tempId+"'><td>"+tempData.agencyId+"</td><td>"+(tempData.totalPay/10)+"</td><td style='width:20px;'><img style='width:20px;height:12px;' id='img"+tempId+"' src='<%=basePath%>/image/a.png'/></td></tr>";
                                newRow+=tempRowSub;
                            }
                        }
                        if (newRow==""){
                            newRow="<tr onclick='displayData(this.id)' id='data"+tempId+"'><td>"+tempData.agencyId+"</td><td colspan='2'>"+(tempData.totalPay/10)+"</td></tr>";
                        }

                        rowStr+=newRow;
                    });
                    $("#table_data").append(rowStr);
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
        var tempId1=currentDate.replace("data","img");
        var a=document.getElementById(tempId);
        if (a!=null&&a!=undefined){
            if ($("#"+tempId).css("display")=="none"){
                $("#"+tempId).css("display","");
                $("#"+tempId1).attr("src","<%=basePath%>/image/b.png");
            }else{
                $("#"+tempId).css("display","none");
                $("#"+tempId1).attr("src","<%=basePath%>/image/a.png");
            }
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