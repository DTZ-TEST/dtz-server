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
    <title>快乐打筒子</title>

    <style>
        .icon1{
            width: 44px;height: 36px;margin-top: 8px;
        }
        .icon2{
            width: 18px;height: 32px;margin-top: 8px;
        }
        .bottom-float-left{display: -webkit-box;display: -moz-box;
				display: -ms-flexbox;
				display: -webkit-flex;
				display: -o-flex;
			  	display: flex;-webkit-align-items: flex-end;
				-moz-align-items: flex-end;
				-ms-align-items: flex-end;
				-o-align-items: flex-end;align-items: flex-end;
			}
    </style>
    <link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.1">
    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
</head>

<body style="width: 100%;height: 100%;font-family: 黑体;margin: 0 0 0 0;padding: 0 0 0 0;">
<table id="table_top" cellspacing="0" cellpadding="0" style="text-align:left;background-image: url('<%=basePath%>/image/top_background.png');background-repeat: round;background-size:100%;width: 100%;">
    <tr>
        <td rowspan="2"  onclick="loadMyQrcode()"style="width: 80px;vertical-align: middle;padding: 9px;"><img
                id="mypic"
                style="width: 90px;height: 90px;text-align: center;margin-top: 4px;"
                src="<%=basePath%>/image/game.png"/>
        </td>
        <td style="text-align: left;vertical-align: middle;color: #007cc3;font-size: 16px;padding-top: 15px;">
            &nbsp;姓名：<span id="myname" style="color: red">未填写</span><span id="mycode">--</span><br/>
            &nbsp;邀请码：${sessionScope.roomCard.agencyId}<br/>
            &nbsp;钻石数：<span id="card">--</span>
        </td>
        <td rowspan="2" style="text-align: center;vertical-align: middle;color: #007cc3;font-size: 18px;padding-top: 5px;">
            <div>
                <img id="update_msg" onclick="window.location='<%=path%>/page/user/detail'" style="width: 80px;height: 32px;" src="<%=basePath%>/image/update_msg.png" />
            </div>
            <div>
                <img onclick="window.location='<%=path%>/user/logout'" style="width: 80px;height: 32px;" src="<%=basePath%>/image/quit.png" />
            </div>
        </td>
    </tr>
    <tr>
    <td class="bottom-float-left" width="100%">
    <input type="button"  onclick="clickItems(14)" id="buy_cards2" value="售钻给玩家" />
    <input type="button"  onclick="clickItems(15)" id="buy_cards3" value="售钻给代理"/>
    </td>
    </tr>
</table>
<%-- 	<div class="player" style="text-align:left;background-image: url('<%=basePath%>/image/top_background.png');background-repeat: round;background-size:100%;width: 100%;">
		<div style="padding: 0px;" class="bottom-float-left">
			<div style="width:50%;">
				
				
			</div>
			<div style="width:50%;">
				<p onclick="clickItems(15)" id="buy_cards3">售钻给代理</p>
			</div>
		</div>
	</div> --%>
