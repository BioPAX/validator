
var opts = document.forms['validate'].elements['switch'];
for(i=0; i < opts.length; i++) {
  opts[i].onclick = switchInput;
}
document.getElementById('autofix').onclick = updateValidatorOptions;

//onclick event handler in the 'switch' radio buttons on check.jsp
function switchInput() {
  var f = document.getElementById('file');
  var u = document.getElementById('url');
  if (this.value == "file") {
    f.disabled = false;
    u.disabled = true;
    u.value = null;
    document.getElementById('urlMsg').innerHTML = null;
  } else if (this.value == "url") {
    f.disabled = true;
    f.value = null;
    u.disabled = false;
  } else { //just in case...
    console.error("unsupported value");
  }
}

function isUrl(s) {
		var regexp = /(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
		return regexp.test(s);
}

function validate() {
	  var u = document.getElementById('url');
	  if(!u.disabled && !isUrl(u.value)) {
		  document.getElementById('urlMsg').innerHTML = 'Malformed URL!';
		  return false;
	  }
}

function updateValidatorOptions() {
		var style = document.getElementById("normalizerOptions").style;
		var cb = document.getElementById("autofix");
		var retOwl = document.getElementById("retOwl");
		if (cb.checked == false) {
			style.display = "none";
			retOwl.checked = false;
			retOwl.disabled = true;
			document.getElementById("retHtml").checked = true;
		} else {
			style.display = "block"; 
			retOwl.disabled = false;
		}
}
