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
    <script type="text/javascript" src="<%=basePath%>/js/md5.js"></script>
    <script src="<%=basePath%>/js/area.js?v=1.0.2" type="text/javascript"></script>
</head>

<body onresize="resize()" style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<table class="header_table" cellpadding="0" cellspacing="0">
    <tr>
        <td class="fix_td" onclick="history.back()"><img class="header_img1" src="<%=basePath%>/image/back.png"/></td>
        <td><img src="<%=basePath%>/image/msg_detail.png" class="header_img2" style="width: 94px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div style="text-align: center;width: 100%;">
    <table cellpadding="0" cellspacing="15" style="width: 100%;">
        <tr>
            <td>
                <input onblur="checkName()" id="nickname" value="${sessionScope.roomCard.userName}" class="text-input" placeholder="真实姓名">
            </td>
        </tr>
         <tr>
            <td>
                <input id="wx" value="${sessionScope.roomCard.agencyWechat}" class="text-input" placeholder="微信号" type="hidden">
            </td>
        </tr> 
        <tr>
            <td>
                <input id="tel" value="${sessionScope.user.userTel}" class="text-input" type="text" pattern="[0-9]*" placeholder="手机号">
            </td>
        </tr>
        <tr>
            <td align="center">
                <div  id="get_code" style="height: 32px;width: 65%;background-color: #36a756;border:0px red outset;border-radius: 5px;color: white;font-size: 16px;text-align: center;padding-top: 12px;" onclick="loadTelCode()">获取手机验证码</div>
            </td>
        </tr>
         <tr>
            <td>
                <i class="icon-input" style="background-image:url('<%=basePath%>/image/msg.png');"></i>
                <input class="text-input" pattern="[0-9]*" id="tel_code" placeholder="手机验证码">
            </td>
        </tr>
        <tr>
            <td>
                 <img id="tr" src="<%=basePath%>/image/submit.png" style="width: 153px;height: 50px;border: none;margin-top: 25px;"  onclick="save()"/>
                 <img id="cz" src="<%=basePath%>/image/rest.png" style="width: 153px;height: 50px;border: none;margin-top: 25px;"  onclick="czxx()"/>
            </td>
        </tr>
    </table>
    </div>
    <div id="tt" style="margin-top: 2px;text-align: center" >
        <span id="xs" style="color: red;font-size: 20px;"> 请注意：请务必准确填写您的真实姓名，并与微信实名认证名字一致，否则无法提现！<br/>微信实名认证名字查看方法：点击【我】-【钱包】-右上角点击选择【支付管理】-【实名认证】 </span>
   </div>

<input type="hidden" id="bankName" value="${sessionScope.roomCard.bankName}" class="text-input" placeholder="开户银行（如：工商银行）">
</body>

<script>
    var current=0;
    var max=60;
    var flag = 0;

    var isReturn=false;
    function czxx() {
         $("#get_code").show();
         $("#tel_code").show();
         $("#tel").show();
         if(flag == 0){
           flag++;
           return;
         }
         if ($.trim($("#tel_code").val()) == "") {
            alert("请输入手机验证码");
            return;
        }
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/info/reset",
            data: {
                 telCode:$("#tel_code").val(),
                 tel:$("#tel").val(),gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {
                if (result.code!=1000){
                    alert(result.message);
                }else{
                    flag = 0;
                    alert(result.message);
                    window.location.reload();
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
    function resize() {
        $("#bankNameOption").width($("#tel").width()+2);
        $("#s_province").width($("#tel").width()+2);
        $("#s_city").width($("#tel").width()+2);
    }
    function loadTelCode() {
        if (isReturn){
            return;
        }else{
            isReturn=true;
        }
        var tel = $("#tel").val();

        if ($.trim(tel)==""||!tel.match(/^1[34578]\d{9}$/)){
            alert("请输入正确的手机号码");
            return;
        }

        $("#get_code").html(max+"秒后重发");
        current=max;
        var timeId=setInterval(function(){
            current--;
            $("#get_code").html(current+"秒后重发");

            if (current<=0){
                clearInterval(timeId);
                isReturn=false;
                $("#get_code").html("获取手机验证码");
            }

        },1000);

        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/vercode/telcode",
            data: {
                tel: tel,gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {
                if (result.code!=1000){
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
    function checkName() {
        var nickname=$("#nickname").val().trim();
        if (nickname.length>0){
            if (nickname.length==1||nickname.length>4){
                alert("请填写有效的真实姓名");
                $("#nickname").val("");
                return false;
            }else if (!/^[\u4e00-\u9fa5]+$/.test(nickname)){
                alert("请填写有效的真实姓名");
                $("#nickname").val("");
                return false;
            }
        }
        return true;
    }

    var nickname;
    //    var name;
    var tel;
    //    var qq;
    var wx;
    //    var email;
    var bankName;
    var bankNum;
    //    var comment;
    $(document).ready(function () {
        resize();
        nickname=$("#nickname").val().trim();
        $("#tel").hide();
        tel=$("#tel").val().trim();
        wx=$("#wx").val().trim();
        $("#get_code").hide();
        $("#tel_code").hide();
        if(nickname!=""){
            $("#nickname").attr("disabled","disabled");
            $("#tr").hide();
            $("#cz").show();
        }else{
            $("#tr").show();
            $("#cz").hide();
        }
        $("#tel").attr("disabled","disabled");
        
        $("#wx").attr("disabled","disabled");
        
    });

    function selectOption(id,msg) {
        $("#"+id+" option").each(function(){ //遍历全部option
//            var text = $(this).text(); //获取option的text
            var value = $(this).val(); //获取option的value
            if (msg.indexOf(value)!=-1||value.indexOf(msg)!=-1){
                $("#"+id).val(value);
                $("#"+id).attr("disabled","disabled");
            }
        });
    }

    function save() {

        if (!checkName()){
            return;
        }

        var nickname=$("#nickname").val().trim();
        if (nickname==""){
            alert("请填写有效的真实姓名");
            return;
        }
        var a=confirm("请确保信息真实有效，否则将无法提现成功。确定提交吗?");
        if (a==true)

            $.ajax({
                timeout: 60000,
                async: true,
                type: "POST",
                url: "<%=path%>/user/update",
                data: {
                    nickname:nickname,gameId:localStorage.getItem('gameId')
                },
                dataType: "json",
                success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                    if (result.code == 1000) {
                        alert(result.message);
                        history.back();
                    } else {
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

</script>
</html>