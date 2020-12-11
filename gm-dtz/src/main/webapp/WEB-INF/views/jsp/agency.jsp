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
    <table id="table_data" style="width: 100%;text-align: center;font-size: 16px;" cellspacing="0" cellpadding="10">
        <tr style="color: #ee6a2a;font-size: 18px;">
            <td>邀请码</td>
            <td>姓名</td>
            <td>手机号</td>
            <td>创建时间</td>
        </tr>
        <%--<tr>--%>
        <%--<td>1</td>--%>
        <%--&lt;%&ndash;<td>迅游棋牌</td>&ndash;%&gt;--%>
        <%--<td>小幺鸡</td>--%>
        <%--<td>2017-04-17 12:00:30</td>--%>
        <%--</tr>--%>
        <%--<tr>--%>
        <%--<td>2</td>--%>
        <%--&lt;%&ndash;<td>迅游棋牌</td>&ndash;%&gt;--%>
        <%--<td>小幺鸡</td>--%>
        <%--<td>2017-04-17 12:00:30</td>--%>
        <%--</tr>--%>
    </table>
</div>
<div id="pp" style="margin-top: 2px;text-align: center">
     <span id="page"></span>
     <span id="curpage">当前第1页</span>
     <button  name="uppage"   value="上一页" onclick="uppage()">上一页 </button>
     <button  name="downpage"    value="下一页" onclick="downpage()"> 下一页</button>
</div>

</body>

<script>
    $(document).ready(function () {
        myQuery();
    });
    
     var totalPage;
    var page = 1;
    $(document).ready(function () {
        if ("1"!=getQueryString("today")){
            $("#img").attr("src","../../image/my_player.png");
            $("#img").css("width","94px");
        }
        myQuery();
    });

    function uppage()
    {
      page = page - 1;
      if(page <= 0){
         page = 1;
      }
      $("#page").html(page+"/"+totalPage);
      myQuery();
    }
    
    function downpage()
    {
      page = page + 1;
      if(page > totalPage){
         page = totalPage;
      }
       $("#page").html(page+"/"+totalPage);
      myQuery();
    }
    
    
    function myQuery() {
        $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/agencies/detail",
            data: {t:new Date().getTime(),pageNo:page,gameId:localStorage.getItem('gameId')},
            dataType: "json",
            success: function(result){
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if (result.code==1000){
                    $("#table_data").html('<tr style="color: #ee6a2a;font-size: 18px;"><td>邀请码</td><td>姓名</td><td>手机号</td><td>创建时间</td></tr>');
                    totalPage = result.page;
                    if(totalPage <= 1){
                      $("#pp").hide();
                    }
                     $("#page").html(page+"/"+totalPage);
                    $.each(result.datas,function (index,tempData){
                        var tempTime;
                        if (!isNaN(tempData.createTime)){
                            var tmpDate=new Date();
                            tmpDate.setTime(tempData.createTime);
                            tempTime=tmpDate.format("yyyy-MM-dd");
                        }else{
                            tempTime=tempData.createTime;
                        }

                        var userName;
                        if (tempData.userName){
                            userName=tempData.userName;
                        }else{
                            userName="--"
                        }

                        var newRow="<tr><td>"+tempData.agencyId+"</td><td>"+userName+"</td><td>"+tempData.agencyPhone+"</td><td>"+tempTime+"</td></tr>";
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