<%@include file="./switchGame.html"%>
<marquee id="marqueeText" behavior="scroll" direction="left" style="color: red;margin-top: 5px;display: none;">${sessionScope.marqueeText}</marquee>
<div id="div_content" style="display: block;">
    <div class="table1" id="yjDiv" style="display: none;">
        <table cellspacing="0" cellpadding="9" style="text-align:center;width: 100%;margin-top: 5px;color: #393a3e;font-size: 16px;">
            <!--<tr>-->
            <!--<td colspan="6" style="text-align: center;">代理分成比例</td>-->
            <!--</tr>-->
            <!--style="background: url('<%=basePath%>/image/background2.png') no-repeat;background-size:99%;"-->
            <tr>
                <td>月业绩</td>
                <td id="zhibiao_1">0</td>
                <td id="zhibiao_2">501</td>
                <td id="zhibiao_3">1001</td>
                <td id="zhibiao_4">2001</td>
                <td id="zhibiao_5">3001</td>
            </tr>
            <tr>
                <td>返佣比例</td>
                <td id="zhibiao1">40%</td>
                <td id="zhibiao2">45%</td>
                <td id="zhibiao3">50%</td>
                <td id="zhibiao4">55%</td>
                <td id="zhibiao5">60%</td>
            </tr>
            <tr>
                <td colspan="6" style="text-align: center;color: #36a756;font-size: 16px;" id="current_level">当前比例：--</td>
            </tr>
        </table>

        <table cellspacing="0" cellpadding="9" style="text-align:center;width: 100%;margin-top: 10px;font-size: 16px;color: #5b5b5b;">
            <tr>
                <td style="width: 40px;"></td>
                <td>今日</td>
                <td>本周</td>
                <td>本月</td>
            </tr>
            <tr>
                <td>总<br/>佣金</td>
                <td><span id="pay_today3" style="color: #ee6a2a;font-size: 18px;">0</span></td>
                <td><span id="pay_week3" style="color: #ee6a2a;font-size: 18px;">0</span></td>
                <td>
                    <span id="pay_month3" style="color: #ee6a2a;font-size: 18px;">0</span>
                    <span id="span_pay_month3" style="font-size: 16px;display: block">未结:0</span>
                </td>
            </tr>
            <tr>
                <td>我的充值</td>
                <td id="td_today1" onclick="myClick(this);">
                    <span id="span_pay_today1"  style="color: #ee6a2a;font-size: 18px;display: block">0</span>
                    <span id="pay_today1" style="font-size: 16px;">0</span>
                </td>
                <td id="td_week1" onclick="myClick(this);">
                    <span id="span_pay_week1"  style="color: #ee6a2a;font-size: 18px;display: block">0</span>
                    <span id="pay_week1" style="font-size: 16px;">0</span>
                </td>
                <td id="td_month1" onclick="myClick(this);">
                    <span id="span_pay_month1"  style="color: #ee6a2a;font-size: 18px;display: block">0</span>
                    <span id="pay_month1" style="font-size: 16px;">0</span>
                </td>
            </tr>
            <tr>
                <td>代理收入</td>
                <td id="td_today3" onclick="myClick(this);">
                    <span id="span_pay_today4" style="color: #ee6a2a;font-size: 18px;display: block">0</span>
                    <span id="pay_today4" style="font-size: 16px;">0</span>
                </td>
                <td id="td_week3" onclick="myClick(this);">
                    <span id="span_pay_week4" style="color: #ee6a2a;font-size: 18px;display: block">0</span>
                    <span id="pay_week4" style="font-size: 16px;">0</span>
                </td>
                <td id="td_month3" onclick="myClick(this);">
                    <span id="span_pay_month4" style="color: #ee6a2a;font-size: 18px;display: block">0</span>
                    <span id="pay_month4" style="font-size: 16px;">0</span>
                </td>
            </tr>
            <tr>
                <td>代理直充</td>
                <td id="td_today2" onclick="myClick(this);">
                    <span id="span_pay_today2" style="color: #ee6a2a;font-size: 18px;display: block">0</span>
                    <span id="pay_today2" style="font-size: 16px;">0</span>
                </td>
                <td id="td_week2" onclick="myClick(this);">
                    <span id="span_pay_week2" style="color: #ee6a2a;font-size: 18px;display: block">0</span>
                    <span id="pay_week2" style="font-size: 16px;">0</span>
                </td>
                <td id="td_month2" onclick="myClick(this);">
                    <span id="span_pay_month2" style="color: #ee6a2a;font-size: 18px;display: block">0</span>
                    <span id="pay_month2" style="font-size: 16px;">0</span>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <span style="font-size: 14px;">您可获得下级和下下级代理官方直充金额10%返利<br/>您可获得下级和下下级代理绑码充值金额10%返利</span>
                </td>
            </tr>
        </table>
    </div>

    <div class="table2" style="margin-bottom: 10px;font-family: 黑体;color: #393a3e;font-size: 22px; ">
        <table id="itemTable" cellspacing="0" cellpadding="15" style="width: 100%;margin-top: 10px;text-align: left;vertical-align: middle;">
            <%--<tr id="get_my_pay" onclick="clickItems(8)">--%>
                <%--<td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/qrcode1.png" class="icon1"/></td>--%>
                <%--<td>提现</td>--%>
                <%--<td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png"  class="icon2"/></td>--%>
            <%--</tr>--%>
            <tr id="pay_delegate" onclick="clickItems(19)">
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/pay_icon2.png" class="icon1"/></td>
                <td>代充</td>
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png" class="icon2"/>
                </td>
            </tr>
            <%-- <tr onclick="clickItems(13)" id="buy_cards1">
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/bi1.png" class="icon1"/></td>
                <td>代理购钻</td>
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png"  class="icon2"/></td>
            </tr> --%>
                <tr onclick="clickItems(14)" id="buy_cards2">
                    <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/pay_icon2.png"
                                                                      class="icon1"/></td>
                    <td>售钻给玩家</td>
                    <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png" class="icon2"/>
                    </td>
                </tr>
                <tr onclick="clickItems(15)" id="buy_cards3">
                    <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/pay_icon2.png"
                                                                      class="icon1"/></td>
                    <td>售钻给代理</td>
                    <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png" class="icon2"/>
                    </td>
                </tr>
            <tr id="my_qrcode_item" onclick="clickItems(7)" style="display: none;">
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/qrcode1.png" class="icon1"/></td>
                <td>发展代理</td>
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png"  class="icon2"/></td>
            </tr>
            <tr onclick="clickItems(0)">
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/money1.png" class="icon1"/></td>
                <td>查询</td>
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png"  class="icon2"/></td>
            </tr>
             <tr onclick="clickItems(16)" id="myItem16" style="display: none">
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/bi1.png" class="icon1"/></td>
                <td>每日返佣提现</td>
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png" class="icon2"/>
                </td>
            </tr>
            <tr onclick="clickItems(2)" id="myItem2" style="display: none;">
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/person4.png" class="icon1"/></td>
                <td>我的玩家(<span id="span_my_players">0</span>人)</td>
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png" class="icon2"/></td>
            </tr>
            <tr onclick="clickItems(3)" style="display: none;">
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/person3.png" class="icon1"/></td>
                <td>我的代理(<span id="span_my_agencies">0</span>人)</td>
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png" class="icon2"/></td>
            </tr>
            <tr onclick="clickItems(4)">
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/person1.png" class="icon1"/></td>
                <td>我的信息</td>
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png"  class="icon2"/></td>
            </tr>
             <%-- <tr onclick="clickItems(12)" id="jt">
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/person3.png" class="icon1"/></td>
                <td>俱乐部</td>
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png"  class="icon2"/></td>
            </tr> --%>
        </table>
    </div>
