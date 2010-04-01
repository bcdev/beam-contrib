/*
 * $Id: $
 *
 * Copyright (C) 2007 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.decisiontree;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.util.StringUtils;

import com.bc.jexp.ParseException;
import com.bc.jexp.Term;

/**
 * Created by marcoz.
 *
 * @author marcoz
 * @version $Revision: $ $Date: $
 */
class DecisionNode {
	
	String name;
	String expression;
	transient Term term;
	String yes;
	String no;
	transient DecisionNode yesNode;
	transient DecisionNode noNode;
	transient DecisionNode parent;
	String rgb;
	transient Color color;
	transient boolean isLeaf;
	transient boolean[] data;
	byte value;
	
	void initNode(Product sourceProduct) throws OperatorException {
		
		if (StringUtils.isNotNullAndNotEmpty(expression) &&
				StringUtils.isNotNullAndNotEmpty(yes) &&
				StringUtils.isNotNullAndNotEmpty(no)) {
			try {
				term = sourceProduct.createTerm(expression);
			} catch (ParseException e) {
				throw new OperatorException(
						"Couldn't creat term for expression: " + expression, e);
			}
		} else if (StringUtils.isNotNullAndNotEmpty(rgb) && value != 0) {
			Pattern p = Pattern.compile("(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*");
	        Matcher m = p.matcher(rgb);
	        if (m.matches() && m.groupCount() == 3) {
	        	int r = Integer.parseInt(m.group(0));
	        	int g = Integer.parseInt(m.group(1));
	        	int b = Integer.parseInt(m.group(2));
	        	color = new Color(r, g, b);
	        } else {
	        	throw new OperatorException("Wrong format rgb color expression: "+ rgb);
	        }
	        isLeaf = true;
		} else {
			throw new OperatorException("Node is neither leaf nor internal node");
		}
	}
}