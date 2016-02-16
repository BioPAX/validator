/*
 * #%L
 * BioPAX Validator Web Application
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
};
