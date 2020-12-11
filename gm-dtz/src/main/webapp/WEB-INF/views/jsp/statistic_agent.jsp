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
        <td><img src="<%=basePath%>/image/agent.png" class="header_img2" style="width: 94px;"/></td>
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
        <tr style="color: #ee6a2a;font-size: 10px;">
            <td>日期</td>
            <td>总代理</td>
            <td>新增代理</td>
            <td>付费代理</td>
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
            url: "<%=path%>/data/statistics/agentList",
            data: {startDate:$("#start_time").val(),endDate:$("#end_time").val(),
                gameId:localStorage.getItem("gameId")},
            success: function(result){
                result=JSON.parse(result);//json转字符串
                 if (result.code==1000){
                    $("#table_data").html('<tr style="color: #ee6a2a;font-size: 20px;"><td>日期</td><td>总代理</td><td>新增代理</td><td>付费代理</td></tr>');
                    $.each(result.datas,function (index,tempData){
                         var tempTime=tempData.dateTime;

                        var tmpData1=0;
                        var tmpData2=0;
                        var tmpData3=0;

                        var tmpData4=0;
                        var tmpData5=0;

                        var tmpData6=0;
                        var tmpData7=0;

                        var tmpData8=0;
                        var tmpData9=0;

                        var tmpData10=0;
                        if(tempData.agencyTotal) {
                            var tmp=tempData.agencyTotal;
                            if(tmp.trim().length>0){
                                var tmps=tmp.split(",");
                                for(var i=0;i<tmps.length;i++){
                                    var tmpVar=tmps[i];
                                    if (tmpVar.trim().length>0){
                                        var tmpVars = tmpVar.split("_");
                                        if (tmpVars[0]=="0"){
                                            tmpData8=tmpVars[1];
                                            tmpData10+=parseInt(tmpVars[1],10);
                                        }else if (tmpVars[0]=="1"){
                                            tmpData6=tmpVars[1];
                                            tmpData10+=parseInt(tmpVars[1],10);
                                        }
                                        else if (tmpVars[0]=="2"){
                                            tmpData4=tmpVars[1];
                                            tmpData10+=parseInt(tmpVars[1],10);
                                        }
                                        else if (tmpVars[0]=="all"){
                                            tmpData1=tmpVars[1];
                                        }
                                    }
                                }
                            }
                        }
                         if (tempData.agencyCount){
                             var tmp=tempData.agencyCount;
                             if(tmp.trim().length>0){
                                 var tmps=tmp.split(",");
                                 for(var i=0;i<tmps.length;i++){
                                     var tmpVar=tmps[i];
                                     if (tmpVar.trim().length>0){
                                         var tmpVars = tmpVar.split("_");
                                         if (tmpVars[0]=="all"){
                                             tmpData2=tmpVars[1];
                                         }
                                     }
                                 }
                             }
                         }
                        if (tempData.payCount){
                            var tmp=tempData.payCount;
                            if(tmp.trim().length>0){
                                var tmps=tmp.split(",");
                                for(var i=0;i<tmps.length;i++){
                                    var tmpVar=tmps[i];
                                    if (tmpVar.trim().length>0){
                                        var tmpVars = tmpVar.split("_");
                                        if (tmpVars[0]=="all"){
                                            tmpData3=tmpVars[1];
                                        }
                                    }
                                }
                            }
                        }
                        if (tempData.payTotal){
                            var tmp=tempData.payTotal;
                            if(tmp.trim().length>0){
                                var tmps=tmp.split(",");
                                for(var i=0;i<tmps.length;i++){
                                    var tmpVar=tmps[i];
                                    if (tmpVar.trim().length>0){
                                        var tmpVars = tmpVar.split("_");
                                        if (tmpVars[0]=="0"){
                                            tmpData9=tmpVars[1];
                                        }else if (tmpVars[0]=="1"){
                                            tmpData7=tmpVars[1];
                                        }
                                        else if (tmpVars[0]=="2"){
                                            tmpData5=tmpVars[1];
                                        }
//                                        else if (tmpVars[0]=="all"){
//                                            tmpData1=tmpVars[1];
//                                        }
                                    }
                                }
                            }
                        }

                        var bl1="0%";
                        var bl2="0%";
                        var bl3="0%";
                        var bl4="0%";
                        var bl5="0%";
                        var bl6="0%";
                        if (tmpData10>0){
                            bl1=(100*tmpData4/tmpData10).toFixed(2)+"%";
                            bl3=(100*tmpData6/tmpData10).toFixed(2)+"%";
                            bl5=(100*tmpData8/tmpData10).toFixed(2)+"%";
                        }
                        if (tmpData4>0){
                            bl2=(100*tmpData5/tmpData4).toFixed(2)+"%";
                        }
                        if (tmpData6>0){
                            bl4=(100*tmpData7/tmpData6).toFixed(2)+"%";
                        }
                        if (tmpData8>0){
                            bl6=(100*tmpData9/tmpData8).toFixed(2)+"%";
                        }


                          var newRow="<tr onclick='displayData(this.id)' id='data"+tempTime+"'><td>"+tempTime+"</td><td>"+tmpData1+"</td><td>"+tmpData2+"</td><td>"+tmpData3 +"</td></tr>";
                        newRow+= '<tr id="detail'+tempTime+'" style="display: none"><td colspan="4"><table class="table3" style="width: 100%;text-align: left;background-color: #f3ffea;" cellspacing="0" cellpadding="4">'
                        +'<tr><td>顶级VIP</td><td>'+tmpData4+'('+bl1+')'+'</td><td>付费代理 </td><td>'+tmpData5+'('+bl2+')'+'</td></tr>'
                        +'<tr><td>超级VIP</td><td>'+tmpData6+'('+bl3+')'+'</td><td>付费代理 </td><td>'+tmpData7+'('+bl4+')'+'</td></tr>'
                        +'<tr><td>VIP代理</td><td>'+tmpData8+'('+bl5+')'+'</td><td>付费代理 </td><td>'+tmpData9+'('+bl6+')'+'</td></tr>'
                        ;
                        $('#table_data').append(newRow);  
                    });
                }else{
                    alert(result.message);
                } 
            },
            error : function( req, status, err) {
                console.info(status+","+err);
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