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
/*------------------------------------------------------------
	Document Text Sizer- Copyright 2003 - Taewook Kang.  All rights reserved.
	Coded by: Taewook Kang (txkang.REMOVETHIS@hotmail.com)
	Web Site: http://txkang.com
	Script featured on Dynamic Drive (http://www.dynamicdrive.com)
	
	Please retain this copyright notice in the script.
	License is granted to user to reuse this code on 
	their own website if, and only if, 
	this entire copyright notice is included.
--------------------------------------------------------------*/

//Specify affected tags. Add or remove from list:
var tgs = new Array( 'div','td','tr');

//Specify spectrum of different font sizes:
var szs = new Array( 'xx-small','x-small','small','medium','large','x-large','xx-large' );
var startSz = 2;

function ts( trgt,inc ) {
	if (!document.getElementById) return
	var d = document,cEl = null,sz = startSz,i,j,cTags;
	
	sz += inc;
	if ( sz < 0 ) sz = 0;
	if ( sz > 6 ) sz = 6;
	startSz = sz;
		
	if ( !( cEl = d.getElementById( trgt ) ) ) cEl = d.getElementsByTagName( trgt )[ 0 ];

	cEl.style.fontSize = szs[ sz ];

	for ( i = 0 ; i < tgs.length ; i++ ) {
		cTags = cEl.getElementsByTagName( tgs[ i ] );
		for ( j = 0 ; j < cTags.length ; j++ ) cTags[ j ].style.fontSize = szs[ sz ];
	}
}