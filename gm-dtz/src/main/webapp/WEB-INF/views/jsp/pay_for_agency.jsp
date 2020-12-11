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
        .text-input{
            height:25px;width: 65%;color: #0d6cac;font-size: 16px;
        }
    </style>
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.2">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
</head>

<body style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<table class="header_table" cellpadding="0" cellspacing="0">
    <tr>
        <td class="fix_td" onclick="history.back()"><img class="header_img1" src="<%=basePath%>/image/back.png"/></td>
        <td><img src="<%=basePath%>/image/pay_icon3.png" class="header_img2" style="width: 120px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div style="text-align: center;width: 100%;">

    <table cellpadding="0" cellspacing="15" style="width: 100%;">
        <tr>
            <td colspan=2>
                <span id="rest">我的钻石数：${sessionScope.roomCard.commonCard}</span>
            </td>
        </tr>
        <tr>
            <td >
            </td>
            <td>
                <select id="myAgency" class="text-input" style="width: 65%;" onchange="changeAgencyId()">
                    <option value="" selected="selected">请选择代理商</option>
                </select>
            </td>
        </tr>
         <tr>
            <td >
                                                   代理商邀请码:
            </td>
            <td>
                <input id="agencyId" class="text-input" style="margin-top: 15px;" type="text" pattern="[0-9]*" placeholder="代理商邀请码">
            </td>
        </tr>
        <tr>
            <td >
                                                   充值钻石数:
            </td>
            <td id="input_text">
                <input id="count" class="text-input" type="text" pattern="[0-9]*" placeholder="充值钻石数">
            </td>
        </tr>
        
        <tr id='free'>
            <td colspan=2>
                <input  name="isFree" id="isFree" type="checkbox" value=1> 赠送
            </td>
        </tr>
        
        <tr>
            <td colspan=2>
                <input type="button" value="确 认" onclick="save()" style="width:80px;height:40px;">
            </td>
        </tr>
        <tr id='tt'>
            <td colspan=2>
                                                          最近10条充值记录
            </td>
        </tr>
    </table>
</div>
<div class="table2" style="margin-top: 2px;">
    <table id="table_data" style="width: 100%;text-align: center;font-size: 16px;" cellspacing="0" cellpadding="10">
        <tr style="color: #ee6a2a;font-size: 18px;">
            <td>邀请码</td>
            <td>姓名</td>
            <td>钻石数</td>
            <td>充值时间</td>
        </tr>
    </table>
</div>
<input type="hidden" id="admin" value="${sessionScope.roomCard.partAdmin}"/>
<input type="hidden" id="me" value="${sessionScope.roomCard.agencyId}"/>
<input type="hidden" id="agencyLevel" value="${sessionScope.roomCard.agencyLevel}"/>
</body>

