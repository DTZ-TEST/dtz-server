<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String path = request.getContextPath() + "";
    String basePath = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() == 80 ? "" : (":" + request.getServerPort())) + request.getContextPath();

    String noticePay = String.valueOf(request.getSession().getAttribute("notice_pay"));
    if (!"null".equals(noticePay)){
        request.getSession().removeAttribute("notice_pay");
    }

    String orderConfirm = String.valueOf(request.getSession().getAttribute("order_confirm"));
    if (!"null".equals(orderConfirm)){
        request.getSession().removeAttribute("order_confirm");
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <%--<meta http-equiv="refresh" content="30">--%>
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
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img1" src="<%=basePath%>/image/back.png"/></td>
        <td><img src="<%=basePath%>/image/delegate_pay.png" class="header_img2" style="width: 94px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div>
    <table style="width: 100%;" align="center" cellpadding="9">
        <tr  align="center">
            <td style="width: 50%;">
                <input id="agencyId" class="text-input" style="width: 100%;" type="text" pattern="[0-9]*" placeholder="代充的邀请码">
            </td>
            <td><input type="button" value="查询" onclick="queryAgency()"></td>
        </tr>
        <tr align="center">
            <td id="td_agencyName">姓名:--</td>
            <td id="td_agencyPhone">手机:--</td>
        </tr>
    </table>
</div>
<div id="div_buycards" class="table" style="margin-top: 10px;margin-bottom:10px;text-align: center;display: none">
    <div id="buycards" class="table2" style="margin-top:15px;margin-bottom: 40px;width:100%;font-size:18px;">
        <%--<form action="<%=path%>/pay/create" name="form" id="form" method="post" onsubmit="return checkSubmit();">--%>
            <table id="mytable" cellspacing="0" style="width:100%;text-align:left;font-size: 16px;padding: 0px;">
            <tr align="center" style="height:35px;">
                <%--钻石  赠送   价格   折扣--%>
                <td align="center">钻石</td>
                <td align="center">赠送</td>
                <td align="center">价格</td>
                <td align="center">折扣</td>
            </tr>
            </table>
        <%--</form>--%>

        <img src='<%=basePath%>/image/pay_button.png' style='width: 153px;height: 50px;border: none;margin-top: 20px;' onclick='save()'/>
    </div>

    <%--<span style="font-size:16px;text-align:center;padding: 10px;">首充奖励：完成首次充值，可联系群内客服额外领取200元钻石，作为建群奖励。</span>--%>

</div>

<input type="hidden" id="goods" value="${sessionScope.server_goods_items}">
</body>

<script>
    var goodsMsgs;
    var token="";
    var agencyId="";
    var payNotSupportTip="3000元及以上金额，请直接联系客服购买";
    
    function queryAgency() {
        var tempAgencyId=$("#agencyId").val();
        if (tempAgencyId==null||isNaN(tempAgencyId)||tempAgencyId.length!=6){
            alert("请输入代充的邀请码");
            return;
        }

        token="";
        agencyId="";
        $("#div_buycards").css("display","none");

        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/agency/info",
            data: {agencyId:tempAgencyId,token:1,gameId:localStorage.getItem('gameId')},
            dataType: "json",
            success: function (result) {
                if (result.code==1000){
                    var dataMsg=result.info;
                    token=result.token;
                    var myName="[未填写]";
                    if(dataMsg.hasOwnProperty("userName")&&$.trim(dataMsg.userName)!=""){
                        myName=dataMsg.userName;
                    }

                    $('#td_agencyName').html("姓名:"+myName);

                    $('#td_agencyPhone').html("手机:"+dataMsg.agencyPhone);
                    agencyId=dataMsg.agencyId;

                    $("#div_buycards").css("display","");
                }else{
                    alert(result.message);
                }
            },
            error: function (req, status, err) {
                console.info(status + "," + err)
                var auth_url = req.getResponseHeader("REQUIRES_AUTH_URL");
                if (auth_url) {
                    window.location.href = auth_url;
                } else {
//                    alert("请稍后再试");
                }
            }
        });
    }

    function initData() {
        $("#mytable").html('<tr align="center" style="height:35px;"><td align="center">钻石</td><td align="center">价格</td><td align="center">比例</td></tr>');

        var goods=$("#goods").val();
        goodsMsgs=goods.split(";");
        var col=1;
        var currentContent;
        for(var i=0;i<goodsMsgs.length;i++){
            var temps=goodsMsgs[i].split(",");
            var val=temps[0];

            var temp;
            if (i==1){
                temp="<tr align=\"center\" id='tr"+val+"' onclick='checkItem("+val+")' style='height: 45px;;background-color: rgb(122,196,240);'><td align=\"center\">";
                currentContent=val;
                temp+="<input style='display: none;' type=\"radio\" value=\""+val+"\" name=\"total_fee\" checked>"+temps[1]+"钻";
                temp+="</td>";
            }else{
                temp="<tr align=\"center\" id='tr"+val+"' onclick='checkItem("+val+")' style='height: 45px;;padding-bottom:15px;background-color: transparent'><td align=\"center\">";
                temp+="<input style='display: none;' type=\"radio\" value=\""+val+"\" name=\"total_fee\">"+temps[1]+"钻";
                temp+="</td>";
            }
            temp+="<td>"+(parseInt(temps[0])/100)+"元</td>";

            col=3;

            temp+="<td align=\"center\">"+temps[2]+"</td>";
            temp+="</tr>";

            $("#mytable").append(temp);
        }

//        $("#mytable").append("<tr><td align=\"center\" colspan='"+col+"'></td></tr>");
        $("#mytable").append("<tr style='height: 45px;color: #0a11ff;font-size: 16px;'><td align=\"center\" colspan='"+col+"' id='currentChecked'></td></tr>");
        <%--$("#mytable").append("<tr id='pay_tr'><td align=\"center\" colspan='"+col+"'><img src='<%=basePath%>/image/pay_button.png' style='width: 153px;height: 50px;border: none;' onclick='save()'/></td></tr>");--%>

        checkItem(currentContent);
    }

    $(document).ready(function () {
        initData();
    });

    function checkItem(val) {
        var tip=false;
        $("input:radio[name='total_fee']").each(function () {
            if (this.value==val){
                $("#tr"+this.value).css("background-color","rgb(122,196,240)");
                this.checked="checked";
                var currentContent="当前选择：";

                for(var i=0;i<goodsMsgs.length;i++){
                    var temps=goodsMsgs[i].split(",");
                    if(parseInt(val)==parseInt(temps[0])){
                        currentContent+=temps[1]+"钻";

                        if (parseInt(temps[3])>0){
                            tip=true;
                        }
                        break;
                    }
                }
                currentContent+="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

                currentContent+=((parseInt(val)/100));

                currentContent+="元";
                $("#currentChecked").html(currentContent);
            }else{
                $("#tr"+this.value).css("background-color","transparent");
            }
        });

        if (tip){
            alert(payNotSupportTip);
        }
    }

    function save() {

        if (token==""){
            alert("参数错误");
            return;
        }

        var item= $("input:radio[name='total_fee']:checked").val();
        console.log(item);
        var isOk=false;
        for(var i=0;i<goodsMsgs.length;i++){
            var temps=goodsMsgs[i].split(",");
            if(parseInt(item)==parseInt(temps[0])&&parseInt(temps[3])==0){
                isOk=true;
                break;
            }
        }
        if (isOk){
            $.ajax({
                timeout: 60000,
                async: true,
                type: "POST",
                url: "<%=path%>/pay/create",
                data: {t:new Date().getTime(),total_fee:item,token:token,gameId:localStorage.getItem('gameId')},
                dataType: "json",
                success: function (result) {
                    if (result.code==1000){
                        console.log(result);
                        var tempUrl="<%=path%>/page/h5pay?t="+new Date().getTime();

                        for (var sProp in result) {
                            tempUrl=tempUrl+"&"+sProp+"="+encodeURIComponent(encodeURIComponent(result[sProp]));
                        }

                        window.location=tempUrl;
                    }else{
                        alert(result.message);
                    }
                },
                error: function (req, status, err) {
                    console.info(status + "," + err)
                    var auth_url = req.getResponseHeader("REQUIRES_AUTH_URL");
                    if (auth_url) {
                        window.location.href = auth_url;
                    } else {
//                    alert("请稍后再试");
                    }
                }
            });
        }else{
            alert(payNotSupportTip);
        }
    }
</script>
</html>