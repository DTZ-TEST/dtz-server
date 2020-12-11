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
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.2">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
</head>
<body style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<table class="header_table" cellpadding="0" cellspacing="0">
    <tr>
        <td class="fix_td" onclick="history.back()"><img class="header_img1" src="<%=basePath%>/image/back.png"/></td>
        <td><img src="<%=basePath%>/image/sdszbl.png" class="header_img2" style="width: 142px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3"
                                                                                src="<%=basePath%>/image/home.png"/>
        </td>
    </tr>
</table>
<table cellpadding="5" style="font-size:35px;text-align: center;margin-top: 10px;width: 100%;padding-bottom: 10px;">
   <tr>
        <td colspan="1" style="text-align: center" >
            <input id="agencyId" class="text-input" style="margin-top: 15px;" type="text" placeholder="代理邀请码" onblur="fn()" value="100000">
        </td>
    </tr>
    <tr>
        <td colspan="1" style="text-align: center" >
            <input id="playerId" class="text-input" style="margin-top: 15px;" type="text" placeholder="玩家id" onblur="fc()" disabled="disabled">
        </td>
    </tr>
    <tr>
        <td colspan="1" style="text-align: center">
           <input id="money" class="text-input" style="margin-top: 15px;" type="text" placeholder="补录金额">
         </td>
    </tr>
    <tr>
        <td><input onclick="submitOrder()" type="button" value="确定补录"/></td>
    </tr>
</table>
<script>
   function fn(){
	   var wj= agencyId.value;
	   if(wj.length>=1){
		   document.getElementById("playerId").disabled=true;
	   }else{
		   document.getElementById("playerId").disabled=false; 
	   }
   }
   function fc(){
	   var dl= playerId.value;
	  /*  var wj= agencyId.value; */
	   if(dl.length>=1){
		   document.getElementById("agencyId").disabled=true;
	   }else{
		   document.getElementById("agencyId").disabled=false; 
	   }
      
   }
    function submitOrder() {
        var agencyId = $("#agencyId").val();
         var playerId = $("#playerId").val();
        if (!(agencyId != "" || playerId!="")) {
            alert("玩家id不能为空或者邀请码不能为空！");
            return;
        }
        var money = $("#money").val();
        if (money == "") {
            alert("购钻金额不能为空！");
            return;
        }
        if (isNaN(agencyId) || isNaN(playerId) ) {
            alert("代理商邀请码不能为非数字！");
            return;
        }
        if (isNaN(money)) {
            alert("购钻金额不能为非数字！");
            return;
        }
        if (money <= 0) {
            alert("购钻金额必须大于零！");
            return;
        }
        ret = confirm("确定要为" + agencyId+"的代理的玩家"+playerId + "补录" + money + "元吗？")
        if (ret) {
            $.ajax({
                timeout: 60000,
                async: true,
                type: "POST",
                url: "<%=path%>/pay/offlinePay",
                data: {money: money, agencyId: agencyId,playerId:playerId,gameId:localStorage.getItem('gameId')},
                dataType: "json",
                success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                    if (result.code == 1000) {
                        $("#money").val("");
                        alert("补录成功!");
                    } else {
                        alert("补录失败！原因：" + result.message);
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
    }
</script>
</body>
</html>