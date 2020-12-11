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
        .icon1{
            width: 44px;height: 36px;margin-top: 8px;
        }
        .icon2{
            width: 18px;height: 32px;margin-top: 8px;
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
            &nbsp;钻石数：${sessionScope.roomCard.commonCard}
        </td>
        <td rowspan="2" style="text-align: center;vertical-align: middle;color: #007cc3;font-size: 18px;padding-top: 5px;">
            <div>
                <img id="update_msg" onclick="window.location='<%=path%>/page/user/detail?gameId='+localStorage.getItem('gameId')" style="width: 80px;height: 32px;" src="<%=basePath%>/image/update_msg.png" />
            </div>
            <div>
                <img onclick="window.location='<%=path%>/user/logout?gameId='+localStorage.getItem('gameId')" style="width: 80px;height: 32px;" src="<%=basePath%>/image/quit.png" />
            </div>
        </td>
    </tr>
    <tr>
        <td colspan="2" style="text-align: left;vertical-align: top;color: #007cc3;height: 36px;">
            &nbsp;<span id="agency_tip" style="color: red;font-size: 12px;">累积业绩1万元可创建下级代理</span>
        </td>
    </tr>
</table>
<%@include file="./switchGame.html"%>
<marquee id="marqueeText" behavior="scroll" direction="left" style="color: red;margin-top: 5px;display: none;">${sessionScope.marqueeText}</marquee>
<div id="div_content" style="display: block;">
    <div class="table1">
        <table cellspacing="0" cellpadding="9" style="text-align:center;width: 100%;margin-top: 5px;color: #393a3e;font-size: 16px;">
            <!--<tr>-->
            <!--<td colspan="6" style="text-align: center;">代理分成比例</td>-->
            <!--</tr>-->
            <!--style="background: url('<%=basePath%>/image/background2.png') no-repeat;background-size:99%;"-->
            <tr>
                <td>月业绩</td>
                <td id="zhibiao_1">0</td>
                <td id="zhibiao_2">1001</td>
                <td id="zhibiao_3">3001</td>
                <td id="zhibiao_4">6001</td>
                <td id="zhibiao_5">10001</td>
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
                <td>代理充值</td>
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
                    <span style="font-size: 14px;">您可获得下级和下下级代理绑码充值金额10%返利</span>
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
              <%-- <tr onclick="clickItems(13)" id="buy_cards1">
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/bi1.png" class="icon1"/></td>
                <td>代理购钻</td>
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png"  class="icon2"/></td>
            </tr> --%>
            <tr id="my_qrcode_item" onclick="clickItems(7)">
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/qrcode1.png" class="icon1"/></td>
                <td>发展代理</td>
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png"  class="icon2"/></td>
            </tr>
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
            <tr onclick="clickItems(0)">
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/cards1.png" class="icon1"/></td>
                <td>管理</td>
                <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png"  class="icon2"/></td>
            </tr>
           <%--  <tr onclick="clickItems(6)"><td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/cards1.png" class="icon1"/></td> <td>综合数据</td> <td style="text-align: center; width: 12px;"><img src="<%=basePath%>/image/jiantou.png"  class="icon2"/></td> </tr> --%>
        </table>
    </div>
</div>
<input type="hidden" id="agencyId" value="${sessionScope.roomCard.agencyId}"/>
<input type="hidden" id="agencyLevel" value="${sessionScope.roomCard.agencyLevel}"/>
<input type="hidden" id="roleId" value="${sessionScope.user.roleId}">
</body>

<script>

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

    var my_players_count=0;


    $(document).ready(function () {

        var gameId = localStorage.getItem("gameId");
        if (gameId!=null&&gameId!=""){
            checkTab("tabGame_"+gameId);
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
        if (agencyLevel>0){
            $("#agency_tip").css("color","blue");
            $("#agency_tip").html("您已获得创建下级代理权限");
        }else{
            $("#my_qrcode_item").css("display","none");
        }
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
                window.location="<%=path%>/page/khmanage?gameId="+loadCheckedGameId();
                break;
            case 1:
                window.location="<%=path%>/page/income?gameId="+loadCheckedGameId();
                break;
            case 2:
                window.location="<%=path%>/page/player?total="+my_players_count+"&gameId="+loadCheckedGameId();
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
            case 13:
                window.location="<%=path%>/page/buy/cards?gameId="+loadCheckedGameId();//代理购卡
                break;
            case 14:
                window.location = "<%=path%>/page/pay/for/player?gameId="+loadCheckedGameId();
                break;
            case 15:
                window.location = "<%=path%>/page/pay/for/agency?gameId="+loadCheckedGameId();
                break;
            case 12:
                 window.location = "<%=path%>/page/income?gameId="+loadCheckedGameId();//提现
                break;
            case 10:
                window.location="<%=path%>/page/manage?gameId="+loadCheckedGameId();
                break;
        }
    }

    function loadMyQrcode(){

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

                        if (player_month_pay>10000){
                            $("#current_level").html("恭喜！你已达到最高返佣比例！");
                            player_bl_pay=0.6;
                            $("#zhibiao5").css("background-color", "#00CCFF");
                            $("#zhibiao_5").css("background-color", "#00CCFF");
                        }else if (player_month_pay>6000){
                            var temp=10001-player_month_pay;
                            $("#current_level").html("本月业绩再增加"+temp+"元可提升至60%");
                            player_bl_pay=0.55;
                            $("#zhibiao4").css("background-color", "#00CCFF");
                            $("#zhibiao_4").css("background-color", "#00CCFF");
                        }else if (player_month_pay>3000){
                            var temp=6001-player_month_pay;
                            $("#current_level").html("本月业绩再增加"+temp+"元可提升至55%");
                            player_bl_pay=0.5;
                            $("#zhibiao3").css("background-color", "#00CCFF");
                            $("#zhibiao_3").css("background-color", "#00CCFF");
                        }else if (player_month_pay>1000){
                            var temp=3001-player_month_pay;
                            $("#current_level").html("本月业绩再增加"+temp+"元可提升至50%");
                            player_bl_pay=0.45;
                            $("#zhibiao2").css("background-color", "#00CCFF");
                            $("#zhibiao_2").css("background-color", "#00CCFF");
                        }else{
                            var temp=1001-player_month_pay;
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
                            valA=Math.round(player_today_pay*0.4+agency_today_pay*agency_bl_pay);
                            valB=Math.round(player_week_pay*0.4+agency_week_pay*agency_bl_pay);
                            valC=Math.round(player_month_pay*player_bl_pay+agency_month_pay*agency_bl_pay);

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

                        isCompleteOfAgency=true;
                        if (isCompleteOfPlayer){
                            valA=Math.round(player_today_pay*0.4+agency_today_pay*agency_bl_pay);
                            valB=Math.round(player_week_pay*0.4+agency_week_pay*agency_bl_pay);
                            valC=Math.round(player_month_pay*player_bl_pay+agency_month_pay*agency_bl_pay);

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
                            tt="(顶级)";
                        }else if(agencyLevel==1){
                            tt="(特级)";
                        }else if(roleId>0){
                            tt="(数据用户)";
                        }else{
                            tt="(一级)";
                        }

                        $("#mycode").html(tt);

                        if (""==result.mypic){
                            $("#mypic").attr("src","<%=basePath%>/image/game.png");
                        }else{
                            $("#mypic").attr("src",result.mypic);
                        }
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
            window.location="<%=path%>/page/pay/history/player?date=today&gameId="+loadCheckedGameId();
        }else if (obj.id=="td_week1"){
            window.location="<%=path%>/page/pay/history/player?date=week&gameId="+loadCheckedGameId();
        }else if (obj.id=="td_month1"){
            window.location="<%=path%>/page/pay/history/player?date=month&gameId="+loadCheckedGameId();
        }else if (obj.id=="td_today2"){
            window.location="<%=path%>/page/pay/history/agency?date=today&gameId="+loadCheckedGameId();
        }else if (obj.id=="td_week2"){
            window.location="<%=path%>/page/pay/history/agency?date=week&gameId="+loadCheckedGameId();
        }else if (obj.id=="td_month2"){
            window.location="<%=path%>/page/pay/history/agency?date=month&gameId="+loadCheckedGameId();
        }
    }
</script>
</html>