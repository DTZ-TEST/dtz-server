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
        <td><img src="<%=basePath%>/image/pay_icon4.png" class="header_img2" style="width: 120px;"/></td>
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

        <tr align="center">
          <td >
                                                   玩家ID:
            </td>
            <td>
                <input id="playerId" class="text-input" type="text" pattern="[0-9]*" placeholder="玩家ID" onblur="loadUserInfo()" onfocus="clearMsg()">
                <!-- <span style="display: block" id="playerName"></span>
                <img id="myIco" style="width: 90px;height: 90px;display: none;"/> -->
            </td>
        </tr>
        <tr>
            <td>
                                                    昵称:
            </td>
            <td>
                <input id="playerName" class="text-input" type="text" placeholder="昵称"  disabled="disabled">
            </td>
        </tr>
        <tr>
             <td>
                                                    玩家现有钻石数:
            </td>
            <td>
                <input id="playerCards" class="text-input" type="text" placeholder="玩家现有钻石数"  disabled="disabled">
            </td>
        </tr>
        <tr>
            <td>
                                                   充值钻石数:
            </td>
            <td>
                <input id="count" class="text-input" type="text" pattern="[0-9]*" placeholder="充值钻石数">
            </td>
        </tr>
        
        <tr id='free'>
            <td colspan=2>
                <input name="isFree"  type="checkbox"  value=1> 赠送
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
    
    <div class="table2" style="margin-top: 2px;">
    <table id="table_data" style="width: 100%;text-align: center;font-size: 18px;" cellspacing="0" cellpadding="10">
        <tr style="color: #ee6a2a;font-size: 20px;">
            <td>ID</td>
            <td>昵称</td>
            <td>钻石数</td>
            <td>充值时间</td>
        </tr>
    </table>
</div>

    <input type="hidden" id="agencyLevel" value="${sessionScope.roomCard.agencyLevel}"/>
    <input type="hidden" id="agencyId" value="${sessionScope.roomCard.agencyId}"/>
</div>

</body>

<script>

   $(function(){
         czxx();
     }); 
    
    function czxx(){
    $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/get/player/card/record",
            data: {agencyId:$("#agencyId").val(),gameId:localStorage.getItem('gameId')},
            dataType: "json",
            success: function(result, status, request){
                if (result.code==1000){
                
                    $("#table_data").html('<tr style="color: #ee6a2a;font-size: 20px;"><td>ID</td><td>昵称</td><td>钻石数</td><td>充值时间</td></tr>');
                    $.each(result.data,function (index,tempData){
                        var tempTime;
                        if (!isNaN(tempData.createTime)){
                            var tmpDate=new Date();
                            tmpDate.setTime(tempData.createTime);
                            tempTime=tmpDate.format("yyyy-MM-dd hh:mm:ss");
                        }else{
                            tempTime=tempData.createTime;
                        }
                        var newRow="<tr><td>"+tempData.roleId+"</td><td>"+tempData.playerName+"</td><td>"+(tempData.commonCards+tempData.freeCards)+"</td><td>"+tempTime+"</td></tr>";
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
    $(document).ready(function () {

    });
    function clearMsg() {
        $('#playerName').html('');
        $("#myIco").css("display","none");
    }
    
    function selectvalue(index) {
        var userId = "userId"+index;
        var nameId = "nameId"+index;
        var userIds = $("#"+userId).html();
        var name = $("#"+nameId).html();
        $("#playerId").val(userIds);
        $("#playerName").val(name);
    }
    
    function loadUserInfo() {

        var playerId = $("#playerId").val().trim();
        var value1 = playerId.replace(/[^0-9]/ig,""); 
        
     	 $("#playerId").val(value1.substring(0,6));
        var playerIdD = $("#playerId").val().trim();
          if (playerIdD==""||isNaN(playerIdD)){
            $("#count").focus();
             alert("请输入玩家ID"); 
            return;
        }   

        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/player/info",
            data: {
                userId:playerIdD,gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if(result.code==1000){
                    var dataMsg=result.info;
                    var myName;
                    if(dataMsg.hasOwnProperty("name")){
                        myName=dataMsg.name;
                    }else{
                        myName=dataMsg.userId;
                    }
                   /*  if(dataMsg.hasOwnProperty("headimgurl")&&$.trim(dataMsg.headimgurl).length>0){
                        $("#myIco").attr("src",dataMsg.headimgurl);
                        $("#myIco").css("display","block");
                    }else{
                        $("#myIco").css("display","none");
                    }
                    */
                    $('#playerName').val(myName);
                    $('#playerCards').val(dataMsg.cards+dataMsg.freeCards);
                }else{
                    $("#myIco").css("display","none");
                    $("#playerId").focus();
                    $('#playerName').html("");
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

    function save() {

        var playerId = $("#playerId").val().trim();
        var count = $("#count").val().trim();
        var oCheckbox = document.getElementsByName("isFree");
	    var isFree ;
	    var pattern = /\d{6}/; 
	    var numCnt = playerId.replace(/\D/g, '').length;
	    var playerID=pattern.exec(playerId);
	     for(var i=0;i<oCheckbox.length;i++)
	     {
	          if(oCheckbox[i].checked)
	          {    
	             isFree = oCheckbox[i].value;
	          }
	     }
	    if(numCnt>6){
	    	alert("请输入正确充值ID,统一为6位数");
            return;
	     }
	    if(pattern.test(playerId)==false){
	        	alert("请输入正确充值ID,统一为6位数");
	            return;
	        }
/*         if (playerID==""||playerID==null){
            alert("请输入充值ID");
            return;
        } */
        

        if (count==""||isNaN(count)){
            alert("请输入充值金额");
            return;
        }

        var a=confirm("您确认要给"+playerID+"（"+$('#playerName').html()+"）充值"+count+"钻石吗？");
        if (a==true)
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/pay/player",
            data: {
                playerId:playerID,
                isFree:isFree,
                count:count,gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {

                    if (result.code==1000){
                        $("#rest").html("我的钻石数:"+result.rest);
                        loadUserInfo();
                        czxx();
                         $("#count").val("");
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