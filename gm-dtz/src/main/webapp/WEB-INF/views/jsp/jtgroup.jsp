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
    <title>快乐玩游戏代理后台_快乐打筒子_快乐跑得快_快乐跑胡子</title>

    <style>
    </style>
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.2">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="<%=basePath%>/js/md5.js"></script>
</head>

<body style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<table class="header_table" cellpadding="0" cellspacing="0">
    <tr>
        <td class="fix_td" onclick="history.back()"><img class="header_img1" src="<%=basePath%>/image/back.png"/></td>
        <td><img src="<%=basePath%>/image/jlb.png" class="header_img2" style="width: 94px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div class="table2">
    <table cellspacing="0" cellpadding="15" style="width: 100%;text-align: left;vertical-align: middle;">
       <tr id="t2" onclick="window.location='<%=path%>/page/blacks/groupmanage?gameId='+localStorage.getItem('gameId')">
            <td style="font-size: 22px;">更换群主</td>
            <td style="text-align: right;">
                <img src="<%=basePath%>/image/jiantou.png" style="width: 16px;height: 29px;"/>
            </td>
        </tr>
    </table>
</div>
</body>
<script>
 // $(document).ready(function () {
       <%-- $("#t4").hide();
       $("#t1").hide();
        $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/query/have/jtgroup",
            dataType: "json",
           success: function(result){
                if (result.code==1000){
                    if(result.result!=null){
                       $("#t1").show();
                       $("#t3").show();
                       $("#t2").hide();
                       if(result.type==1){
	                       $("#t3").hide();
	                       $("#t4").show();
                        }
                    }
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
    });
  function save(){
     var f = confirm("代理业绩累积1000元可创建牌友群，确定要创建吗？");
     if(!f){
       return;
     }
      $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/create/poker/group",
            dataType: "json",
           success: function(result){
                if (result.code==1000){
                  alert(result.message);
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
        }); --%>
  // }
</script>
</html>