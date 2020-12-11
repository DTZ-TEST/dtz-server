<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String path = request.getContextPath()+"";
    String basePath = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() == 80 ? "" : (":" + request.getServerPort())) + request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1.0"/>
    <title id="title">战绩分享</title>

    <style>

    </style>
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.1">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
</head>

<body style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<%-- <input type="hidden" id="uri" value="${requestScope.uri}"/> --%>
                <img src="<%=basePath%>/image/notGroup.png" style="width: 100%;height: 100%;"/>
</body>

<script>

  <%--   $(document).ready(function () {
        var tempUri=$("#uri").val();
        if ($.trim(tempUri)!=""){
            var callbackUrl="<%=basePath%>/"+tempUri;
//            var agencyId=tempUri.replace("/","_");
            console.log(callbackUrl);
            console.log(tempUri);
            <%=basePath%>/v1/callback/code
            var authUrl="https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx67debb4ff5787149&redirect_uri="+encodeURIComponent(callbackUrl)+"&response_type=code&scope=snsapi_userinfo&state="+tempUri+"#wechat_redirect";
              window.location=authUrl;
//            console.log(authUrl);
//            window.location="https://open.weixin.qq.com/connect/qrconnect?appid=wx518349f40dc68bcf&redirect_uri=http://localhost:8888/gm/v1/callback/code?action=viewtest&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
        }else{
            alert("请重新打开链接");
        }
    }); --%>

</script>
</html>