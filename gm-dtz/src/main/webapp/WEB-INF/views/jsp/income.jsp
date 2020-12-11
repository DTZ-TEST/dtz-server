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
        <td><img src="<%=basePath%>/image/daycash.png" class="header_img2" style="width: 142px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div id="pp" style="margin-top: 10px;text-align: center" >
     <span id="page" style="color:blue;text-align: center">提现周期T+2，1号的佣金将在3号显示提现按钮</span><br/>
</div>
<div class="table2">
    <table style="width: 100%;text-align: center;font-size: 20px;border-top: none;border-bottom: none;" cellspacing="0" cellpadding="10">
        <tr>
            <td style="text-align: left;width: 30%"><input type="month" placeholder="请选择月份" id="month"/></td>
             <td>
                <input name="type"  type="checkbox"  value=1> 只显示未提现
            </td>
            <td ><input type="button"  value="查询" onclick="selectDatas()"/></td>
        </tr>
    </table>
    <table id="table_data" style="width: 100%;text-align: center;font-size: 20px;" cellspacing="0" cellpadding="10">
        <%--<tr>--%>
        <%--<td style="text-align: left">2017.04.01-2017.04.07</td>--%>
        <%--<td style="text-align: right">0</td>--%>
        <%--<td style="text-align: right;width: 16px;"><img src="<%=basePath%>/image/jiantou.png" style="margin-top: 3px;width: 16px;height: 26px;"/></td>--%>
        <%--</tr>--%>
        <%--<tr>--%>
        <%--<td style="text-align: left">2017.04.08-2017.04.14</td>--%>
        <%--<td style="text-align: right">0</td>--%>
        <%--<td style="text-align: right;width: 16px;"><img src="<%=basePath%>/image/jiantou.png" style="margin-top: 3px;width: 16px;height: 26px;"/></td>--%>
        <%--</tr>--%>
    </table>
</div>
<input type="hidden" id="agencyLevel" value="${sessionScope.roomCard.agencyLevel}"/>
<input type="hidden" id="openid" value="${sessionScope.roomCard.openid}">
<input type="hidden" id="cash_code_state" value="${sessionScope.cash_code_state}"/>
<input type="hidden" id="appid" value="${wx_appid}"/>
</body>
</body>

