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
// Multiple file selector by Stickman -- http://www.the-stickman.com 
// with thanks to: [for Safari fixes] Luis Torrefranca -- http://www.law.pitt.edu and Shawn Parker & John Pennypacker -- http://www.fuzzycoconut.com [for duplicate name bug] 'neal'
function MultiSelector(list_target, max) {
	this.list_target = list_target;
	this.count = 0;
	this.id = 0;
	if (max) {
		this.max = max;
	} else {
		this.max = -1;
	}
	;
	this.addElement = function(element) {
		if (element.tagName == 'INPUT' && element.type == 'file') {
			element.name = 'file_' + this.id++;
			element.multi_selector = this;
			element.onchange = function() {
				var new_element = document.createElement('input');
				new_element.type = 'file';
				this.parentNode.insertBefore(new_element, this);
				this.multi_selector.addElement(new_element);
				this.multi_selector.addListRow(this);
				this.style.position = 'absolute';
				this.style.left = '-1000px';
			};
			if (this.max != -1 && this.count >= this.max) {
				element.disabled = true;
			}
			;
			this.count++;
			this.current_element = element;
		} else {
			alert('Error: not a file input element');
		}
		;
	};
	this.addListRow = function(element) {
		var new_row = document.createElement('div');
		var new_row_button = document.createElement('input');
		new_row_button.type = 'button';
		new_row_button.value = 'Delete';
		new_row.element = element;
		new_row_button.onclick = function() {
			this.parentNode.element.parentNode
					.removeChild(this.parentNode.element);
			this.parentNode.parentNode.removeChild(this.parentNode);
			this.parentNode.element.multi_selector.count--;
			this.parentNode.element.multi_selector.current_element.disabled = false;
			return false;
		};
		new_row.innerHTML = element.value;
		new_row.appendChild(new_row_button);
		this.list_target.appendChild(new_row);
	};
};