</div>
<input type="hidden" id="agencyId" value="${sessionScope.roomCard.agencyId}"/>
<input type="hidden" id="agencyLevel" value="${sessionScope.roomCard.agencyLevel}"/>
<input type="hidden" id="roleId" value="${sessionScope.user.roleId}">
<input type="hidden" id="purchase" value="${sessionScope.user.isHavePurchase}">
</body>

<script>
 /*  $(function(){
         if($("#purchase").val()!=1){
          $("buy_cards1").remove();
             $("buy_cards2").remove();
             $("buy_cards3").remove();
         }
         
     });  */
    function copyData()
    {
        <%--window.clipboardData.setData("Text","<%=basePath%>/${sessionScope.roomCard.agencyId}");--%>
        <%--alert("复制成功!");--%>
    }

    var roleId=0;
    var agencyLevel=0;
    var isCompleteOfPlayer=false;
    var isCompleteOfAgency=false;
    var player_today_pay=0;
    var player_week_pay=0;
    var player_month_pay=0;
    var agency_today_pay=0;
    var agency_week_pay=0;
    var agency_month_pay=0;
    var player_bl_pay=0;
    var agency_bl_pay=0;
    var now = new Date(); //当前日期 
	var nowDayOfWeek = now.getDay(); //今天本周的第几天 
	var nowDay = now.getDate(); //当前日 
	var nowMonth = now.getMonth(); //当前月 
	var nowYear = now.getYear(); //当前年 
    nowYear += (nowYear < 2000) ? 1900 : 0; //
    var my_players_count=0;
    var popMsg = '';

    $(document).ready(function () {

        var gameId = localStorage.getItem("gameId");
        if (gameId!=null&&gameId!=""){
            checkTab("tabGame_"+gameId);
        }
        if ("3"==gameId){
            $("#yjDiv").css("display","none");
            $("#myItem16").remove();
            $("#myItem2").remove();
        }

        roleId=$("#roleId").val();
        agencyLevel = $("#agencyLevel").val();
        if (!(agencyLevel==99 || roleId==1)){
                $("#pay_delegate").remove();
            }
        
        if(agencyLevel!=99){
        	$("#my_qrcode_item").hide();
        	$("#my_trans").hide();
        }
        
        if (roleId!=null&&roleId!=undefined){
            if (parseInt(roleId+"")>=1){
                $("#itemTable").append('<tr onclick="clickItems(6)"><td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/cards1.png" class="icon1"/></td> <td>综合数据</td> <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png"  class="icon2"/></td> </tr>');
                $("#itemTable").append('<tr onclick="clickItems(10)"><td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/cards1.png" class="icon1"/></td> <td>管理</td> <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png"  class="icon2"/></td> </tr>');
            }else {
                $("#get_my_pay").remove();
            }
        }else{
            roleId=0;
            $("#get_my_pay").remove();
        }

        var marqueeText=$("#marqueeText").html().trim();
        if (marqueeText!=""){
            $("#marqueeText").css("display","");
        }else{
            $("#marqueeText").css("display","none");
        }

//        var with1=window.document.getElementById("td_today1").scrollWidth;
        loadMyDatas("base");
        loadMyDatas("pay1");
        loadMyDatas("pay2");

        loadMyDatas("1");
        loadMyDatas("2");

        agencyLevel=parseInt($("#agencyLevel").val(),10);
        $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: "<%=path%>/user/get/info/agency",
            data: {agencyId:$("#agencyId").val(),gameId:loadCheckedGameId()},
            dataType: "json",
            success: function(result){
                $("#card").html(result.data.commonCard);
            }
        });
    });


    function switchGame() {
        var gameId = localStorage.getItem("gameId");
        var gameId1 = loadCheckedGameId();
        if (gameId!=null&&gameId!=""&&gameId!=gameId1) {
            localStorage.setItem("gameId",gameId1);
            window.location="<%=path%>/page/home?gameId="+gameId1+"&t="+new Date().getTime();
        }
    }


    function clickItems(a) {
        switch (a){
            case 8:
                window.location="<%=path%>/page/cash?gameId="+loadCheckedGameId();
                break;
            case 7:
                window.location="<%=path%>/page/qrcode?gameId="+loadCheckedGameId();
                break;
            case 6:
                window.location="<%=path%>/page/statistics/all?gameId="+loadCheckedGameId();
                break;
            case 0:
                window.location="<%=path%>/page/pay_history?gameId="+loadCheckedGameId();
                break;
            case 1:
                window.location="<%=path%>/page/income?gameId="+loadCheckedGameId();
                break;
            case 2:
                window.location="<%=path%>/page/player?total="+my_players_count+"&gameId="+loadCheckedGameId();;
                break;
            case 3:
                window.location="<%=path%>/page/agency?gameId="+loadCheckedGameId();
                break;
            case 4:
                window.location="<%=path%>/page/msg/detail?gameId="+loadCheckedGameId();
                break;
            case 5:
                window.location="<%=path%>/page/html/policy.html?gameId="+loadCheckedGameId();
                break;
            case 12:
                window.location="<%=path%>/page/jtgroup?gameId="+loadCheckedGameId();
                break;
            case 13:
                window.location="<%=path%>/page/buy/cards?gameId="+loadCheckedGameId();//代理购卡
                break;
            case 14:
                window.location = "<%=path%>/page/pay/for/player?gameId="+loadCheckedGameId();
                break;
            case 15:
                window.location = "<%=path%>/page/pay/for/agency?gameId="+loadCheckedGameId();
                break;
            case 16:
                 window.location = "<%=path%>/page/income?gameId="+loadCheckedGameId();//提现
                break;
            case 10:
                window.location="<%=path%>/page/manage?gameId="+loadCheckedGameId();
                break;
            case 19:
                window.location = "<%=path%>/page/buy/cards?delegate=1&gameId="+loadCheckedGameId();
                break;
        }
    }

    function loadMyQrcode(){

    }
    function checkPopMsg(){
        if(popMsg != null && popMsg.length >0){
            alert(popMsg);
        }
    }

    function loadMyDatas(str) {
        var requestUrl="<%=path%>/test/welcome";
        if ("1"==str){
            requestUrl="<%=path%>/user/players";
        }else if ("2"==str){
            requestUrl="<%=path%>/user/agencies";
        }else if (str=="pay1"){
            requestUrl="<%=path%>/user/players/pay";
        }else if (str=="pay2"){
            requestUrl="<%=path%>/user/agencies/pay";
        }else if (str=="base"){
            requestUrl="<%=path%>/user/msg";
        }else{
            return;
        }

        $.ajax({
            timeout:60000,
            async:true,
            type: "POST",
            url: requestUrl,
            data: {agencyId:$("#agencyId").val(),gameId:loadCheckedGameId()},
            dataType: "json",
            success: function(result){
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if (result.code==1000){
                    if ("1"==str){
                        $("#span_my_players").html(result.count);
                        my_players_count=result.count;
                    }else if ("2"==str){
                        $("#span_my_agencies").html(result.count);
                    }else if (str=="pay1"){
                        player_today_pay=result.today/10;
                        player_week_pay=result.week/10;
                        player_month_pay=result.month/10;

                        if (player_month_pay>3000){
                            $("#current_level").html("恭喜！你已达到最高返佣比例！");
                            player_bl_pay=0.6;
                            $("#zhibiao5").css("background-color", "#00CCFF");
                            $("#zhibiao_5").css("background-color", "#00CCFF");
                        }else if (player_month_pay>2000){
                            var temp=3001-player_month_pay;
                            $("#current_level").html("本月业绩再增加"+temp+"元可提升至60%");
                            player_bl_pay=0.55;
                            $("#zhibiao4").css("background-color", "#00CCFF");
                            $("#zhibiao_4").css("background-color", "#00CCFF");
                        }else if (player_month_pay>1000){
                            var temp=2001-player_month_pay;
                            $("#current_level").html("本月业绩再增加"+temp+"元可提升至55%");
                            player_bl_pay=0.5;
                            $("#zhibiao3").css("background-color", "#00CCFF");
                            $("#zhibiao_3").css("background-color", "#00CCFF");
                        }else if (player_month_pay>500){
                            var temp=1001-player_month_pay;
                            $("#current_level").html("本月业绩再增加"+temp+"元可提升至50%");
                            player_bl_pay=0.45;
                            $("#zhibiao2").css("background-color", "#00CCFF");
                            $("#zhibiao_2").css("background-color", "#00CCFF");
                        }else{
                            var temp=501-player_month_pay;
                            $("#current_level").html("本月业绩再增加"+temp+"元可提升至45%");
                            player_bl_pay=0.4;
                            $("#zhibiao1").css("background-color", "#00CCFF");
                            $("#zhibiao_1").css("background-color", "#00CCFF");
                        }

                        var valA=Math.round(player_today_pay*0.4);
                        var valB=Math.round(player_week_pay*0.4);
                        var valC=Math.round(player_month_pay*player_bl_pay);

                        $("#pay_today1").html(player_today_pay+"(40%)");
                        $("#pay_week1").html(player_week_pay+"(40%)");
                        $("#pay_month1").html(player_month_pay+"("+Math.round(player_bl_pay*100)+"%)");

                        $("#span_pay_today1").html(valA);
                        $("#span_pay_week1").html(valB);
                        $("#span_pay_month1").html(valC);

                        isCompleteOfPlayer=true;
                        if (isCompleteOfAgency){
                            valA=Math.round(player_today_pay*0.4+(agency_today_pay+agency_today0_pay)*agency_bl_pay);
                            valB=Math.round(player_week_pay*0.4+(agency_week_pay+agency_week0_pay)*agency_bl_pay);
                            valC=Math.round(player_month_pay*player_bl_pay+(agency_month_pay+agency_month0_pay)*agency_bl_pay);

                            $("#pay_today3").html(valA);
                            $("#pay_week3").html(valB);
                            $("#pay_month3").html(valC);

                            var valD=Math.round(player_month_pay*(player_bl_pay-0.4));

                            $("#span_pay_month3").html('未结:'+valD);

//                            }else{
//                            $("#span_pay_month3").css("display","none");}
                        }
                    }else if (str=="pay2"){
                       agency_today_pay=result.today/10;
                        agency_week_pay=result.week/10;
                        agency_month_pay=result.month/10;
                        agency_today0_pay=result.today0/10;
                        agency_week0_pay=result.week0/10;
                        agency_month0_pay=result.month0/10;

                        if(agencyLevel==99){
                            agency_bl_pay=0;
                        }else{
                            agency_bl_pay=0.1;
                        }

                        var valA=Math.round(agency_today_pay*agency_bl_pay);
                        var valB=Math.round(agency_week_pay*agency_bl_pay);
                        var valC=Math.round(agency_month_pay*agency_bl_pay);

                        $("#pay_today2").html(agency_today_pay+"("+(agency_bl_pay*100)+"%)");
                        $("#pay_week2").html(agency_week_pay+"("+(agency_bl_pay*100)+"%)");
                        $("#pay_month2").html(agency_month_pay+"("+(agency_bl_pay*100)+"%)");

                        $("#span_pay_today2").html(valA);
                        $("#span_pay_week2").html(valB);
                        $("#span_pay_month2").html(valC);
                        
                        valA=Math.round(agency_today0_pay*agency_bl_pay);
                        valB=Math.round(agency_week0_pay*agency_bl_pay);
                        valC=Math.round(agency_month0_pay*agency_bl_pay);

                        $("#pay_today4").html(agency_today0_pay+"("+(agency_bl_pay*100)+"%)");
                        $("#pay_week4").html(agency_week0_pay+"("+(agency_bl_pay*100)+"%)");
                        $("#pay_month4").html(agency_month0_pay+"("+(agency_bl_pay*100)+"%)");

                        $("#span_pay_today4").html(valA);
                        $("#span_pay_week4").html(valB);
                        $("#span_pay_month4").html(valC);
                        

                        isCompleteOfAgency=true;
                        if (isCompleteOfPlayer){
                            valA=Math.round(player_today_pay*0.4+(agency_today_pay+agency_today0_pay)*agency_bl_pay);
                            valB=Math.round(player_week_pay*0.4+(agency_week_pay+agency_week0_pay)*agency_bl_pay);
                            valC=Math.round(player_month_pay*player_bl_pay+(agency_month_pay+agency_month0_pay)*agency_bl_pay);

                            $("#pay_today3").html(valA);
                            $("#pay_week3").html(valB);
                            $("#pay_month3").html(valC);

                            var valD=Math.round(player_month_pay*(player_bl_pay-0.4));

                            $("#span_pay_month3").html('未结:'+valD);
//                            }else{
//                            $("#span_pay_month3").css("display","none");
//                        }
                        }
                    }else if (str=="base"){
                        if (result.myname==undefined||result.myname==null||$.trim(result.myname)==""){
                            $("#myname").html("未填写");
                            $("#myname").css("color","red");
                            $("#update_msg").css("display","");
                        }else{
                            $("#myname").html(result.myname);
                            $("#myname").css("color","#007cc3");
                            $("#update_msg").css("display","none");
                        }

//                        管理员
//                        顶级代理
//                        特级代理
//                        一级代理
//                        数据用户
                        var tt;
                        if(agencyLevel==99){
                            tt="(管理员)";
                        }else if(agencyLevel==2){
                            tt="(顶级代理)";
                        }else if(agencyLevel==1){
                            tt="(特级代理)";
                        }else if(roleId>0){
                            tt="(数据用户)";
                        }else{
                            tt="(一级代理)";
                        }

                        
                        $("#mycode").html(tt);

                        if (""==result.mypic){
                            $("#mypic").attr("src","<%=basePath%>/image/game"+loadCheckedGameId()+".png");
                        }else{
                            $("#mypic").attr("src",result.mypic);
                        }
                        popMsg = result.popMsg;
                        checkPopMsg();
                    }
                }
            },
            error : function( req, status, err) {
                console.info(status+","+err)
                var auth_url = req.getResponseHeader("REQUIRES_AUTH_URL");
                if(auth_url){
                    window.location.href = auth_url;
                }else{
//                    alert("请稍后再试");
                }
            }
        });
    }

    function myClick(obj) {
       if (obj.id=="td_today1"){
            window.location="<%=path%>/page/pay/info?gameId="+loadCheckedGameId()+"&type=1&start="+new Date().format('yyyy-MM-dd')+"&end="+new Date().format('yyyy-MM-dd');
        }else if (obj.id=="td_week1"){
            window.location="<%=path%>/page/pay/info?gameId="+loadCheckedGameId()+"&type=1&start="+getWeekStartDate()+"&end="+getWeekEndDate();
        }else if (obj.id=="td_month1"){
            window.location="<%=path%>/page/pay/info?gameId="+loadCheckedGameId()+"&type=1&start="+getMonthStartDate()+"&end="+getMonthEndDate();
        }else if (obj.id=="td_today2"){
            window.location="<%=path%>/page/pay/info?gameId="+loadCheckedGameId()+"&type=2&start="+new Date().format('yyyy-MM-dd')+"&end="+new Date().format('yyyy-MM-dd');
        }else if (obj.id=="td_week2"){
            window.location="<%=path%>/page/pay/info?gameId="+loadCheckedGameId()+"&type=2&start="+getWeekStartDate()+"&end="+getWeekEndDate();
        }else if (obj.id=="td_month2"){
            window.location="<%=path%>/page/pay/info?gameId="+loadCheckedGameId()+"&type=2&start="+getMonthStartDate()+"&end="+getMonthEndDate();
        } else if (obj.id=="td_today3"){
            window.location="<%=path%>/page/pay/info?gameId="+loadCheckedGameId()+"&type=3&start="+new Date().format('yyyy-MM-dd')+"&end="+new Date().format('yyyy-MM-dd');
        }else if (obj.id=="td_week3"){
            window.location="<%=path%>/page/pay/info?gameId="+loadCheckedGameId()+"&type=3&start="+getWeekStartDate()+"&end="+getWeekEndDate();
        }else if (obj.id=="td_month3"){
            window.location="<%=path%>/page/pay/info?gameId="+loadCheckedGameId()+"&type=3&start="+getMonthStartDate()+"&end="+getMonthEndDate();
        } 
    }


    //格式化日期：yyyy-MM-dd 
	function formatDate(date) { 
	var myyear = date.getFullYear(); 
	var mymonth = date.getMonth()+1; 
	var myweekday = date.getDate(); 
	
	if(mymonth < 10){ 
	mymonth = "0" + mymonth; 
	} 
	if(myweekday < 10){ 
	myweekday = "0" + myweekday; 
	} 
	return (myyear+"-"+mymonth + "-" + myweekday); 
	} 
    
	    //获得本周的开始日期 
	function getWeekStartDate() { 
	var weekStartDate = new Date(nowYear, nowMonth, nowDay - nowDayOfWeek+1); 
	return formatDate(weekStartDate); 
	} 
	
	//获得本周的结束日期 
	function getWeekEndDate() { 
	var weekEndDate = new Date(nowYear, nowMonth, nowDay + (7 - nowDayOfWeek)); 
	return formatDate(weekEndDate); 
	} 
	
	//获得本月的开始日期 
	function getMonthStartDate(){ 
	var monthStartDate = new Date(nowYear, nowMonth, 1); 
	return formatDate(monthStartDate); 
	} 
	
	//获得本月的结束日期 
	function getMonthEndDate(){ 
	var monthEndDate = new Date(nowYear, nowMonth, getMonthDays(nowMonth)); 
	return formatDate(monthEndDate); 
	}
	//获得某月的天数 
	function getMonthDays(myMonth){ 
	var monthStartDate = new Date(nowYear, myMonth, 1); 
	var monthEndDate = new Date(nowYear, myMonth + 1, 1); 
	var days = (monthEndDate - monthStartDate)/(1000 * 60 * 60 * 24); 
	return days; 
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