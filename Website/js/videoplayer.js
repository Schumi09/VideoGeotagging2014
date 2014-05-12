var video = document.getElementById("video");
/*Changing Video*/

function changeVideo(vsource, newsrc, position){
	var video = document.getElementById("video");
	if (vsource != newsrc){
				
				video.pause();
				video.src = newsrc;
				video.load();
				video.play();
				initVideo(position);
				var w_height = $( window ).height();
				if (w_height <= 700){
				resizePlayer();
				}

				document.getElementById('name').innerHTML = name;	
					
			}
				else{
					
					video.currentTime = position;
					video.play();
					}	
} 

/*function for waiting for loaded metadata*/
function initVideo(position){
	var video = document.getElementById("video");
	video.addEventListener('loadedmetadata', function() {
		video.currentTime = position;
		
		});
	}

/*Simple function to adjust player size for smaller screens*/

function resizePlayer(){
	
	var vheight = video.videoHeight;
	var vwidth  = video.videoWidth;
	var ratio = vwidth/vheight;
	var defaultheight = 350;
	video.height = defaultheight;
	video.width = defaultheight*ratio;

}