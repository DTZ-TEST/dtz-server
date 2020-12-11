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
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.2">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="<%=basePath%>/js/jquery.qrcode.min.js"></script>
    <script type="text/javascript" src="<%=basePath%>/js/clipboard.min.js"></script>
</head>

<body style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<table id="table_top" class="header_table" cellpadding="0" cellspacing="0">
    <tr>
        <td class="fix_td" onclick="history.back()"><img class="header_img1" src="<%=basePath%>/image/back.png"/></td>
        <td><img src="<%=basePath%>/image/get_agency.png" class="header_img2" style="width: 94px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div id="div_myvercode" class="table2" style="margin-top: 2px;text-align: center">
    <div id="qr_link" style="margin-top: 10px;width: 100%;">
        <table cellpadding="5" cellspacing="0" style="width: 100%;border-width: 0px;">
            <tr>
                <td>您的专属发展玩家链接</td>
            </tr>
            <tr>
                <td><span id="content"><%=basePath%>/d3/${sessionScope.roomCard.agencyId}</span></td>
            </tr>
            <tr>
                <td>
                    <img id="copy" class="copy" src="<%=basePath%>/image/copy.png" style="width: 153px;height: 50px;border: none;"/>
                </td>
            </tr>
            <tr>
                <td>通过该链接注册的玩家，永久和您的邀请码绑定</td>
            </tr>
        </table>
    </div>
    <div id ="div_qrcode"></div>
</div>
</div>

<%--<input type="hidden" id="agencyId0" value="${requestScope.agencyId0}">--%>
<%--<input type="hidden" id="agencyId0ttl" value="${requestScope.agencyId0ttl}">--%>
</body>

<script>
    var link1;
//    var isTemp=false;
    var ttl=0;
    $(document).ready(function () {
        <%--var agencyId0=$("#agencyId0").val();--%>
        <%--if (agencyId0!=null&&agencyId0!=undefined&&agencyId0.trim().length>0){--%>
            <%--link1="<%=basePath%>/${requestScope.agencyId0}";--%>
            <%--isTemp=true;--%>
            <%--ttl=parseInt($("#agencyId0ttl").val());--%>
        <%--}else{--%>
            link1="<%=basePath%>/d3/${sessionScope.roomCard.agencyId}";
//        }
        $("#content").html(link1);

        if ($.trim($("#div_qrcode").html()).length==0){
            var h1=window.document.getElementById("table_top").scrollHeight;
            var h2=document.body.scrollHeight;
            $("#div_myvercode").css("height",(h2-h1-30));
            $("#div_qrcode").css("margin-top",(h2-h1-418)/2);
            $('#div_qrcode').qrcode({width: 168,height: 168,text: link1});

            var image = new Image();
            image.src = document.getElementsByTagName("canvas").item(0).toDataURL("image/png");

            var m=Math.round(ttl/60);

            $("#div_qrcode").html('<image src="'+image.src+'"/><br/><br/><span>分享链接或者二维码发展玩家，永久有效</span>');
        }

        if (window.clipboardData) {    //for ie
            var copyBtn = document.getElementById("copy");
            copyBtn.onclick = function () {
                window.clipboardData.setData('text', link1);
                alert("复制成功");
            }
        } else {
//            var clip=new ZeroClipboard($("#copy"));
//            clip.setText($("#content").html());

            var clipboard=new Clipboard('.copy', {
                text: function(trigger) {
                    return link1;
                }
            });

            clipboard.on('success', function(e) {
//                console.info('Action:', e.action);
//                console.info('Text:', e.text);
//                console.info('Trigger:', e.trigger);

                e.clearSelection();

                alert("复制成功");
            });

            clipboard.on('error', function(e) {
//                console.error('Action:', e.action);
//                console.error('Trigger:', e.trigger);
            });
        }
    });
</script>
</html>