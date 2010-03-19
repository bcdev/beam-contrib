/*
 * $Id: AsciiProductReaderPluginTest.java,v 1.1 2007/04/16 13:24:26 marcop Exp $
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
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.dataio.DecodeQualification;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;

public class AsciiProductReaderPluginTest extends TestCase {

    private AsciiProductReaderPlugIn _plugin;

    @Override
    protected void setUp() throws Exception {
        _plugin = new AsciiProductReaderPlugIn();
    }

    public void testDefaultFileExtensions() {
        final String[] defaultFileExtensions = _plugin.getDefaultFileExtensions();
        assertNotNull(defaultFileExtensions);
        assertEquals(1, defaultFileExtensions.length);
        assertEquals(".txt", defaultFileExtensions[0]);
    }

    public void testDescription() {
        assertEquals("BEAM Ascii product reader", _plugin.getDescription(null));
    }

    public void testFormatNames() {
        final String[] formatNames = _plugin.getFormatNames();
        assertNotNull(formatNames);
        assertEquals(1, formatNames.length);
        assertEquals("BEAM-ASCII", formatNames[0]);
    }

    public void testOutputTypes() {
        final Class[] inputTypes = _plugin.getInputTypes();
        assertNotNull(inputTypes);
        assertEquals(3, inputTypes.length);
        assertEquals(String.class, inputTypes[0]);
        assertEquals(File.class, inputTypes[1]);
        assertEquals(ImageInputStream.class, inputTypes[2]);
    }

    public void testCanDecodeInput() {
        final String lines = AsciiProductFormatConstants.FIRST_LINE + "dklfj\n234j\nsdf\tsdf\tsdaf\n";
        final ByteArrayInputStream bais = new ByteArrayInputStream(lines.getBytes());
        final ImageInputStream stReader = new MemoryCacheImageInputStream(bais);
        assertEquals(DecodeQualification.INTENDED, _plugin.getDecodeQualification(stReader));
    }

    public void testCreateReaderInstance() {
        final ProductReader reader = _plugin.createReaderInstance();
        assertNotNull(reader);
        assertSame(AsciiProductReader.class, reader.getClass());
    }
}
