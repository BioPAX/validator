function externalLinks() {
 if (!document.getElementsByTagName) return;
 var anchors = document.getElementsByTagName("a");
 for (var i=0; i<anchors.length; i++) {
   var anchor = anchors[i];
   if (anchor.getAttribute("href") &&
       anchor.getAttribute("rel") == "external")
     anchor.target = "_blank";
 }
}

window.onload = function init() { externalLinks; switchNormalizerOptions()};

function switchInput() {
	  var f = document.getElementById('file');
	  var u = document.getElementById('url');
	  var cb = document.getElementById('switch');
	  if(cb.checked == true) {
		f.disabled = false;
		u.disabled = true;
		u.value = null;
		document.getElementById('urlMsg').innerHTML = null;
	  } else {
		f.disabled = true;
		f.value = null;
		u.disabled = false;
	  }
};

function isUrl(s) {
		var regexp = /(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
		return regexp.test(s);
};

function validate() {
	  var u = document.getElementById('url');
	  if(!u.disabled && !isUrl(u.value)) {
		  document.getElementById('urlMsg').innerHTML = 'Malformed URL!';
		  return false;
	  }
};

function switchNormalizerOptions() {
		var style = document.getElementById("normalizerOptions").style;
		var cb = document.getElementById("autofix");
		if (cb.checked == false) { //changed to 'checked'
			style.display = "none";
		} else {
			style.display = "block";
		}
};

function switchit(list) {
	var listElementStyle = document.getElementById(list).style;
	if (listElementStyle.display == "none") {
		listElementStyle.display = "block";
	} else {
		listElementStyle.display = "none";
	}
};