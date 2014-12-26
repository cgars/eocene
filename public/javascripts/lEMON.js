/**
 * lEMON - Earthdawn Map ONline
 */

var map;
var brooklyn = new google.maps.LatLng(48.036195, 7.847339);


function initialize() {
  var styles = [ 
                      { "stylers": [ { "visibility": "off" } ] },
                      { "featureType": "landscape.natural", 
                    	  "stylers": [ { "visibility": "on" } ] },
                      { "featureType": "water", 
                    		  "stylers": [ { "visibility": "on" } ] },
                      { "featureType": "water", 
                    		   "stylers": [ { "visibility": "on" } ] 
                    		  }, 
                      {"featureType":"water",
                    			  "elementType":"labels",
                    			  	"stylers":[{"visibility":"off"}]
                      },
                      {"featureType":"landscape.natural",
            			  "elementType":"labels",
            			  	"stylers":[{"visibility":"off"}]
              }
                 ]

  var mapOptions = {
    zoom: 5,
    center: brooklyn,
    mapTypeControlOptions: {
      mapTypeIds: [google.maps.MapTypeId.TERRAIN]
    },
    mapTypeId: google.maps.MapTypeId.TERRAIN
  };

  map = new google.maps.Map(document.getElementById('map-canvas'),
      mapOptions);


  map.setOptions({styles: styles});

  $.getJSON("/assets/json/data.json",addPlaces);
  
  function addPlaces(geojson){
	  var features = map.data.addGeoJson(geojson);
	  features.forEach(function(element, index, array) {
		  map.data.overrideStyle(element,{icon:
		  		{
			    path: google.maps.SymbolPath.CIRCLE,
			    scale: element.k.size+1
			  },
			  title:element.k.title});
	  	})
  }
  
  var drawingManager = new google.maps.drawing.DrawingManager({
	    drawingMode: google.maps.drawing.OverlayType.MARKER,
	    drawingControl: true,
	    drawingControlOptions: {
	      position: google.maps.ControlPosition.TOP_CENTER,
	      drawingModes: [
	                     google.maps.drawing.OverlayType.POLYLINE,
	                     ]
	    				},
	    editable:false,
	    drawingMode:null
	  });
  
  drawingManager.setMap(map);
  
  google.maps.event.addListener(drawingManager, 'overlaycomplete', function(event) {
	  var distance = google.maps.geometry.spherical.computeLength(event.overlay.getPath())
	  var position = new google.maps.LatLng(50, 14)
	  var infowindow = new google.maps.InfoWindow({
		    content: "Distance is:" + distance/1000,
		    position: event.overlay.getPath().getAt(
		    		event.overlay.getPath().getLength()-1)
		});
	  infowindow.open(map);
	  event.overlay.setMap(null);
	});
}

google.maps.event.addDomListener(window, 'load', initialize);