<script>
    var agencyLevel=0;
    var agencyRatio=0;
    var month;
    $(document).ready(function () {
        agencyLevel=parseInt($("#agencyLevel").val(),10);
        if (agencyLevel!=99){
            agencyRatio=0.1;
        }
        month=$("#month").val();
        if ($.trim(month).length==0){
            $("#month").val(new Date().format("yyyy-MM"));
            selectDatas();
        }
    });

    function selectDatas() {
     var oCheckbox = document.getElementsByName("type");
	    var type ;
	     for(var i=0;i<oCheckbox.length;i++)
	     {
	          if(oCheckbox[i].checked)
	          {    
	             type = oCheckbox[i].value;
	          }
	     }
        month=$("#month").val();
        $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/income/count2",
            data: {month:month,type:type,gameId:localStorage.getItem('gameId')},
            dataType: "json",
            success: function(result){
                if (result.code==1000){
                    var datas=result.datas;
//                    $("#td_total").html(result.month+"月总金额(￥"+(datas.month*tempAgencyRatio).toFixed(2)+")");
                    $("#td_total").html(result.month+"月");
                    $('#table_data').html('');
                    myObjs=new Array();
                    var idx=0;
                    var currrentNum=0;
                    
                    $.each(result.datas,function (index,tempData){
                        for(var i in tempData){
                        if (tempData.hasOwnProperty(i)&&i!="month"&&i.indexOf("a")==-1&&i.indexOf("t")==-1&&i.indexOf("m")==-1&&i.indexOf("d")==-1) { //filter,只输出man的私有属性
                            var yuan=(tempData[i]*1.0/100).toFixed(2);
                            var newRow='<tr> <td style="text-align: left">'+i+'</td> <td style="text-align: right">'+yuan+'</td>';
                            if(result.monthend==i){
                               newRow='<tr> <td style="text-align: left">'+result.month+'(月末补足)</td> <td style="text-align: right">'+yuan+'</td>';
                            }
                            var currentState = tempData["a"+i];
                            myObjs[idx]={date:i,state:currentState,rmb:yuan,type:tempData["t"+i]};
                            if (datas[i]==0){
                                newRow+='<td> </td>';
                            }else{
                                if("0"==currentState||"-1"==currentState){
                                    newRow+=('<td><span id="myObj'+idx+'" style="color: blue;text-underline: none;" onclick="cash(this.id.substring(5));">提现</span></td>');
                                }else if("2"==currentState){
                                    newRow+='<td><span id="myObj'+idx+'" style="color: red;text-underline: none;">提现中</span></td>';
                                }else if("-2"==currentState){
                                    newRow+='<td><span id="myObj'+idx+'" style="color: red;text-underline: none;"></span></td>';
                                }else{//1,2,3
                                    newRow+='<td><span style="color: green;">已提现</span></td>';
                                }
                            }
                            var rr=0;
                            var rrr="40%";
                            if(yuan >0){
                             newRow+='<td id="data'+index+'" style="text-align: right;width: 16px;"  onclick="displayData(this.id)"><img src="<%=basePath%>/image/jiantou.png" style="margin-top: 3px;width: 16px;height: 26px;"/></td></tr>';
                             newRow+='<tr id="detail'+index+'" style="display: none"><td colspan="3"><table class="table3" style="width: 100%;text-align: left;background-color: #f3ffea;" cellspacing="0" cellpadding="4">';
                             newRow=newRow
                                        +'<tr><td>我的充值</td><td>返佣比例</td><td>返佣金额</td></tr>'
                                        +'<tr><td>'+tempData["m"+i]/10+'</td><td>'+rrr+'</td><td>'+(tempData["m"+i]*0.4/10).toFixed(2)+'</td></tr>'
                                        +'<tr><td>代理充值</td><td>返佣比例</td><td>返佣金额</td></tr>'
                                        +'<tr><td>'+tempData["d"+i]/10+'</td><td>'+(0.1*100)+'%</td><td>'+(tempData["d"+i]*0.1/10).toFixed(2)+'</td></tr>';
                            }else{
                               newRow+='<td></td></tr>';
                            }
                            $('#table_data').append(newRow);
                            idx++;
                        }
                    }
                    });
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
        var a=document.getElementById(tempId);
        if (a!=null&&a!=undefined){
            if ($("#"+tempId).css("display")=="none"){
                $("#"+tempId).css("display","");
            }else{
                $("#"+tempId).css("display","none");
            }
        }
    }
function cash(obj) {
        console.log(obj+":"+JSON.stringify(myObjs[obj]));
        if (myObjs!=null){
            if (parseFloat(myObjs[obj].rmb)<1){
                alert("最低可提现金额为1元");
            }else{
                if ("0"==myObjs[obj].state){
                    var openid=$("#openid").val();
                    if($.trim(openid)==""){
                        var authUrl="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+$('#appid').val()+"&redirect_uri="+encodeURIComponent('http://gm.688gs.com/pdkuposa/redirect/dtz')+"&response_type=code&scope=snsapi_base&state="+$('#cash_code_state').val()+"#wechat_redirect";
                        window.location=authUrl;
                    }else{
                        var a=confirm("提现金额将直接发放至微信零钱中，确定提现吗？");
                        if (a==true){
                            $.ajax({
                                timeout: 60000,
                                async: true,
                                type: "POST",
                                url: "<%=path%>/pay/cash/income",
                                data: {
                                    dateMsg:myObjs[obj].date,type:myObjs[obj].type,gameId:localStorage.getItem('gameId')
                                },
                                dataType: "json",
                                success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json

                                    if (result.code==1000){
                                        alert("提现成功");
                                        selectDatas();
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
                    }
//                    alert("即将开放，敬请期待");
                }else{
                    alert("本周的佣金在下周一12点之后可提取");
                }
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