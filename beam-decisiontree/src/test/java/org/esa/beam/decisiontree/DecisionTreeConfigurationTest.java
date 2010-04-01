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

import junit.framework.TestCase;

import java.awt.Color;
import java.io.StringReader;

/**
 * Created by marcoz.
 *
 * @author marcoz
 * @version $Revision: $ $Date: $
 */
public class DecisionTreeConfigurationTest extends TestCase {

	/**
	 * Test method for {@link org.esa.beam.decisiontree.DecisionTreeConfiguration#fromXML(java.io.Reader)}.
	 * @throws Exception 
	 */
	public void testConstructorFromXML() throws Exception {
		String xml = "<decisionTree name=\"WAQSomat\">" +
						"<classes>"+ 
						"<class name=\"WOLKE\" value=\"1\" color=\"WHITE\"/>" +
						"<class name=\"SAND\" value=\"2\" color=\"YELLOW\"/>" +
						"</classes>"+
						"<decision name=\"d1\" term=\"true\">" +
						"<yes>WOLKE</yes>" +
						"<no><decision name=\"d2\" term=\"true\"><yes>WOLKE</yes><no>SAND</no></decision></no>" +
						"</decision>" +
						"</decisionTree>";
						
		StringReader reader = new StringReader(xml);
		
		DecisionTreeConfiguration configuration = DecisionTreeConfiguration.fromXML(reader);
		assertNotNull(configuration);
		assertEquals("WAQSomat", configuration.getName());
		
		Classification[] classes = configuration.getClasses();
		assertEquals(2, classes.length);
		
		Classification cloudClass = classes[0];
		assertNotNull(cloudClass);
		
		assertSame(cloudClass, configuration.getClass("WOLKE"));
		assertEquals("WOLKE", cloudClass.getName());
		assertEquals(1, cloudClass.getValue());
		assertEquals(Color.WHITE, cloudClass.getColor());
		
		Decision rootDecisions = configuration.getRootDecisions();
		assertNotNull(rootDecisions);
		assertEquals("d1", rootDecisions.getName());
		assertEquals("WOLKE", rootDecisions.getYesClass().getName());
		assertNull(rootDecisions.getNoClass());
		assertNull(rootDecisions.getYesDecision());
		
		Decision noDecision = rootDecisions.getNoDecision();
        assertNotNull(noDecision);
        assertEquals("d2", noDecision.getName());
        assertEquals("WOLKE", noDecision.getYesClass().getName());
        assertEquals("SAND", noDecision.getNoClass().getName());
        assertNull(noDecision.getYesDecision());
        assertNull(noDecision.getNoDecision());
	}
	
	public void testparseColor_numerical() throws Exception {
		Color color123 = DecisionTreeDomConverter.parseColor("1,2,3");
		assertEquals(new Color(1,2,3), color123);
		
		Color color224466 = DecisionTreeDomConverter.parseColor("22,44,66");
		assertEquals(new Color(22,44,66), color224466);
	}
	
	public void testparseColor_badNumerical() throws Exception {
		try {
		    DecisionTreeDomConverter.parseColor("1,2");
			fail("IllegalArgumentException expected");
		}catch (IllegalArgumentException e) {
		}
		
		try {
		    DecisionTreeDomConverter.parseColor("2828");
			fail("IllegalArgumentException expected");
		}catch (IllegalArgumentException e) {
		}
	}
	
	public void testparseColor_names() throws Exception {
		Color smallWhite = DecisionTreeDomConverter.parseColor("white");
		assertEquals(Color.white, smallWhite);
		
		Color bigWhite = DecisionTreeDomConverter.parseColor("WHITE");
		assertEquals(Color.white, bigWhite);
	}

}
