/*
 * $Id: AsciiProductWriterPluginTest.java,v 1.1 2007/04/16 13:24:26 marcop Exp $
 *
 * Copyright (C) 2002 by Brockmann Consult (info@brockmann-consult.de)
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
package org.esa.beam.dataio.ascii;

import junit.framework.TestCase;
import org.esa.beam.framework.dataio.ProductWriter;

import java.io.File;
import java.io.Writer;

public class AsciiProductWriterPluginTest extends TestCase {

    private AsciiProductWriterPlugIn _plugin;

    @Override
    protected void setUp() throws Exception {
        _plugin = new AsciiProductWriterPlugIn();
    }

    public void testDefaultFileExtensions() {
        final String[] defaultFileExtensions = _plugin.getDefaultFileExtensions();
        assertNotNull(defaultFileExtensions);
        assertEquals(1, defaultFileExtensions.length);
        assertEquals(".txt", defaultFileExtensions[0]);
    }

    public void testDescription() {
        assertEquals("BEAM Ascii product writer", _plugin.getDescription(null));
    }

    public void testFormatNames() {
        final String[] formatNames = _plugin.getFormatNames();
        assertNotNull(formatNames);
        assertEquals(1, formatNames.length);
        assertEquals("BEAM-ASCII", formatNames[0]);
    }

    public void testOutputTypes() {
        final Class[] outputTypes = _plugin.getOutputTypes();
        assertNotNull(outputTypes);
        assertEquals(3, outputTypes.length);
        assertEquals(String.class, outputTypes[0]);
        assertEquals(File.class, outputTypes[1]);
        assertEquals(Writer.class, outputTypes[2]);
    }

    public void testCreateWriterInstance() {
        final ProductWriter writer = _plugin.createWriterInstance();
        assertNotNull(writer);
        assertSame(AsciiProductWriter.class, writer.getClass());
    }
}
