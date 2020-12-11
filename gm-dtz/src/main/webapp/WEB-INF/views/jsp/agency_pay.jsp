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
        <td><img src="<%=basePath%>/image/pay.png" class="header_img2"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<div class="table1">
    <button onclick="history.back();">返回</button>
    <table id="table_list" cellspacing="0" cellpadding="5" style="text-align:center;width: 100%;margin-top: 15px;">
        <thead>
            <td>代理ID</td>
            <td>代理昵称</td>
            <td>金额</td>
        </thead>
    </table>
</div>

<input type="hidden" id="agencyId" value="${sessionScope.roomCard.agencyId}"/>
</body>

<script>
    $(document).ready(function () {
        var paramStr = "";
        var idx=window.location.href.indexOf("?");
        if (idx>-1){
            paramStr=window.location.href.substring(idx+1);
        }
        var paramKVs=paramStr.split("&");
        for (var i=0;i<paramKVs.length;i++){
            var kv=paramKVs[i].split("=");
            if (kv[0]=="type"){
                paramStr=kv.length==2?kv[1]:"today";
            }
        }

        if ("today"==paramStr){
            $("#title").html("今日代理充值")
        }else if ("week"==paramStr){
            $("#title").html("本周代理充值")
        }else if ("month"==paramStr){
            $("#title").html("本月代理充值")
        }

        $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/agencies/detail",
            data: {agencyId:$("#agencyId").val(), type:paramStr,gameId:localStorage.getItem('gameId')},
            dataType: "json",
            success: function(result){
                if (result.code==1000){
                    var datas=result.datas;
                    if (datas.length>0){
                        $("#table_list").html("<thead><td>代理ID</td><td>代理昵称</td><td>金额</td></thead>");
                        $.each(datas,function (index,tempData){
                            var newRow="<tr><td>"+tempData.agencyId+"</td><td>"+tempData.userName+"</td><td>"+(tempData.userAmount/10)+"</td></tr>";
                            $('#table_list').append(newRow);
                        });
                    }
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
    });

</script>
</html>