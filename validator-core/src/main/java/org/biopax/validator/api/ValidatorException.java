package org.biopax.validator.api;

/*
 * #%L
 * Object Model Validator Core
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

/**
 * A validation runtime exception.
 *
 * @author rodche
 */
public class ValidatorException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	private final Object element;
	private final Object[] msgArgs;

    public ValidatorException(String msg, Object... args) {
    	super(msg);
    	element = null;
        this.msgArgs = args;
    }
    
    public ValidatorException(Throwable t, Object... args) {
        super(t);
        element = null;
        this.msgArgs = args;
    }
    
    public Object[] getMsgArgs() {
		return msgArgs;
	}
	
    public Object getElement() {
		return element;
	}
    
    @Override
    public String toString() {
    	return super.toString() + msgArgs.toString();
    }
    
}