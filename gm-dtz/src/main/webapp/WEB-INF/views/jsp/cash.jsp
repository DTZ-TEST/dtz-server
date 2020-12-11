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
        <td><img src="<%=basePath%>/image/my_agency.png" class="header_img2" style="width: 95px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div class="table2" style="margin-top: 2px;">
    <table style="width: 100%;">
        <tr>
            <td>
                <input id="count" class="text-input" type="text" pattern="[0-9]*" placeholder="提现金额">
            </td>
        </tr>
        <tr>
            <td>
                <input type="button" value="确 认" onclick="save()">
            </td>
        </tr>
    </table>
</div>
</div>
<input type="hidden" id="tempid" value="${sessionScope.roomCard.openid}"/>
<input type="hidden" id="cash_code_state" value="${sessionScope.cash_code_state}"/>
</body>

<script>

    $(document).ready(function () {
        var tempid=$("#tempid").val();
        if ($.trim(tempid)==""){
            var authUrl="https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx518349f40dc68bcf&redirect_uri="+encodeURIComponent('http://www.139up.cn/gm.html')+"&response_type=code&scope=snsapi_base&state="+$("#cash_code_state").val()+"#wechat_redirect";
            window.location=authUrl;
        }
    });

    function save() {
        var count = $("#count").val().trim();

        if (count==""||isNaN(count)||count<=0){
            alert("请输入充值金额");
            return;
        }

        var a=confirm("您确认要提现"+count+"元吗？");
        if (a==true)
            $.ajax({
                timeout: 60000,
                async: true,
                type: "POST",
                url: "<%=path%>/pay/cash",
                data: {
                    count:count,gameId:localStorage.getItem('gameId')
                },
                dataType: "json",
                success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json

                    if (result.code==1000){
                        alert("提现成功")
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

</script>
</html>