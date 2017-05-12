var app = angular.module("twitterApp", []);

app.controller("twCtrl", function($scope) {

    $scope.wsSupported = null;
    
    $scope.currentPicImage = null;
    
    $scope.currentName = null;
    
    $scope.currentMessage = null;
    

    try{
        var webSocket = new WebSocket('ws://' + window.document.location.host + contextPath +'/wsfeed');

        webSocket.onerror = function(event) {
            onError(event.data)
        };

        webSocket.onopen = function(event) {

        };

        webSocket.onerror = function(event){

        }

        webSocket.onclose = function(event){
            window.location.reload();
        }

        webSocket.onmessage = function(event) {
            //arrTweets.unshift(JSON.stringify(event.data));
            if(event.data == "Ping"){
                webSocket.send("Pong");
                $scope.$apply();
            }
            else if(event.data == "facebookError"){
            	$scope.showFacebookLogo = false;
            	$scope.$apply();
            }            
            else{
                webSocket.send("Received post");
                $scope.addFeedPost(event.data);
            }
        };

        $scope.wsSupported = true;

    }catch(e){ //throws code 15 if has socket to me babies
        console.log('Websocket not supported.')
        $scope.wsSupported = false;

    }

    var arrTweets = [];

    $scope.tweets = arrTweets;
    $scope.showFacebookLogo = true;
    
    var arrFeedPosts = [];
    $scope.feedPosts = arrFeedPosts;

    $scope.isWsSupported = function(){

        return $scope.wsSupported;
    }

    $scope.addTweet = function(tw){

        var novoTweet = JSON.parse(tw);

        if(novoTweet instanceof Array){
            $scope.tweets.push.apply($scope.tweets,novoTweet);
        }else{
        	if( typeof novoTweet.media != 'undefined'){
        		$scope.currentPicImage = novoTweet.pictureUrl;
        		$scope.currentName = novoTweet.name;
        		$scope.currentMessage = novoTweet.message;
        		var media = novoTweet.media;
        		$scope.setPicture(media);
        	}
        	
            $scope.tweets.unshift(novoTweet);
        }

        $scope.$apply();
    }
    
    $scope.addFeedPost = function(feedPostsJson){
    	var newFeedPost = JSON.parse(feedPostsJson);
    	
    	if(newFeedPost instanceof Array){
    		$scope.feedPosts.push.apply($scope.feedPosts, newFeedPost);
    	}
    	else{
    		var isTweetPic = newFeedPost.feedType == 'TWITTER' && typeof newFeedPost.media != 'undefined';
    		var isFacebookPic = newFeedPost.feedType == 'FACEBOOK'&& newFeedPost.type == 'photo';
    		
    		if(isTweetPic || isFacebookPic){
    			$scope.currentPicImage = newFeedPost.pictureUrl;
        		$scope.currentName = newFeedPost.name;
        		$scope.currentMessage = newFeedPost.message;
        		$scope.currentType = newFeedPost.feedType;
        		var media = newFeedPost.media;
        		$scope.setPicture(media);
    		}
    		
    		$scope.feedPosts.unshift(newFeedPost);
    		
    		
    	}
    	
    	$scope.$apply();
    }

    $scope.setPicture = function(picture){
    	 $('#imagepreview').attr('src', picture); // here asign the image to the modal when the user click the enlarge link
         $('#imagemodal').modal('show');
         
         setTimeout(function(){
        	 
        	 if($('#imagepreview').attr('src') == picture){
        		 $('#imagemodal').modal('hide');
        	 }
        	 
         }, 5000);
    }
    
    
    $scope.getTime  = function  (milliseconds) {

        milliseconds = new Date().getTime() - milliseconds;

        var temp = Math.floor(milliseconds / 1000);
        var years = Math.floor(temp / 31536000);

        if(years < 0){
            return "Now";
        }

        if (years) {
            return years + ' y';
        }
        //TODO: Months! Maybe weeks?
        var days = Math.floor((temp %= 31536000) / 86400);
        if (days) {
            return days + ' d' ;
        }
        var hours = Math.floor((temp %= 86400) / 3600);
        if (hours) {
            return hours + ' h' ;
        }
        var minutes = Math.floor((temp %= 3600) / 60);
        if (minutes) {
            return minutes + 'm' ;
        }
        var seconds = temp % 60;
        if (seconds) {
            return "Now";
        }
        return 'Now'; //'just now' //or other string you like;
    }
    
    $scope.isMobile = function() {
    		
	    	if( /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) ) {
	    		return true;
	    	}else{
	    		return false;
	    	}
    	}
    
    
});

