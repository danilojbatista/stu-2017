<%@page import="com.ibm.br.ltc.rte.bean.FacebookSingleton"%>
<%@page import="com.ibm.br.ltc.rte.bean.TwitterSingleton"%>
<html>
<head>
	<meta charset="UTF-8">
	<title>Social Posts <%out.println(TwitterSingleton.getHashTags());%></title>
	<meta charset="utf-8">
	<meta name="viewport" content="width-device-width, initial-scale=1">
	<link rel="stylesheet" href="../css/bootstrap.min.css">
	<link rel="stylesheet" href="../css/bootstrap-theme.min.css">
	<script src="../js/jquery.min.js"></script>
	<script src="../js/bootstrap.min.js"></script>
	<link rel="stylesheet" href="../css/twitterapp.css" />
  

</head>
<body>
<script src="../js/angular.min.js"></script>
<script src="../js/twitterapp.js"></script>

<div ng-app="twitterApp" ng-controller="twCtrl">
    <nav  class="navbar navbar-default" style="height: 94px;" >
		<div class="container-fluid">
			<div class="navbar-header" >			
			</div>	
			<div ng-if="!isMobile()">		
				<a class="brand" style="margin: 0; float: none; text-align:center"  > <img src="../img/IBMSTU.png" width="400px" height="118px" class="img-responsive"  />	</a>
			</div>
			<div ng-if="isMobile()">
				<img  src="../img/IBMSTU.png" width="400px" height="118px" class="img-responsive"  />		
			</div>
		</div>
	</nav> 

	<div class="container-fluid" style="margin-top: -11px;">

		<!-- Check if the websocket is supported -->
		<div ng-if="!isWsSupported()"  id="sktNotSupp" class="alert alert-danger" role="alert">
			<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
			WebSocket is not supported!
		</div>

		<ul class="list-group" >

			<!--Creates header -->
			<li class="list-group-item" style="padding-bottom:5px; padding-top:5px;" > 
			
				<div id="appHeader">
						<div id="twitterTag">
							<img style="margin-top: 5px;"
								src="../img/twb.png" width="40px" height="40px;">
							<h3><%out.println(TwitterSingleton.getHashTags());%></h3>
						</div>
						<div id="facebookTag" ng-if="showFacebookLogo">
							<div>
								<img style="margin-top: 5px;" src="../img/fbb.png" width="40px"
									height="40px;">
								<h3><%out.println(FacebookSingleton.getFacebookPageName()); %></h3>
							</div>

						</div>
					</div>
			</li>

			<!--List of tweets -->
			<div  style="overflow-y: scroll; overflow-x: hidden ;max-height: 560px;">
				<li class="list-group-item" style="padding: 5px;" ng-repeat="feedPost in feedPosts" >
					<div class="row" ng-if="feedPost.feedType == 'TWITTER'">
						<div class="col-md-12" >
								<span class="tweetDate"> {{getTime(feedPost.date)}}</span>
								<img width="61px" height="61px;" ng-src="{{feedPost.pictureUrl}}" class="img-rounded tweetImg"/>
								<div class="content-heading" >
									<h4 class="tweetCompleteName" >&nbsp{{feedPost.completeName}}</h4> 
									<img src="../img/twitter_logo.jpg" style="width: 30px; float: right;"/>
								
								</div>
								<span class="tweetName">@{{feedPost.name}}  </span>

								<br/>
								<br/>

							<span class="tweetMessage" >{{feedPost.message}}</span>

						</div>
					</div>
					
					<div class="row" ng-if="feedPost.feedType == 'FACEBOOK'">
						<div class="col-md-12" >
								<span class="tweetDate"> {{getTime(feedPost.date)}}</span>
								<img width="61px" height="61px;" ng-src="{{feedPost.pictureUrl}}" class="img-rounded tweetImg"/>
								<div class="content-heading" ><h4 class="tweetCompleteName" >&nbsp{{feedPost.name}}</h4> </div>
								<img src="../img/fbb.png" style="height: 30px; float: right;"/>
								<br/>
								<br/>

							<span class="tweetMessage" >{{feedPost.message}}</span>

						</div>
					</div>

				</li>
				<li class="list-group-item" ><div style="height: 300px; border-color: white;"></div> </li>

			</div>
		</ul>
	</div>
	
	<!-- Creates the bootstrap modal where the image will appear -->
	<div class="modal fade" id="imagemodal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	  <div class="modal-dialog">
	      <div >
	        <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
	        <img width="61px" height="61px;" ng-src="{{currentPicImage}}" class="img-rounded tweetImg"/>
	         <h4 class="tweetCompleteName" style="color: white;" ng-if="currentType == 'TWITTER'">&nbsp @{{currentName}}</h4>
	         <h4 class="tweetCompleteName" style="color: white;" ng-if="currentType == 'FACEBOOK'"> {{currentName}}</h4>
	        <h4 style="margin-left:20px;color: white;" >{{currentMessage}}</h4>
	      </div>
	      <div class="modal-body">
	        <img src="" id="imagepreview"  >
	      </div>
	  </div>
	</div>

	<nav class="navbar navbar-default navbar-fixed-bottom" >
		<div class="container-fluid">
			<div class="navbar-header" style="float: right;">
				<img src="../img/ibmtm.png"  width="63.36px" height="43.56" />
			</div>
		</div>
	</nav>


</div>

<script type="text/javascript">

var contextPath='<%=request.getContextPath()%>';

console.log(contextPath);


</script>

</body>
</html>
