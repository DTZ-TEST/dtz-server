<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
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
        <td><img src="<%=basePath%>/image/select.png" class="header_img2" style="width: 142px;"/></td>
        <td class="fix_td" onclick="window.location='<%=path%>/page/home?gameId='+localStorage.getItem('gameId')"><img class="header_img3" src="<%=basePath%>/image/home.png"/></td>
    </tr>
</table>
<table id="con_table" cellpadding="5" style="text-align: center;margin-top: 10px;border-bottom: 1px solid #b5b5b5;width: 100%;padding-bottom: 10px;">
   <tr id="agencyId3"><td colspan="1" style="text-align: left;font-size: 20px;"><select id="num" class="text-input" style="width: 95%;height: 30px;"><option value="1000000" selected="selected">余钻100万以上</option><option value="500000" >余钻50万以上</option><option value="300000" >余钻30万以上</option><option value="100000" >余钻10万以上</option></select></td>
   <td><input onclick="myQuery('0')" type="button" value="查询"/></td>
   </tr>
  <tr>
        <td colspan="2" style="text-align: left;font-size: 16px;">
            <span id="span_total">共0名,0钻</span>
        </td>
    </tr>  
</table>
<div class="table2" style="margin-top: 2px;">
    <table id="table_data" style="width: 100%;text-align: center;font-size: 18px;" cellspacing="0" cellpadding="10">
        <tr style="color: #ee6a2a;font-size: 20px;">
            <td>邀请码</td>
            <td>用户名</td>
            <td>数量</td>
        </tr>
    </table>
</div>
<div id="pp" style="margin-top: 2px;text-align: center" >
    <button  name="uppage"   value="上一页" onclick="uppage()">上一页 </button>
    <span id="page"></span>
    <button  name="downpage"    value="下一页" onclick="downpage()"> 下一页</button>
   <input id="type" value="${requestScope.type}" type="hidden" />
   <input type="hidden" id="agencyId" value="${sessionScope.roomCard.agencyId}"/>
    <input id="start" value="${requestScope.start}" type="hidden" />
   <input id="end" value="${requestScope.end}" type="hidden" />
</div>
</body>
<script>
    var currentNo=0;
    var isEnd=false;
    var loading=false;
    var totalPage;
    var page = 1;
    var type = 0;
    $(document).ready(function () {
        myQuery("1");
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
    function selectSub(num) {
        var temp=$("#agency_"+(0)).val();
        if(temp !=""){
           loadAgencies(1,temp.split("_")[1]);
           $("#agencyId2").css("display","");
        }else{
           $("#agencyId2").css("display","none");
        }
        
    }

    function loadAgencies(trid,userId) {
        //
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/agencies/detail",
            data: {
                userId:userId,gameId:localStorage.getItem('gameId')
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if(result.code==1000){
                        if(trid == 1){
				           $("#agency_1").find("option").remove();
				           $("#agency_"+trid).html('<option value="">全部</option>');
				        }
                       // $("#agency_"+trid).html('<option value="">全部</option>');
                        $.each(result.datas,function (index,tempData){
                            var temp;
                            if (tempData.userName){
                                temp=tempData.userName;
                            }else{
                                temp=tempData.agencyPhone;
                            }
                            var newRow='<option value="'+tempData.agencyId+'_'+tempData.userId+'">'+tempData.agencyId+'('+temp+')</option>';
                            $('#agency_'+trid).append(newRow);
                        });

                }else
                    alert(result.message);
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
    var agencyId1;
    var agencyId2;
    var payType;
    function myQuery(append) {
        $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/queryAgencyCard",
            data: {num:$("#num").val(),gameId:localStorage.getItem('gameId')},
            dataType: "json",
            success: function(result){
                if (result.code==1000){
                    totalPage = result.page;
                    if(totalPage <= 1){
                      $("#pp").hide();
                      page=1; 
                    }else{
                      $("#pp").show();
                    }
                     $("#page").html(page+"/"+totalPage);
                     $("#span_total").html('共'+result.count+'名,'+(result.total)+'钻');
                    $("#table_data").html('<tr style="color: #ee6a2a;font-size: 20px;"><td>邀请码</td><td>用户名</td><td>数量</td></tr>');
                     $.each(result.datas,function (index,tempData){
                    	 var name = "";
                         if(tempData.userName != null){
                            name = tempData.userName;
                         }
                        var newRow="<tr><td>"+tempData.agencyId+"</td><td>"+name+"</td><td>"+tempData.commonCard+"</td></tr>";
                        $('#table_data').append(newRow);
                    });

                    loading=false;
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