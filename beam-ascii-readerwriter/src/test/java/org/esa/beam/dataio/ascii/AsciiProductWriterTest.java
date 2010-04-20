/*
 * $Id: AsciiProductWriterTest.java,v 1.1 2007/04/16 13:24:26 marcop Exp $
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
import org.esa.beam.framework.dataio.ProductWriter;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.dataop.maptransf.Datum;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.io.IOException;
import java.io.StringWriter;
import java.awt.geom.AffineTransform;

public class AsciiProductWriterTest extends TestCase {

    public static final String LATITUDE_NAME = "latitude";
    public static final String LONGITUDE_NAME = "longitude";
    public static final String LAT_BAND_NAME = "LatBand";
    public static final String LON_BAND_NAME = "LonBand";
    public static final int TiePoint_GC = 1;
    public static final int PIXEL_GC = 2;
    public static final int MAP_GC = 3;

    private Product _product;
    private ProductWriter _writer;

    @Override
    protected void setUp() throws Exception {
        initProduct();
        final AsciiProductWriterPlugIn writPlugin = new AsciiProductWriterPlugIn();
        _writer = writPlugin.createWriterInstance();
    }

    public void testWriteProduct_TiePointGeocoding() throws IOException {
        addGeocoding(TiePoint_GC);

        final StringWriter output = new StringWriter();
        _writer.writeProductNodes(_product, output);
        _writer.close();

        assertEquals(getExpectedOutput(TiePoint_GC), output.toString());
    }

    public void testWriteProduct_PixelGeocoding() throws IOException {
        addGeocoding(PIXEL_GC);

        final StringWriter output = new StringWriter();
        _writer.writeProductNodes(_product, output);
        _writer.close();

        assertEquals(getExpectedOutput(PIXEL_GC), output.toString());
    }

    public void testWriteProduct_MapGeocoding() throws IOException {
        addGeocoding(MAP_GC);

        final StringWriter output = new StringWriter();
        _writer.writeProductNodes(_product, output);
        _writer.close();

        assertEquals(getExpectedOutput(MAP_GC), output.toString());
    }

    private String getExpectedOutput(int geocodingType) {
        final String numBands;
        if (geocodingType == TiePoint_GC
                || geocodingType == PIXEL_GC) {
            numBands = "NumBands\t5\n";
        } else {
            numBands = "NumBands\t7\n";
        }

        final String firstPart = "BEAM_ASCII_PRODUCT_FORMAT_V001\n" +
                "\n" +
                " Product Header\n" +
                "****************\n" +
                "\n" +
                "ProductName\tProdName\n" +
                "ProductType\tProdType\n" +
                "Width\t5\n" +
                "Height\t5\n" +
                "StartTime\t17-APR-2581 00:07:36.000232\n" +
                "EndTime\t17-APR-2581 00:09:16.000210\n" +
                numBands +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                " RasterData\n" +
                "************\n" +
                "\n" +
                "Band_1\t  Band_1_Unit\t  Band_1_Description\n" +
                "5.0,6.0,7.0,8.0,9.0\n" +
                "10.0,11.0,12.0,13.0,14.0\n" +
                "15.0,16.0,17.0,18.0,19.0\n" +
                "20.0,21.0,22.0,23.0,24.0\n" +
                "25.0,26.0,27.0,28.0,29.0\n" +
                "\n" +
                "LatBand\t  LatBand_Unit\t  LatBand_Description\n" +
                "42.0,43.0,44.0,45.0,46.0\n" +
                "47.0,48.0,49.0,50.0,51.0\n" +
                "52.0,53.0,54.0,55.0,56.0\n" +
                "57.0,58.0,59.0,60.0,61.0\n" +
                "62.0,63.0,64.0,65.0,66.0\n" +
                "\n" +
                "LonBand\t  LonBand_Unit\t  LonBand_Description\n" +
                "84.0,85.0,86.0,87.0,88.0\n" +
                "89.0,90.0,91.0,92.0,93.0\n" +
                "94.0,95.0,96.0,97.0,98.0\n" +
                "99.0,100.0,101.0,102.0,103.0\n" +
                "104.0,105.0,106.0,107.0,108.0\n" +
                "\n" +
                "latitude\t  degree\t  Latitude coordinate values\n" +
                "12.0,12.5,13.0,13.5,14.0\n" +
                "13.5,14.0,14.5,15.0,15.5\n" +
                "15.0,15.5,16.0,16.5,17.0\n" +
                "16.5,17.0,17.5,18.0,18.5\n" +
                "18.0,18.5,19.0,19.5,20.0\n" +
                "\n" +
                "longitude\t  degree\t  Longitude coordinate values\n" +
                "32.0,32.5,33.0,33.5,34.0\n" +
                "33.5,34.0,34.5,35.0,35.5\n" +
                "35.0,35.5,36.0,36.5,37.0\n" +
                "36.5,37.0,37.5,38.0,38.5\n" +
                "38.0,38.5,39.0,39.5,40.0\n";

        final String mapGcPart;
        if (geocodingType == MAP_GC) {
            mapGcPart = "\n" +
                    "LatMapCoords\t  degree\t  Latitude coordinate values\n" +
                    "0.5,1.5,2.5,3.5,4.5\n" +
                    "0.5,1.5,2.5,3.5,4.5\n" +
                    "0.5,1.5,2.5,3.5,4.5\n" +
                    "0.5,1.5,2.5,3.5,4.5\n" +
                    "0.5,1.5,2.5,3.5,4.5\n" +
                    "\n" +
                    "LonMapCoords\t  degree\t  Longitude coordinate values\n" +
                    "0.5,0.5,0.5,0.5,0.5\n" +
                    "1.5,1.5,1.5,1.5,1.5\n" +
                    "2.5,2.5,2.5,2.5,2.5\n" +
                    "3.5,3.5,3.5,3.5,3.5\n" +
                    "4.5,4.5,4.5,4.5,4.5\n";
        } else {
            mapGcPart = "";
        }

        final String middlePart = "\n" +
                "\n" +
                " Geocoding\n" +
                "***********\n" +
                "\n";
        final String gcPart;
        if (geocodingType == TiePoint_GC) {
            gcPart = "LatitudeRasterName\t  latitude\n" +
                    "LongitudeRasterName\t  longitude\n";
        } else if (geocodingType == PIXEL_GC) {
            gcPart = "LatitudeRasterName\t  LatBand\n" +
                    "LongitudeRasterName\t  LonBand\n";
        } else {
            gcPart = "LatitudeRasterName\t  LatMapCoords\n" +
                    "LongitudeRasterName\t  LonMapCoords\n";
        }
        final String lastPart = "\n" +
                "\n" +
                " Metadata\n" +
                "**********\n" +
                "\n" +
                "ME ++ metadata\n" +
                "ME ++   ebene 1\n" +
                "MA --     att1\t  utc\t  13-JAN-2000 00:00:23.000034\t  att1_unit\t  att1_desc\n" +
                "MA --     att2\t  ascii\t  Text\t  att2_unit\t  att2_desc\n" +
                "MA --     att3\t  float32\t  12.4,13.5,0.21\t  att3_unit\t  att3_desc\n" +
                "ME ++     ebene 2\n" +
                "MA --       att1\t  utc\t  13-JAN-2000 00:00:23.000034\t  att1_unit\t  att1_desc\n" +
                "MA --       att2\t  ascii\t  Text\t  att2_unit\t  att2_desc\n" +
                "MA --       att3\t  float32\t  12.4,13.5,0.21\t  att3_unit\t  att3_desc\n";
        return firstPart + mapGcPart + middlePart + gcPart + lastPart;
    }

    private void initProduct() {
        _product = new Product("ProdName", "ProdType", 5, 5);
        _product.setStartTime(new ProductData.UTC(212313, 456, 232));
        _product.setEndTime(new ProductData.UTC(212313, 556, 210));
        addMetadata();
        addRasterdata();
    }

    private void addMetadata() {
        final MetadataElement metadataRoot = _product.getMetadataRoot();

        final MetadataElement ebene1 = new MetadataElement("ebene 1");
        metadataRoot.addElement(ebene1);

        String name;
        ProductData data;
        String unit;
        String desc;

        name = "att1";
        data = new ProductData.UTC(12, 23, 34);
        unit = name + "_unit";
        desc = name + "_desc";
        ebene1.addAttribute(createAttribute(name, data, unit, desc));

        name = "att2";
        data = ProductData.createInstance("Text");
        unit = name + "_unit";
        desc = name + "_desc";
        ebene1.addAttribute(createAttribute(name, data, unit, desc));

        name = "att3";
        data = ProductData.createInstance(new float[]{12.4f, 13.5f, 0.21f});
        unit = name + "_unit";
        desc = name + "_desc";
        ebene1.addAttribute(createAttribute(name, data, unit, desc));

        final MetadataElement ebene2 = new MetadataElement("ebene 2");
        ebene1.addElement(ebene2);

        name = "att1";
        data = new ProductData.UTC(12, 23, 34);
        unit = name + "_unit";
        desc = name + "_desc";
        ebene2.addAttribute(createAttribute(name, data, unit, desc));

        name = "att2";
        data = ProductData.createInstance("Text");
        unit = name + "_unit";
        desc = name + "_desc";
        ebene2.addAttribute(createAttribute(name, data, unit, desc));

        name = "att3";
        data = ProductData.createInstance(new float[]{12.4f, 13.5f, 0.21f});
        unit = name + "_unit";
        desc = name + "_desc";
        ebene2.addAttribute(createAttribute(name, data, unit, desc));
    }

    private MetadataAttribute createAttribute(
            final String name,
            final ProductData data,
            final String unit,
            final String desc) {
        final MetadataAttribute att = new MetadataAttribute(name, data, true);
        att.setUnit(unit);
        att.setDescription(desc);
        return att;
    }

    private void addRasterdata() {
        final String bandName = "Band_1";
        _product.addBand(bandName, ProductData.TYPE_INT16);
        final Band band = _product.getBand(bandName);
        band.setDescription(bandName + "_Description");
        band.setUnit(bandName + "_Unit");
        band.ensureRasterData();
        fill(band.getData(), 5);

        final String latBandName = LAT_BAND_NAME;
        _product.addBand(latBandName, ProductData.TYPE_UINT16);
        final Band latBand = _product.getBand(latBandName);
        latBand.setDescription(latBandName + "_Description");
        latBand.setUnit(latBandName + "_Unit");
        latBand.ensureRasterData();
        fill(latBand.getData(), 42);

        final String lonBandName = LON_BAND_NAME;
        _product.addBand(lonBandName, ProductData.TYPE_UINT16);
        final Band lonBand = _product.getBand(lonBandName);
        lonBand.setDescription(lonBandName + "_Description");
        lonBand.setUnit(lonBandName + "_Unit");
        lonBand.ensureRasterData();
        fill(lonBand.getData(), 84);

        final TiePointGrid latTiePoints = new TiePointGrid(LATITUDE_NAME, 3, 3, 0, 0, 2, 2, new float[9]);
        latTiePoints.setDescription("Latitude coordinate values");
        latTiePoints.setUnit("degree");
        fill(latTiePoints.getData(), 11);
        _product.addTiePointGrid(latTiePoints);

        final TiePointGrid lonTiePoints = new TiePointGrid(LONGITUDE_NAME, 3, 3, 0, 0, 2, 2, new float[9]);
        lonTiePoints.setDescription("Longitude coordinate values");
        lonTiePoints.setUnit("degree");
        fill(lonTiePoints.getData(), 31);
        _product.addTiePointGrid(lonTiePoints);
    }

    private void fill(ProductData data, int offset) {
        for (int i = 0; i < data.getNumElems(); i++) {
            data.setElemIntAt(i, i + offset);
        }
    }

    private void addGeocoding(int geocodingType) throws IOException {
        if (geocodingType == TiePoint_GC) {
            final TiePointGrid latGrid = _product.getTiePointGrid(LATITUDE_NAME);
            final TiePointGrid lonGrid = _product.getTiePointGrid(LONGITUDE_NAME);
            _product.setGeoCoding(new TiePointGeoCoding(latGrid, lonGrid));
        } else if (geocodingType == PIXEL_GC) {
            final Band latBand = _product.getBand(LAT_BAND_NAME);
            final Band lonBand = _product.getBand(LON_BAND_NAME);
            _product.setGeoCoding(new PixelGeoCoding(latBand, lonBand, null, 5, ProgressMonitor.NULL));
        } else if (geocodingType == MAP_GC) {
            _product.setGeoCoding(createMockGeocoding());
        }
    }

    private GeoCoding createMockGeocoding() {
        return new GeoCoding() {
            public boolean isCrossingMeridianAt180() {
                return false;
            }

            public boolean canGetPixelPos() {
                return true;
            }

            public boolean canGetGeoPos() {
                return true;
            }

            public PixelPos getPixelPos(final GeoPos geoPos, PixelPos pixelPos) {
                if (pixelPos == null) {
                    pixelPos = new PixelPos();
                }
                pixelPos.x = geoPos.lat;
                pixelPos.y = geoPos.lon;
                return pixelPos;
            }

            public GeoPos getGeoPos(final PixelPos pixelPos, GeoPos geoPos) {
                if (geoPos == null) {
                    geoPos = new GeoPos();
                }
                geoPos.lat = pixelPos.x;
                geoPos.lon = pixelPos.y;
                return geoPos;
            }

            public Datum getDatum() {
                return null;
            }

            public void dispose() {
            }

            @Override
            public CoordinateReferenceSystem getBaseCRS() {
                return null;
            }

            @Override
            public CoordinateReferenceSystem getImageCRS() {
                return null;
            }

            @Override
            public CoordinateReferenceSystem getMapCRS() {
                return null;
            }

            @Override
            public CoordinateReferenceSystem getGeoCRS() {
                return null;
            }

            @Override
            public MathTransform getImageToMapTransform() {
                return null;
            }

            @Override
            public CoordinateReferenceSystem getModelCRS() {
                return null;
            }

            @Override
            public AffineTransform getImageToModelTransform() {
                return null;
            }
        };
    }
}