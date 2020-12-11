<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String path = request.getContextPath()+"";
    String basePath = request.getScheme()+"://"+request.getServerName()+(request.getServerPort()==80?"":(":"+request.getServerPort()))+request.getContextPath();
%>
<!DOCTYPE html>
<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />
        <link rel="stylesheet" type="text/css" href="<%=basePath%>/css/reset.css"/>
        <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
        <title></title>
        <style>
        	body{background: url(<%=basePath%>/image/bg.png) no-repeat;background-size:cover;}
        	.info-box{width: 7rem;height: 1.4rem;background: rgba(227,183,77,0.3);border-radius: .4rem;}
        	.info{width: 6.6rem;height: 1.1rem;background: rgb(227,183,77);border-radius: .3rem;}
        	.head{width: .9rem;height: .9rem;border-radius: .1rem;border: 1px solid #BC5435;overflow: hidden;}
        	.img150-60{width: 1.5rem;height: .6rem;}
        	.content-box{width: 7rem;background: rgba(236,84,84,0.3);border-radius: .4rem;}
        	.content{width: 6.6rem;background: rgb(236,84,84);border-radius: .3rem;}
        	.item{width: 6.2rem;height: 1.4rem;}
        	.item1{background: url(<%=basePath%>/image/jx-in.png) no-repeat;background-size: 100% 100%;}
        	.item2{background: url(<%=basePath%>/image/jx1.png) no-repeat;background-size: 100% 100%;}
        	.item3,.item4{background: url(<%=basePath%>/image/jx.png) no-repeat;background-size: 100% 100%;}
        	.img44{width: .44rem;position: absolute;left: .2rem;top: .08rem;}
        	.color-red{color: #AD1414;}.font-w{font-weight: bold;}
        	.img144{width: 1.44rem;position: absolute;left: 50%;margin-left: -.72rem;top: 50%; margin-top: -.3rem;}
        	.color-y{color: #F0E92E;}
        </style>
    </head>
    <body>
    	<div class="center-float-center mt50">
    		<img width="25%" src="<%=basePath%>/image/logo.png" />
    		<img width="70%" src="<%=basePath%>/image/qyq.png" />
    	</div>
    	<!--基本信息-->
    	<div class="center-float-center mt40">
    		<div class="info-box center-float-center">
	    		<div class="info center-float-left top-lr-scatter box-sizing pl20 pr20">
	    			<div class="center-float-left">
	    				<div class="head center-float-center"><img width="100%" src="${sessionScope.gm1img}" /></div>
	    				<div class="ml15 color-fff">
	    					<p><span>${sessionScope.groupName}</span><span class="ml20">ID:${sessionScope.groupNo}</span></p>
	    					<p>管理员：${sessionScope.gmName}</p>
	    				</div>
	    			</div>
	    			<div class="mr20"><img class="img150-60" src="<%=basePath%>/image/ds.png" onclick="lookphoto(0)"/></div>
	    		</div>
	    	</div>
    	</div>
    	<!--列表-->
    	<div class="center-float-center">
    		<div class="content-box mt30 center-float-center pt15 pb15">
	    		<div class="content pt20 pb20 position-re">
	    			<img style="position: absolute;right: .7rem;bottom: .7rem;width: .5rem;" src="<%=basePath%>/image/saizi.png" />
						<img style="position: absolute;right: 0rem;bottom: -1.5rem;width: 1.5rem;" src="<%=basePath%>/image/mj-q1.png" />
	    				
	    			<div class="item item1 auto center-float-left top-lr-scatter box-sizing pl20 pr20 position-re">
	    				<div class="center-float-left ml45">
		    				<div class="head center-float-center"><img width="100%" src="${sessionScope.player1img}" /></div>
		    				<div class="ml15 color-red">
		    					<p class="ft30 font-w ellipsis_one" style="width: 1.8rem;">${sessionScope.player1Name}</p>
		    					<p>ID：${sessionScope.player1ID}</p>
		    				</div>
		    			</div>
		    			<div class="center-float-left">
		    				<p id="s1" class="ft36 mr10" style="color: #445161;text-shadow:1px 1px 1px #674154;">${sessionScope.player1No}</p>
		    				<img class="img150-60" src="<%=basePath%>/image/ds.png"  id="player1Status1" onclick="dashang(1)"/>
		    				<img class="img150-60" src="<%=basePath%>/image/zanwu.png" id="player1Status2"    />
		    				<img class="img150-60" src="<%=basePath%>/image/xiugai.png" id="player1Status3"onclick="lookphoto(1)" />
		    				<img class="img150-60" src="<%=basePath%>/image/sc.png" id="player1Status4"onclick="lookphoto(1)"/>
		    			</div>
		    			<img id="p1t1" class="img44" src="<%=basePath%>/image/dyj.png" />
		    			<img id="p1t2" class="img44" src="<%=basePath%>/image/fh.png" />
	    			</div>
	    			
	    			<div class="item item2 auto center-float-left top-lr-scatter box-sizing pl20 pr20 position-re">
	    				<div class="center-float-left ml45">
		    				<div class="head center-float-center"><img width="100%" src="${sessionScope.player2img}" /></div>
		    				<div class="ml15 color-red">
		    					<p class="ft30 font-w ellipsis_one" style="width: 1.8rem;">${sessionScope.player2Name}</p>
		    					<p>ID：${sessionScope.player2ID}</p>
		    				</div>
		    			</div>
		    			<div class="center-float-left">
		    				<p id="s2" class="ft36 mr10" style="color: #e01818;text-shadow:1px 1px 1px #674154;">${sessionScope.player2No}</p>
		    				<img class="img150-60" src="<%=basePath%>/image/ds.png"  id="player2Status1" onclick="dashang(2)"/>
		    				<img class="img150-60" src="<%=basePath%>/image/zanwu.png" id="player2Status2"    />
		    				<img class="img150-60" src="<%=basePath%>/image/xiugai.png" id="player2Status3"onclick="lookphoto(2)" />
		    				<img class="img150-60" src="<%=basePath%>/image/sc.png" id="player2Status4"onclick="lookphoto(2)"/>
		    			</div>
		    			<img id="p2t1" class="img44" src="<%=basePath%>/image/dyj.png" />
		    			<img id="p2t2" class="img44" src="<%=basePath%>/image/fh.png" />
	    			</div>
	    			
	    			<div class="item item3 auto center-float-left top-lr-scatter box-sizing pl20 pr20 position-re">
	    				<div class="center-float-left ml45">
		    				<div class="head center-float-center"><img width="100%" src="${sessionScope.player3img}" /></div>
		    				<div class="ml15 color-red">
		    					<p class="ft30 font-w ellipsis_one" style="width: 1.8rem;">${sessionScope.player3Name}</p>
		    					<p>ID：${sessionScope.player3ID}</p>
		    				</div>
		    			</div>
		    			<div class="center-float-left">
		    				<p id="s3" class="ft36 mr10" style="color: #e01818;text-shadow:1px 1px 1px #674154;">${sessionScope.player3No}</p>
		    				<img class="img150-60" src="<%=basePath%>/image/ds.png"  id="player3Status1" onclick="dashang(3)"/>
		    				<img class="img150-60" src="<%=basePath%>/image/zanwu.png" id="player3Status2"    />
		    				<img class="img150-60" src="<%=basePath%>/image/xiugai.png" id="player3Status3"onclick="lookphoto(3)" />
		    				<img class="img150-60" src="<%=basePath%>/image/sc.png" id="player3Status4"onclick="lookphoto(3)"/>
		    			</div>
		    			<img id="p3t1" class="img44" src="<%=basePath%>/image/dyj.png" />
		    			<img id="p3t2" class="img44" src="<%=basePath%>/image/fh.png" />
	    			</div>
	    			
	    			<div class="item item4 auto center-float-left top-lr-scatter box-sizing pl20 pr20 position-re">
	    					    				<div class="center-float-left ml45">
		    				<div class="head center-float-center"><img width="100%" src="${sessionScope.player4img}" /></div>
		    				<div class="ml15 color-red">
		    					<p class="ft30 font-w ellipsis_one" style="width: 1.8rem;">${sessionScope.player4Name}</p>
		    					<p>ID：${sessionScope.player4ID}</p>
		    				</div>
		    			</div>
		    			<div class="center-float-left">
		    				<p id="s4" class="ft36 mr10" style="color: #e01818;text-shadow:1px 1px 1px #674154;">${sessionScope.player4No}</p>
		    				<img class="img150-60" src="<%=basePath%>/image/ds.png"  id="player4Status1" onclick="dashang(4)"/>
		    				<img class="img150-60" src="<%=basePath%>/image/zanwu.png" id="player4Status2"    />
		    				<img class="img150-60" src="<%=basePath%>/image/xiugai.png" id="player4Status3"onclick="lookphoto(4)" />
		    				<img class="img150-60" src="<%=basePath%>/image/sc.png" id="player4Status4"onclick="lookphoto(4)"/>
		    			</div>
		    			<img id="p4t1" class="img44" src="<%=basePath%>/image/dyj.png" />
		    			<img id="p4t2" class="img44" src="<%=basePath%>/image/fh.png" />
	    			</div>
	    			<div class="color-y pl30">
	    				<p id="difen2"  class="mt10">${sessionScope.gameName}   &nbsp;&nbsp;底分：${sessionScope.difen}</p>
	    				<p id="difen1" class="mt10">${sessionScope.gameName}   </p>
	    				<p class="mt5"><span>房号：${sessionScope.roomId}  &nbsp;</span><span>局数：${sessionScope.playNo}/${sessionScope.totalCount}</span></p>
	    				<p class="mt5">结束时间：${sessionScope.overTime}</p>
	    			</div>
	    		</div>
	    	</div>
    	</div>
    	<p style="color: #A03838;" class="text-center mt50">VIP代理招募：kuailewan666</p>
    	
    	
 	</body>
 	<%-- <input type="hidden" id="gameName" value="${sessionScope.gameName}"/> --%>
<input type="hidden" id="playNo" value="${sessionScope.playNo}"/>
<input type="hidden" id="roomId" value="${sessionScope.roomId}">
<input type="hidden" id="overTime" value="${sessionScope.overTime}">
<input type="hidden" id="gameName" value="${sessionScope.gameName}">
<input type="hidden" id="totalCount" value="${sessionScope.totalCount}">
<input type="hidden" id="difen" value="${sessionScope.difen}">

<input type="hidden" id="player1Name" value="${sessionScope.player1Name}"/>
<input type="hidden" id="player1ID" value="${sessionScope.player1ID}"/>
<input type="hidden" id="player1No" value="${sessionScope.player1No}"/>
<input type="hidden" id="player1img" value="${sessionScope.player1img}">
<input type="hidden" id="player1Status" value="${sessionScope.player1Status}">
<input type="hidden" id="player1Photo" value="${sessionScope.player1Photo}">

<input type="hidden" id="player2No" value="${sessionScope.player2No}"/>
<input type="hidden" id="player2ID" value="${sessionScope.player2ID}">
<input type="hidden" id="player2Name" value="${sessionScope.player2Name}">
<input type="hidden" id="player2img" value="${sessionScope.player2img}">
<input type="hidden" id="player2Status" value="${sessionScope.player2Status}">
<input type="hidden" id="player2Photo" value="${sessionScope.player2Photo}">

<input type="hidden" id="player3No" value="${sessionScope.player3No}"/>
<input type="hidden" id="player3ID" value="${sessionScope.player3ID}">
<input type="hidden" id="player3Name" value="${sessionScope.player3Name}">
<input type="hidden" id="player3img" value="${sessionScope.player3img}">
<input type="hidden" id="player3Status" value="${sessionScope.player3Status}">
<input type="hidden" id="player3Photo" value="${sessionScope.player3Photo}">

<input type="hidden" id="player4No" value="${sessionScope.player4No}"/>
<input type="hidden" id="player4ID" value="${sessionScope.player4ID}">
<input type="hidden" id="player4Name" value="${sessionScope.player4Name}">
<input type="hidden" id="player4img" value="${sessionScope.player4img}">
<input type="hidden" id="player4Status" value="${sessionScope.player4Status}">
<input type="hidden" id="player4Photo" value="${sessionScope.player4Photo}">

<input type="hidden" id="gmName" value="${sessionScope.gmName}"/>
<input type="hidden" id="groupName" value="${sessionScope.groupName}">
<input type="hidden" id="groupNo" value="${sessionScope.groupNo}">
<input type="hidden" id="gm1img" value="${sessionScope.gm1img}">
<input type="hidden" id="groupUserID" value="${sessionScope.groupUserID}">

<input type="hidden" id="myuserid" value="${sessionScope.myuserid}">

</html>
<script>


 var browser = {
	    versions: function () {
	        var u = navigator.userAgent, app = navigator.appVersion;
	        return {   //移动终端浏览器版本信息
	            trident: u.indexOf('Trident') > -1, //IE内核
	            presto: u.indexOf('Presto') > -1, //opera内核
	            webKit: u.indexOf('AppleWebKit') > -1, //苹果、谷歌内核
	            gecko: u.indexOf('Gecko') > -1 && u.indexOf('KHTML') == -1, //火狐内核
	            mobile: !!u.match(/AppleWebKit.*Mobile.*/), //是否为移动终端
	            ios: !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), //ios终端
	            android: u.indexOf('Android') > -1 || u.indexOf('Linux') > -1, //android终端或uc浏览器
	            iPhone: u.indexOf('iPhone') > -1, //是否为iPhone或者QQHD浏览器
	            iPad: u.indexOf('iPad') > -1, //是否iPad
	            webApp: u.indexOf('Safari') == -1 //是否web应该程序，没有头部与底部
	        };
	    }(),
	    language: (navigator.browserLanguage || navigator.language).toLowerCase()
	} 
	 


    var myOperate=1;//1添加红名2解除红名
  //  $(document).ready(function () {
    	var player1No=document.getElementById('player1No').value; 
    	var player2No=document.getElementById('player2No').value; 
    	var player3No=document.getElementById('player3No').value;  
    	var player4No=document.getElementById('player4No').value; 
    	/*var player4No=document.getElementById('player4No').value;  */
    	
    	var difen1=document.getElementById('difen').value; 
		if(difen1>=0&&difen1.length>0){
			$('#difen1').hide();
		}else{
			$('#difen2').hide();
		}

    	if(player1No>=0){
    		var s1 = document.getElementById("s1");
    		s1.innerText= "+"+s1.innerText; 
    		$("#s1").css({"color":"#e01818"})
    	}else{
    		$("#s1").css({"color":"#445161"})
    	}
    	
    	if(player2No>=0){
    		var s2 = document.getElementById("s2");
    		s2.innerText= "+"+s2.innerText; 
    		$("#s2").css({"color":"#e01818"})
    	}else{
    		$("#s2").css({"color":"#445161"})
    	}
    	
    	if(player3No>=0){
    		var s3 = document.getElementById("s3");
    		s3.innerText= "+"+s3.innerText; 
    		$("#s3").css({"color":"#e01818"})
    	}else{
    		$("#s3").css({"color":"#445161"})
    	}
    	
    	if(player4No>=0){
    		var s4 = document.getElementById("s4");
    		s4.innerText= "+"+s4.innerText;
    		$("#s4").css({"color":"#e01818"})
    	}else{
    		$("#s4").css({"color":"#445161"})
    	}
    	
    	
    	var a=[player1No,player2No,player3No,player4No];
    	
    	var b1=0;
    	var b2=0;
    	var b3=0;
    	var b4=0;
    	
      	if(player1No==Math.max.apply(null, a)){
    		$('#p1t2').hide();
    		$('.item1').css({"background":"url(<%=basePath%>/image/jx-in.png) no-repeat","background-size":"100% 100%"});
    		$("s1").css({"color":"#445161"})
    	}else{
    		$('.item1').css({"background":"url(<%=basePath%>/image/jx1.png) no-repeat","background-size":"100% 100%"});
    		b1=b1+1;
    	}
    	if(player1No==Math.min.apply(null, a)){
    		$('#p1t1').hide();
    		$('.item1').css({
  			  "background":"url(<%=basePath%>/image/jx1.png) no-repeat",
  			  "background-size":"100% 100%"
  			  });
    	}else{
    		b1=b1+1;
    	}
    	
    	if(b1==2){
    		$('#p1t2').hide();
    		$('#p1t1').hide();
    	}
    	
    	if(player2No==Math.max.apply(null, a)){
    		$('#p2t2').hide();
    		$('.item2').css({"background":"url(<%=basePath%>/image/jx-in.png) no-repeat","background-size":"100% 100%"});
    		$("s2").css({"color":"#445161"})
    	}else{
    		$('.item2').css({"background":"url(<%=basePath%>/image/jx1.png) no-repeat","background-size":"100% 100%"});
    		b2=b2+1;
    	}
    	if(player2No==Math.min.apply(null, a)){
    		$('#p2t1').hide();
    		$('.item2').css({"background":"url(<%=basePath%>/image/jx1.png) no-repeat","background-size":"100% 100%"});
    		
    	}else{
    		b2=b2+1;
    	}
    	if(b2==2){
    		$('#p2t2').hide();
    		$('#p2t1').hide();
    	}
    	
    	if(player3No==Math.max.apply(null, a)){
    		$('#p3t2').hide();
    		$('.item3').css({"background":"url(<%=basePath%>/image/jx-in.png) no-repeat","background-size":"100% 100%"});
    		$("s3").css({"color":"#445161"})
    	}else{
    		$('.item3').css({"background":"url(<%=basePath%>/image/jx1.png) no-repeat","background-size":"100% 100%"});
    		b3=b3+1;
    	}
    	if(player3No==Math.min.apply(null, a)){
    		$('#p3t1').hide();
    		$('.item3').css({"background":"url(<%=basePath%>/image/jx1.png) no-repeat","background-size":"100% 100%"});
    	}else{
    		b3=b3+1;
    	}
    	if(b3==2){
    		$('#p3t2').hide();
    		$('#p3t1').hide();
    	}
    	
    	if(player4No==Math.max.apply(null, a)){
    		$('#p4t2').hide();
    		$('.item4').css({"background":"url(<%=basePath%>/image/jx-in.png) no-repeat","background-size":"100% 100%"});
    		$("s4").css({"color":"#445161"})
    	}else{
    		$('.item4').css({"background":"url(<%=basePath%>/image/jx1.png) no-repeat","background-size":"100% 100%"});
    		b4=b4+1;
    	}
    	if(player4No==Math.min.apply(null, a)){
    		$('#p4t1').hide();
    		$('.item4').css({"background":"url(<%=basePath%>/image/jx1.png) no-repeat","background-size":"100% 100%"});
    	}else{
    		b4=b4+1;
    	}
    	if(b4==2){
    		$('#p4t2').hide();
    		$('#p4t1').hide();
    	}
    	
         var gameId=localStorage.getItem("gameId");
         var player1Status=document.getElementById('player1Status').value; 
         var player2Status=document.getElementById('player2Status').value; 
         var player3Status=document.getElementById('player3Status').value; 
         var player4Status=document.getElementById('player4Status').value; 
         if(player1Status==1){
        	 $("#player1Status2").hide(); 
        	 $("#player1Status3").hide(); 
        	 $("#player1Status4").hide();  
         }else if(player1Status==2){
        	 $("#player1Status1").hide(); 
        	 $("#player1Status3").hide(); 
        	 $("#player1Status4").hide(); 
			}else if(player1Status==3){
				 $("#player1Status2").hide(); 
	        	 $("#player1Status1").hide(); 
	        	 $("#player1Status4").hide(); 
			}else if(player1Status==4){
				 $("#player1Status2").hide(); 
	        	 $("#player1Status1").hide(); 
	        	 $("#player1Status3").hide(); 
			}
         
         if(player2Status==1){
        	 $("#player2Status2").hide(); 
        	 $("#player2Status3").hide(); 
        	 $("#player2Status4").hide(); 
         }else if(player2Status==2){
        	 $("#player2Status1").hide(); 
        	 $("#player2Status3").hide(); 
        	 $("#player2Status4").hide(); 
			}else if(player2Status==3){
				 $("#player2Status2").hide(); 
	        	 $("#player2Status1").hide(); 
	        	 $("#player2Status4").hide(); 
			}else if(player2Status==4){
				 $("#player2Status2").hide(); 
	        	 $("#player2Status1").hide(); 
	        	 $("#player2Status3").hide(); 
			}
         
         if(player3Status==1){
        	 $("#player3Status2").hide(); 
        	 $("#player3Status3").hide(); 
        	 $("#player3Status4").hide(); 
         }else if(player3Status==2){
        	 $("#player3Status1").hide(); 
        	 $("#player3Status3").hide(); 
        	 $("#player3Status4").hide(); 
			}else if(player3Status==3){
				 $("#player3Status2").hide(); 
	        	 $("#player3Status1").hide(); 
	        	 $("#player3Status4").hide(); 
			}else if(player3Status==4){
				 $("#player3Status2").hide(); 
	        	 $("#player3Status1").hide(); 
	        	 $("#player3Status3").hide(); 
			}
         
         if(player4Status==1){
        	 $("#player4Status2").hide(); 
        	 $("#player4Status3").hide(); 
        	 $("#player4Status4").hide(); 
         }else if(player4Status==2){
        	 $("#player4Status1").hide(); 
        	 $("#player4Status3").hide(); 
        	 $("#player4Status4").hide(); 
			}else if(player4Status==3){
				 $("#player4Status2").hide(); 
	        	 $("#player4Status1").hide(); 
	        	 $("#player4Status4").hide(); 
			}else if(player4Status==4){
				 $("#player4Status2").hide(); 
	        	 $("#player4Status1").hide(); 
	        	 $("#player4Status3").hide(); 
			}
         
          isLoad = true;
       /*    document.getElementById('guanli').src = */
        	  var player1img11 =document.getElementById('player1img').value; 
       
         	console.log(player1img11);
         	$('#guanli').attr('src',player1img11);
               if (browser.versions.mobile) {//判断是否是移动设备打开。browser代码在下面
              var ua = navigator.userAgent.toLowerCase();//获取判断用的对象

                if (ua.match(/MicroMessenger/i) == "micromessenger") {
                  //在微信中打开
                  var height=document.body.scrollHeight;//-$(".header_table").height();

                  $("#div_help").css("height",height);
                  if (browser.versions.ios) {
                      //是否在IOS浏览器打开
                      $("#div_help").css("background-image","url('<%=basePath%>/image/ios_h5_help.png')");
                      $("#div_help").css("display","");
                  }
                  if(browser.versions.android){
                      //是否在安卓浏览器打开
                      $("#div_help").css("background-image","url('<%=basePath%>/image/android_h5_help.png')");
                      $("#div_help").css("display","");
                  }
              }else{
                   window.location="${url}"; 
                   alert("请在微信中打开");
              } 

          } else {
              //否则就是PC浏览器打开
            /*   alert("请在手机浏览器中访问"); */
          }   

 //   });

    function dashang(types) {
    	var playerphoto=0;
        if(types==1){
        	playerphoto=document.getElementById('player1Photo').value; 
        	window.location.href=playerphoto;    
        }
        if(types==2){
        	playerphoto=document.getElementById('player2Photo').value; 
        	window.location.href=playerphoto;    
        }
        if(types==3){
        	playerphoto=document.getElementById('player3Photo').value; 
        	window.location.href=playerphoto;    
        }
        if(types==4){
        	playerphoto=document.getElementById('player4Photo').value; 
        	window.location.href=playerphoto;    
        }
    	
    }

    function clearMsg() {
        $('#playerName').html('');
        $("#myIco").css("display","none");
        $('#operate').css("display","none");
    }

     function lookphoto(types) {
    	 /*  var userId = $("#userId").val().trim();
            if (userId==""||isNaN(userId)){
               alert("请输入游戏ID");
               return;
           }  */
           var name=0;
           var no=0;
           var img=0;
           var userID=0
           var myid =document.getElementById('myuserid').value;
           if(types==0){
        	   name=document.getElementById('gmName').value; 
        	   img=document.getElementById('gm1img').value; 
        	   userID=document.getElementById('groupUserID').value; 
           }
           if(types==1){
        	   name=document.getElementById('player1Name').value; 
        	   img=document.getElementById('player1img').value; 
        	   userID=document.getElementById('player1ID').value; 
        	   no=document.getElementById('player1No').value; 
           }
           if(types==2){
        	   name=document.getElementById('player2Name').value; 
        	   img=document.getElementById('player2img').value; 
        	   userID=document.getElementById('player2ID').value; 
        	   no=document.getElementById('player2No').value; 
           }
           if(types==3){
        	   name=document.getElementById('player3Name').value; 
        	   img=document.getElementById('player3img').value; 
        	   userID=document.getElementById('player3ID').value; 
        	   no=document.getElementById('player3No').value; 
           }
           if(types==4){
        	   name=document.getElementById('player4Name').value; 
        	   img=document.getElementById('player4img').value; 
        	   userID=document.getElementById('player4ID').value; 
        	   no=document.getElementById('player4No').value; 
           }
           $.ajax({
               timeout: 60000,
               async: true,
               type: "POST",
               url: "<%=path%>/noauth/checkimgi",
               data: {
            	   name:name,
            	   no:no,img:img,userID:userID,myid:myid
               },
               dataType: "json",
               success: function (result) {
                   if(result.code==1000){
                	   window.location.href='<%=basePath%>/noauth/checkimgOK';//正确登录后页面跳转至
               } else {
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

  <%--   function loadUserIdInfo() {
        var userId = $("#userId").val().trim();
      /*   if (userId==""||isNaN(userId)){
            alert("请输入游戏ID");
            return;
        } */
        $.ajax({
            timeout: 60000,
            async: true,
            type: "POST",
            url: "<%=path%>/user/checkimg",
            data: {
            	userId:$("#userId").val(),
                gameId:$("#gameId").val(),pageNo:page
            },
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json转字符串
//                JSON.parse(jsonStr);//字符串转json
                if(result.code==1000){$("#table_data").html('<tr style="color: #ee6a2a;font-size: 18px;"><td>游戏ID</td><td>昵称</td><td>查看图片</td>操作</tr>');
                $.each(result.data, function (index, tempData) {
                	totalPage = result.page;
                    if(totalPage <= 1){
                      $("#pp").hide();
                    }
                     $("#page").html(page+"/"+totalPage);
                    var newRow = "<tr onclick='displayData(this.id)' id='data" + tempData.userid + "'><td>" + tempData.userid + "</td><td>" + tempData.nickname + "</td><td><img src='"+tempData.img+"' /></td><td><button type='button' onclick='del("+tempData.id+")'>不符</button><button type='button' onclick='ok("+tempData.id+")'>符合</button></td></tr>";
                 

                    newRow += '</table></td></tr>';

                    $('#table_data').append(newRow);
                });
            } else {
                alert(result.message);
            }
        },
            error : function( req, status, err) {
                console.info(status+","+err)+
                var auth_url = req.getResponseHeader("REQUIRES_AUTH_URL");
                if(auth_url){
                    window.location.href = auth_url;
                }else{
                    alert("请稍后再试");
                }
            }
        });
    } --%>
    
    
 

</script>