/*
 * $Id: AsciiProductReaderTest.java,v 1.1 2007/04/16 13:24:26 marcop Exp $
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

import com.bc.ceres.core.ProgressMonitor;
import junit.framework.TestCase;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.*;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class AsciiProductReaderTest extends TestCase {

    public static final String PRODUCT_NAME = "TestProduct";
    public static final String PRODUCT_TYPE = "ProdType";
    public static final int WIDTH = 5;
    public static final int HEIGHT = 4;
    public static final String START_TIME = "17-APR-2581 00:07:36.000232";
    public static final String END_TIME = "17-APR-2581 00:09:16.000210";
    public static final int NUM_BANDS = 4;

    public void testReadProductNodes() throws IOException {
        final AsciiProductReaderPlugIn plugIn = new AsciiProductReaderPlugIn();
        final ProductReader reader = plugIn.createReaderInstance();
        final String inpStr = prepareInputString();
        final ByteArrayInputStream baStream = new ByteArrayInputStream(inpStr.getBytes());
        final ImageInputStream stream = new MemoryCacheImageInputStream(baStream);
        final Product product = reader.readProductNodes(stream, null);
        assertNotNull(product);
        assertEquals(PRODUCT_NAME, product.getName());
        assertEquals(PRODUCT_TYPE, product.getProductType());
        assertEquals(WIDTH, product.getSceneRasterWidth());
        assertEquals(HEIGHT, product.getSceneRasterHeight());

        final ProductData.UTC startTime = product.getStartTime();
        assertNotNull(startTime);
        assertEquals(START_TIME, startTime.toString());
        final ProductData.UTC endTime = product.getEndTime();
        assertNotNull(endTime);
        assertEquals(END_TIME, endTime.toString());
        assertEquals(NUM_BANDS, product.getNumBands());

        final Band band1 = product.getBand("Band_1");
        assertNotNull(band1);
        assertEquals("Band_1_Unit", band1.getUnit());
        assertEquals("Band_1_Description", band1.getDescription());
        assertEquals("float64", ProductData.getTypeString(band1.getDataType()));

        final Band band2 = product.getBand("Band_2");
        assertNotNull(band2);
        assertEquals("Band_2_Unit", band2.getUnit());
        assertEquals("Band_2_Description", band2.getDescription());
        assertEquals("float64", ProductData.getTypeString(band2.getDataType()));

        final Band latBand = product.getBand("LatBand");
        assertNotNull(latBand);
        assertEquals("LatBand_Unit", latBand.getUnit());
        assertEquals("LatBand_Description", latBand.getDescription());
        assertEquals("float64", ProductData.getTypeString(latBand.getDataType()));

        final Band lonBand = product.getBand("LonBand");
        assertNotNull(lonBand);
        assertEquals("LonBand_Unit", lonBand.getUnit());
        assertEquals("LonBand_Description", lonBand.getDescription());
        assertEquals("float64", ProductData.getTypeString(lonBand.getDataType()));

        assertEquals(false, band1.hasRasterData());
        assertEquals(false, band2.hasRasterData());
        assertEquals(false, latBand.hasRasterData());
        assertEquals(false, lonBand.hasRasterData());

        final GeoCoding geoCoding = product.getGeoCoding();
        assertNotNull(geoCoding);
        assertEquals(PixelGeoCoding.class, geoCoding.getClass());
        final PixelGeoCoding pixelGeoCoding = (PixelGeoCoding) geoCoding;
        assertSame(latBand, pixelGeoCoding.getLatBand());
        assertSame(lonBand, pixelGeoCoding.getLonBand());
    }

    public void testReadBandRasterData() throws IOException {
        final AsciiProductReaderPlugIn plugIn = new AsciiProductReaderPlugIn();
        final ProductReader reader = plugIn.createReaderInstance();
        final String inpStr = prepareInputString();
        final ByteArrayInputStream baStream = new ByteArrayInputStream(inpStr.getBytes());
        final ImageInputStream stream = new MemoryCacheImageInputStream(baStream);

        final Product product = reader.readProductNodes(stream, null);

        assertNotNull(product);
        final Band band = product.getBandAt(0);
        assertFalse(band.hasRasterData());

        final double[] elems = new double[4];

        band.readRasterData(1, 1, 2, 2, ProductData.createInstance(elems), ProgressMonitor.NULL);
        assertFalse(band.hasRasterData());
        assertEquals("11.0", String.valueOf(elems[0]));
        assertEquals("12.0", String.valueOf(elems[1]));
        assertEquals("16.0", String.valueOf(elems[2]));
        assertEquals("17.0", String.valueOf(elems[3]));

        band.readRasterDataFully(ProgressMonitor.NULL);
        assertTrue(band.hasRasterData());
        final ProductData bandData = band.getRasterData();
        assertEquals("5.0", bandData.getElemStringAt(0));
        assertEquals("6.0", bandData.getElemStringAt(1));
        assertEquals("7.0", bandData.getElemStringAt(2));
        assertEquals("8.0", bandData.getElemStringAt(3));
        assertEquals("9.0", bandData.getElemStringAt(4));
        assertEquals("10.0", bandData.getElemStringAt(5));
        assertEquals("11.0", bandData.getElemStringAt(6));
        assertEquals("12.0", bandData.getElemStringAt(7));
        assertEquals("13.0", bandData.getElemStringAt(8));
        assertEquals("14.0", bandData.getElemStringAt(9));
        assertEquals("15.0", bandData.getElemStringAt(10));
        assertEquals("16.0", bandData.getElemStringAt(11));
        assertEquals("17.0", bandData.getElemStringAt(12));
        assertEquals("18.0", bandData.getElemStringAt(13));
        assertEquals("19.0", bandData.getElemStringAt(14));
        assertEquals("20.0", bandData.getElemStringAt(15));
        assertEquals("21.0", bandData.getElemStringAt(16));
        assertEquals("22.0", bandData.getElemStringAt(17));
        assertEquals("23.0", bandData.getElemStringAt(18));
        assertEquals("24.0", bandData.getElemStringAt(19));
    }

    private String prepareInputString() {
        return AsciiProductFormatConstants.FIRST_LINE + "001\n" +
                "\n" +
                AsciiProductFormatConstants.KEY_PRODUCT_HEADER + "\n" +
                "****************\n" +
                "\n" +
                AsciiProductFormatConstants.KEY_PRODUCT_NAME + "\t" + PRODUCT_NAME + "\n" +
                AsciiProductFormatConstants.KEY_PRODUCT_TYPE + "\t" + PRODUCT_TYPE + "\n" +
                AsciiProductFormatConstants.KEY_WIDTH + "\t" + WIDTH + "\n" +
                AsciiProductFormatConstants.KEY_HEIGHT + "\t" + HEIGHT + "\n" +
                AsciiProductFormatConstants.KEY_START_TIME + "\t" + START_TIME + "\n" +
                AsciiProductFormatConstants.KEY_END_TIME + "\t" + END_TIME + "\n" +
                AsciiProductFormatConstants.KEY_NUM_BANDS + "\t" + NUM_BANDS + "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                AsciiProductFormatConstants.KEY_RASTER_DATA + "\n" +
                "************\n" +
                "\n" +
                "Band_1\t  Band_1_Unit\t  Band_1_Description\n" +
                "5.0,6.0,7.0,8.0,9.0\n" +
                "10.0,11.0,12.0,13.0,14.0\n" +
                "15.0,16.0,17.0,18.0,19.0\n" +
                "20.0,21.0,22.0,23.0,24.0\n" +
                "\n" +
                "Band_2\t  Band_2_Unit\t  Band_2_Description\n" +
                "15.0,16.0,17.0,18.0,19.0\n" +
                "20.0,21.0,22.0,23.0,24.0\n" +
                "25.0,26.0,27.0,28.0,29.0\n" +
                "30.0,31.0,32.0,33.0,34.0\n" +
                "\n" +
                "LatBand\t  LatBand_Unit\t  LatBand_Description\n" +
                "42.0,43.0,44.0,45.0,46.0\n" +
                "47.0,48.0,49.0,50.0,51.0\n" +
                "52.0,53.0,54.0,55.0,56.0\n" +
                "57.0,58.0,59.0,60.0,61.0\n" +
                "\n" +
                "LonBand\t  LonBand_Unit\t  LonBand_Description\n" +
                "84.0,85.0,86.0,87.0,88.0\n" +
                "89.0,90.0,91.0,92.0,93.0\n" +
                "94.0,95.0,96.0,97.0,98.0\n" +
                "99.0,100.0,101.0,102.0,103.0\n" +
                "\n" +
                "\n" +
                AsciiProductFormatConstants.KEY_GEOCODING + "\n" +
                "***********\n" +
                "\n" +
                AsciiProductFormatConstants.KEY_LATITUDE_RASTER_NAME + "\t  LatBand\n" +
                AsciiProductFormatConstants.KEY_LONGITUDE_RASTER_NAME + "\t  LonBand\n" +
                "\n" +
                "\n" +
                AsciiProductFormatConstants.KEY_METADATA + "\n" +
                "**********\n" +
                "\n" +
                AsciiProductFormatConstants.KEY_METADATA_ROOT + "\n" +
                "ME ++   ebene 1_1\n" +
                "MA --     att1\t  utc\t  13-JAN-2000 00:00:23.000034\t  \t  \n" +
                "MA --     att2\t  ascii\t  Text\t  \t  any Text description\n" +
                "MA --     att3\t  float32\t  12.4,13.5,0.21\t  meter\t  \n" +
                "ME ++     ebene 2\n" +
                "MA --       att4\t  utc\t  13-JAN-2000 00:00:23.000034\t  \t  UTC Description\n" +
                "MA --       att5\t  ascii\t  Text\t  \t  \n" +
                "MA --       att6\t  float32\t  12.4,13.5,0.21\t  km\t  \n" +
                "ME ++   ebene 1_2\n" +
                "MA --     att7\t  utc\t  13-JAN-2000 00:00:23.000034\t  \t  \n" +
                "MA --     att8\t  ascii\t  Text\t  \t  \n" +
                "MA --     att9\t  float32\t  12.4,13.5,0.21\t  kg\t  Description\n";
    }
}