<script>
  $(function(){
         if($("#agencyLevel").val()!=99){
             $("#free").remove();
         }
         czxx();
     }); 
     
     function czxx(){
      $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/get/agency/card/record",
            data: {agencyId:$("#me").val(),gameId:localStorage.getItem('gameId')},
            dataType: "json",
            success: function(result){
                if (result.code==1000){
                    $("#table_data").html('<tr style="color: #ee6a2a;font-size: 18px;"><td>邀请码</td><td>姓名</td><td>钻石数</td><td>充值时间</td></tr>');
                    $.each(result.data,function (index,tempData){
                      var tempTime;
                        if (!isNaN(tempData.createTime)){
                            var tmpDate=new Date();
                            tmpDate.setTime(tempData.createTime);
                            tempTime=tmpDate.format("yyyy-MM-dd hh:mm:ss");
                        }else{
                            tempTime=tempData.createTime;
                        }
                        var newRow="<tr><td>"+tempData.reactiveUserId+"</td><td>"+tempData.agencyName+"</td><td>"+(tempData.roomCardNumber)+"</td><td>"+tempTime+"</td></tr>";
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
     function selectvalue(index) {
       var agencyId = "agencyId"+index;
       var nameId = "nameId"+index;
       var phone = "phone"+index;
       var agencyIds = $("#"+agencyId).html();
       var name = $("#"+nameId).html();
       var phones = $("#"+phone).html();
       $("#agencyId").val(agencyIds);
    }
    
    $(document).ready(function () {

        /* if ("1"==$("#admin").val()){
            var temp='<select id="count" class="text-input" style="width: 65%;"><option value="">请选择充值额</option>';
            temp+='<option value="8000">8000钻石（200元）</option>';
            temp+='<option value="21000">21000钻石（500元）</option>';
            temp+='<option value="44000">44000钻石（1000元）</option>';
            temp+='<option value="92000">92000钻石（2000元）</option>';
            temp+='<option value="144000">144000钻石（3000元）</option>';
            temp+='<option value="250000">250000钻石（5000元）</option>';
            temp+='<option value="416000">416000钻石（8000元）</option>';
            temp+='<option value="540000">540000钻石（10000元）</option>';
            temp+='<option value="1680000">1680000钻石（30000元）</option>';
            temp+='<option value="2900000">2900000钻石（50000元）</option>';
            temp+='<option value="6000000">6000000钻石（100000元）</option>';

            temp=temp+'</select>';
            $("#input_text").html(temp);
        }else{
            $("#input_text").html('<input id="count" class="text-input" type="text" pattern="[0-9]*" placeholder="钻石数">')
        } */

        loadAgencies();
    });

    function changeAgencyId() {
        var agencyId = $("#myAgency").val().trim();
        if (agencyId==""||isNaN(agencyId)){
            $("#agencyId").val("");
            $("#agencyId").removeAttr("disabled");
        }else{
            $("#agencyId").val(agencyId);
            $("#agencyId").attr("disabled","disabled");
        }
    }

    function loadAgencies() {
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/agency/info/list",
            data: {
                gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if(result.code==1000){
                    $("#myAgency").html('<option value="">请选择代理商</option>');
                    $.each(result.datas,function (index,tempData){
                        var temp;
                        if (tempData.userName){
                            temp=tempData.userName;
                        }else{
                            temp=tempData.agencyPhone;
                        }
                        var newRow='<option value="'+tempData.agencyId+'">'+tempData.agencyId+'('+temp+')</option>';
                        $('#myAgency').append(newRow);
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

    function save() {
        var options=$("#myAgency option:selected");
        var agencyId = $("#agencyId").val().trim();
        var count = $("#count").val().trim();
        var isFree ;
        var oCheckbox = document.getElementsByName("isFree");
	    for(var i=0;i<oCheckbox.length;i++)
	     {
	          if(oCheckbox[i].checked)
	          {    
	             isFree = oCheckbox[i].value;
	          }
	     }
        if (agencyId==""||isNaN(agencyId)){
            alert("请输入充值ID");
            return;
        }

        if (count==""||isNaN(count)||parseInt(count)<=0){
            alert("请输入充值金额");
            return;
        }
        if(count <3000){
            alert("充值钻石必须大于3000钻石");
            return;
        }

        if($("#me").val()==agencyId){
            alert("不能给自己充值");
            return;
        }

        var agencyLevel=$("#agencyLevel").val();

        if (count<=0&&(agencyLevel==""||isNaN(agencyLevel)||parseInt(agencyLevel)<99)){
            alert("代理单笔充值不能少于0钻!");
            return;
        }

        var a=confirm("您确认要给"+(agencyId==options.val()?options.text():agencyId)+"充值"+count+"钻石吗？");
        if (a==true)
            $.ajax({
                timeout: 60000,
                async: true,
                type: "POST",
                url: "<%=path%>/pay/agency",
                data: {
                    agencyId:agencyId,
                    count:count,
                    isFree:isFree,gameId:localStorage.getItem('gameId')
                },
                dataType: "json",
                success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json

                    if (result.code==1000){
                        $("#rest").html("我的钻石数:"+result.rest);
                        $("#count").val("");
                        loadAgencies();
                        czxx();
                    }

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