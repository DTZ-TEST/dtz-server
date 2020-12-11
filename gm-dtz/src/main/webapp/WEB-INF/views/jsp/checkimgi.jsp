<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+(request.getServerPort()==80?"":(":"+request.getServerPort()))+request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport"
	content="width=device-width,initial-scale=1,maximum-scale=1.0" />
<title id="title">快乐玩游戏代理后台_快乐打筒子_快乐跑得快_快乐跑胡子</title>

<style>
        	body{background: url(<%=basePath%>/image/code-bg.png) no-repeat;background-size:cover;}
        	.info-box{width: 7rem;height: 2.1rem;background: rgba(34,191,115,0.3);border-radius: .4rem;}
        	.info{width: 6.6rem;height: 1.9rem;background: rgb(34,191,115);border-radius: .3rem;}
        	.head{width: .9rem;height: .9rem;border-radius: .1rem;border: 1px solid #BC5435;overflow: hidden;}
        	.img150-60{width: 1.5rem;height: .6rem;}
        	.code-box{background: url(<%=basePath%>/image/code-bg2.png) no-repeat;background-size:cover;width: 7rem;height: 7rem;overflow: hidden;}
        </style>
     <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
     <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />
     <link rel="stylesheet" type="text/css" href="<%=basePath%>/css/reset.css"/>
	<link type="text/css" rel="stylesheet" href="<%=basePath%>/css/common.css?v=1.0.2">
	<script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
	<script type="text/javascript" src="http://malsup.github.io/jquery.form.js"></script>
</head>


<!--基本信息-->
    	<div class="center-float-center mt60">
    		<div class="info-box center-float-center">
	    		<div class="info center-float-left box-sizing pl20 pr20">
	    			<div class="left-float-top pt25">
	    				<div class="head center-float-center"><img width="100%" src="${sessionScope.img}" /></div>
	    				<div class="ml15 color-fff" style="width: 5rem;">
	    					<p><span>${sessionScope.name}</span><span class="ml20">ID:${sessionScope.userID}</span></p>
	    					<div class="mt5">
	    						<!-- <input class="color-fff" type="file" value="选择文件" /> -->
	    							<form id="uploatForm"  ><%-- action="<%=path%>/noauth/uploadImagetest" method="post" enctype="multipart/form-data"  --%>
										<input id="uploadFile" type="file" accept="image/*"  name="upload" value="选择相片"/> 
										<input  type="hidden" id="userID" value="${sessionScope.userID}">
										<input style="width:76px;height:29px;font-size:100%;margin-top: 7px;" type="image" src="<%=basePath%>/image/sc.png" onclick="sc1()"/>
									</form>
	    					</div>
	    					<%-- <div class="center-float-right mr40"><img class="img150-60" src="<%=basePath%>/image/sc.png" /></div> --%>
	    				</div>
	    			</div>
	    		</div>
	    	</div>
    	</div>
    	<!--我的名片-->
    	<div>
    		<div class="center-float-center mt80">
    			<img src="<%=basePath%>/image/xian-zuo.png" style="width: 35%;"/>
	    		<span class="flex-1 text-center ft42" style="color: #626262;">我的名片</span>
	    		<img src="<%=basePath%>/image/xian-you.png" style="width: 35%;"/>
    		</div>
    		<div class="center-float-center" id="wsc">
    			<div class="code-box mt20 ft42 color-90 center-float-center">
    				未上传
    			</div>
    		</div>
    		<div class="center-float-center" id="sc">
    			<div class="code-box mt20 ft42 color-90">
    				<img style="width:100%;" src="${sessionScope.photo}" />
    			</div>
    		</div>
    	</div>
    	
<input type="hidden" id="img" value="${sessionScope.img}">
<input type="hidden" id="name" value="${sessionScope.name}">
<input type="hidden" id="no" value="${sessionScope.no}">
<input type="hidden" id="myid" value="${sessionScope.myid}">
<input type="hidden" id="photo" value="${sessionScope.photo}">
<script type="text/javascript">
	 

	var photo=document.getElementById('photo').value; 
	if(photo.length>0){
		$('#wsc').hide();
	}else{
		$('#sc').hide();
	}

	  $('#uploatForm').on('click', function(){
		     sc()
		   event.preventDefault() //阻止form表单默认提交 
		}) 
	  

	 
 function sc() {
	 var form = new FormData(document.getElementById("uploatForm"));
		 var dom = document.getElementById("uploadFile");  
		var file=	dom.files[0];
	 var size = Math.floor(file.size/1024); 
	 if (size > 1000) { 
	 alert("检测到上传图片过大,请耐心等候上传！"); 
	 alert("上传中！");
	 };  
    $.ajax({
        type: "POST",                  //提交方式
        dataType: "json",              //预期服务器返回的数据类型
        url: "<%=path%>/noauth/uploadImagetest" ,          //目标url
        data: form, //提交的数据
        processData:false,
        contentType:false,
        success: function (result) {
           // alert(result.code);       //打印服务端返回的数据(调试用)
            if (result.code==200) {
                alert("上传成功");
                window.location.reload()
            }else{
            	alert(result.message);
            	window.location.reload()
            }
            ;
        },
        /*  error : function() {
            alert("异常！");
        }  */
    });
} 
   /*      $(function(){
            //给 “使用ajax上传” 按钮绑定点击事件
            $("#uploadButton").on("click",function(){
                $("#uploatForm").submit(function(e){
                	var userID= document.getElementById('userID');
                	var myid= document.getElementById('myid');
                	  alert("Submitted");
                });
            });
        });  */

    </script>
<script>



 



<%-- 	function uploadImage() {
		
		var userID= document.getElementById('userID');
    	var myid= document.getElementById('myid');
		if(userID!=myid){
			alert("无法操作他人玩家");
			return;
		}
		//获取文件
		var file = $("#imgForm").find("input")[0].files[0];
		//判断类型是不是图片  
		if (!/image\/\w+/.test(file.type)) {
			alert("请确保文件为图像类型");
			return false;
		}
		var reader = new FileReader();
		var AllowImgFileSize = 2100000; //上传图片最大值(单位字节)（ 2 M = 2097152 B ）超过2M上传失败
		var imgUrlBase64;
		if (file) {
			//将文件以Data URL形式读入页面  
			imgUrlBase64 = reader.readAsDataURL(file);
			console.info(reader.result)
			//  reader.onload = function (e) {
			var ImgFileSize = reader.result.substring(reader.result
					.indexOf(",") + 1).length;//截取base64码部分（可选可不选，需要与后台沟通）
			console.info(ImgFileSize)
			if (AllowImgFileSize != 0
					&& AllowImgFileSize < reader.result.length) {
				alert('上传失败，请上传不大于2M的图片！');
				return;
			} else {
				//执行上传操作
				/*    alert(reader.result); */
			}
			// }
		}

		$.ajax({
			type : "POST",
			url : "<%=path%>/noauth/uploadImage",
        data: {
        	file:reader.result
        },
        success: function (result) {},
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
} --%>


/* var totalPage;
var page = 1;
 function uppage()
{
  page = page - 1;
  if(page <= 0){
     page = 1;
  }
  $("#page").html(page+"/"+totalPage);
  loadUserIdInfo();
} */

/* function downpage()
{
  page = page + 1;
  if(page > totalPage){
     page = totalPage;
  }
   $("#page").html(page+"/"+totalPage);
   loadUserIdInfo();
}
    var myOperate=1;//1添加红名2解除红名
    $(document).ready(function () {
    	/* $("#t1").hide();
        $("#t2").hide();
        $("#t0").hide(); */
          var gameId=localStorage.getItem("gameId");
         /*  if ($.trim(gameId)!=""){
              checkTab(gameId);
          } */
        /*   loadUserIdInfo(); */
    });

    function switchGame() {
        clearMsg();
    }

    function clearMsg() {
        $('#playerName').html('');
        $("#myIco").css("display","none");
        $('#operate').css("display","none");
    }
 */
   <%--   function loadUserInfo() {
    	 var userId = $("#userId").val().trim();
            if (userId==""||isNaN(userId)){
               alert("请输入游戏ID");
               return;
           } 
           $.ajax({
               timeout: 60000,
               async: true,
               type: "POST",
               url: "<%=path%>/user/checkimgByuserid",
               data: {
               	userId:$("#userId").val(),
                   gameId:$("#gameId").val(),pageNo:page
               },
               dataType: "json",
               success: function (result) {
//                   JSON.stringify(result);//json转字符串
//                   JSON.parse(jsonStr);//字符串转json
                   if(result.code==1000){$("#table_data").html('<tr style="color: #ee6a2a;font-size: 18px;"><td>游戏ID</td><td>昵称</td><td>查看图片</td><td>操作</td></tr>');
                   $.each(result.data, function (index, tempData) {
              /*   	   var img="<img src='"+tempData.img+"'>"; */
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
                   console.info(status+","+err)
                   var auth_url = req.getResponseHeader("REQUIRES_AUTH_URL");
                   if(auth_url){
                       window.location.href = auth_url;
                   }else{
                       alert("请稍后再试");
                   }
               }
           });
    }  --%>

<%--     function loadUserIdInfo() {
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
                console.info(status+","+err)
                var auth_url = req.getResponseHeader("REQUIRES_AUTH_URL");
                if(auth_url){
                    window.location.href = auth_url;
                }else{
                    alert("请稍后再试");
                }
            }
        });
    } --%>
    
   
    
 

//将图片压缩转成base64 
	function getBase64Image(img) {
		var canvas = document.createElement("canvas");
		var width = img.width;
		var height = img.height;
		// calculate the width and height, constraining the proportions 
		if(width > height) {
			if(width > 100) {
				height = Math.round(height *= 100 / width);
				width = 100;
			}
		} else {
			if(height > 100) {
				width = Math.round(width *= 100 / height);
				height = 100;
			}
		}
		canvas.width = width; /*设置新的图片的宽度*/
		canvas.height = height; /*设置新的图片的长度*/
		/*var ctx = canvas.getContext("2d");
		ctx.drawImage(img, 0, 0, width, height); 绘图*/ 
		var dataURL = canvas.toDataURL("image/png", 0.8);
		return dataURL.replace("data:image/png;base64,", "");
	}


  
</script>
</html>