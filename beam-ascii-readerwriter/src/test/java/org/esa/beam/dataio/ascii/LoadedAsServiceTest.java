/*
 * $Id: LoadedAsServiceTest.java,v 1.1 2007/04/16 13:24:26 marcop Exp $
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
import org.esa.beam.framework.dataio.ProductIOPlugInManager;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.framework.dataio.ProductWriterPlugIn;

import java.util.Iterator;

/**
 * Created by Marco Peters.
 *
 * @author Marco Peters
 * @version $Revision: 1.1 $ $Date: 2007/04/16 13:24:26 $
 */
public class LoadedAsServiceTest extends TestCase {

    public void testReaderIsLoaded() {

        ProductIOPlugInManager plugInManager = ProductIOPlugInManager.getInstance();
        Iterator readerPlugIns = plugInManager.getReaderPlugIns(AsciiProductFormatConstants.FORMAT_NAME);

        if (readerPlugIns.hasNext()) {
            ProductReaderPlugIn plugIn = (ProductReaderPlugIn) readerPlugIns.next();
            assertEquals(AsciiProductReaderPlugIn.class, plugIn.getClass());
        }
    }

    public void testWriterIsLoaded() {

        ProductIOPlugInManager plugInManager = ProductIOPlugInManager.getInstance();
        Iterator writerPlugIns = plugInManager.getWriterPlugIns(AsciiProductFormatConstants.FORMAT_NAME);

        if (writerPlugIns.hasNext()) {
            ProductWriterPlugIn plugIn = (ProductWriterPlugIn) writerPlugIns.next();
            assertEquals(AsciiProductWriterPlugIn.class, plugIn.getClass());
        }
    }
}
