/*
 * $Id: AsciiProductWriter.java,v 1.1 2007/04/16 13:24:25 marcop Exp $
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
import org.esa.beam.framework.dataio.AbstractProductWriter;
import org.esa.beam.framework.dataio.ProductWriterPlugIn;
import org.esa.beam.framework.datamodel.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

public class AsciiProductWriter extends AbstractProductWriter {

    private final static String _SEPARATOR = "\t  ";

    private Writer _writer;
    private int _linesWritten;

    public AsciiProductWriter(ProductWriterPlugIn writerPlugIn) {
        super(writerPlugIn);
    }

    @Override
    protected void writeProductNodesImpl() throws IOException {
        final Object output = getOutput();
        if (output instanceof String) {
            _writer = new FileWriter((String) output);
        } else if (output instanceof File) {
            _writer = new FileWriter((File) output);
        } else if (output instanceof Writer) {
            _writer = (Writer) output;
        }
        _linesWritten = 0;
        println(AsciiProductFormatConstants.FIRST_LINE +
                AsciiProductFormatConstants.VERSION_STRING);
        println();
        println(" Product Header");
        println("****************");
        println();
        printProductHeader();
        println();
        final int lineNumber = 47;
        println(" RasterData", lineNumber);
        println("************");
        println();
        printRasterdata();
        println();
        println(" Geocoding");
        println("***********");
        println();
        printGeocodingInfo();
        println();
        println(" Metadata");
        println("**********");
        println();
        printMetadata();

        getSourceProduct().setModified(false);
    }

    private void printProductHeader() throws IOException {
        final Product product = getSourceProduct();
        println("ProductName\t" + product.getName());
        println("ProductType\t" + product.getProductType());
        println("Width\t" + product.getSceneRasterWidth());
        println("Height\t" + product.getSceneRasterHeight());
        final ProductData.UTC startTime = product.getStartTime();
        if (startTime != null) {
            println("StartTime\t" + startTime.format());
        }
        final ProductData.UTC endTime = product.getEndTime();
        if (endTime != null) {
            println("EndTime\t" + endTime.format());
        }

        int numRasters = product.getNumBands();
        numRasters += product.getNumTiePointGrids();
        final GeoCoding geoCoding = product.getGeoCoding();
        if (geoCoding != null && !isRastergeocoding(geoCoding)) {
            numRasters += 2;
        }
        println("NumBands\t" + numRasters);
    }

    private boolean isRastergeocoding(GeoCoding geoCoding) {
        if (geoCoding instanceof TiePointGeoCoding
                || geoCoding instanceof PixelGeoCoding) {
            return true;
        } else {
            return false;
        }
    }

    private void printMetadata() throws IOException {
        final MetadataElement metadataRoot = getSourceProduct().getMetadataRoot();
        final String indent = "";
        printElements(new MetadataElement[]{metadataRoot}, indent);
    }

    private void printElements(MetadataElement[] elements, String indent) throws IOException {
        final String prefix = "ME ++ " + indent;
        for (int i = 0; i < elements.length; i++) {
            final MetadataElement element = elements[i];
            println(prefix, element.getName());
            printAttributes(element.getAttributes(), indent + "  ");
            printElements(element.getElements(), indent + "  ");
        }
    }

    private void printAttributes(MetadataAttribute[] attributes, String indent) throws IOException {
        final String prefix = "MA -- " + indent;
        for (int j = 0; j < attributes.length; j++) {
            final MetadataAttribute attribute = attributes[j];
            final ProductData data = attribute.getData();
            final int dataType;
            if (data instanceof ProductData.UTC) {
                dataType = ProductData.TYPE_UTC;
            } else if (data instanceof ProductData.ASCII) {
                dataType = ProductData.TYPE_ASCII;
            } else {
                dataType = data.getType();
            }
            final StringBuffer sb = new StringBuffer();
            sb.append(attribute.getName());
            sb.append(_SEPARATOR);
            sb.append(ProductData.getTypeString(dataType));
            sb.append(_SEPARATOR);
            sb.append(data.getElemString());
            sb.append(_SEPARATOR);
            final String unit = attribute.getUnit();
            sb.append(unit != null ? unit : "");
            sb.append(_SEPARATOR);
            final String description = attribute.getDescription();
            sb.append(description != null ? description : "");
            println(prefix, sb.toString());
        }
    }

    private void printRasterdata() throws IOException {
        final ArrayList rasterList = new ArrayList();
        collectBands(rasterList);
        collectTiePointRaster(rasterList);
        for (int i = 0; i < rasterList.size(); i++) {
            final RasterDataNode node = (RasterDataNode) rasterList.get(i);
            printRasterData(node);
        }

        final Product product = getSourceProduct();
        final GeoCoding geoCoding = product.getGeoCoding();
        if (geoCoding != null
                && !(geoCoding instanceof TiePointGeoCoding)
                && !(geoCoding instanceof PixelGeoCoding)) {
            printMapGeocodingRaster();
        }
    }

    private void printMapGeocodingRaster() throws IOException {
        final Product product = getSourceProduct();
        final int height = product.getSceneRasterHeight();
        final int width = product.getSceneRasterWidth();

        final GeoCoding geoCoding = product.getGeoCoding();

        final GeoPos geoPos = new GeoPos();
        final PixelPos pixelPos = new PixelPos();

        //create lat map band
        println(AsciiProductFormatConstants.LAT_MAP_COORDS_NAME +
                AsciiProductFormatConstants.LAT_MAP_COORDS_LINE_SUFFIX);
        for (float y = 0.5f; y < height; y++) {
            final StringBuffer sb = new StringBuffer();
            for (float x = 0.5f; x < width; x++) {
                if (x > 1) {
                    sb.append(",");
                }
                pixelPos.setLocation(x, y);
                geoCoding.getGeoPos(pixelPos, geoPos);
                sb.append(String.valueOf(geoPos.lat));
            }
            println(sb.toString());
        }
        println();

        //create lon map band
        println(AsciiProductFormatConstants.LON_MAP_COORDS_NAME +
                AsciiProductFormatConstants.LON_MAP_COORDS_LINE_SUFFIX);
        for (float y = 0.5f; y < height; y++) {
            final StringBuffer sb = new StringBuffer();
            for (float x = 0.5f; x < width; x++) {
                if (x > 1) {
                    sb.append(",");
                }
                pixelPos.setLocation(x, y);
                geoCoding.getGeoPos(pixelPos, geoPos);
                sb.append(String.valueOf(geoPos.lon));
            }
            println(sb.toString());
        }
        println();
    }

    private void printRasterData(final RasterDataNode node) throws IOException {
        final StringBuffer sb = new StringBuffer();
        sb.append(node.getName());
        sb.append(_SEPARATOR);
        sb.append(node.getUnit());
        sb.append(_SEPARATOR);
        sb.append(node.getDescription());
        println(sb.toString());

        final int width = node.getSceneRasterWidth();
        final int height = node.getSceneRasterHeight();
        final float[] line = new float[width];
        final ProductData data = ProductData.createInstance(line);
        for (int y = 0; y < height; y++) {
            node.readPixels(0, y, width, 1, line, ProgressMonitor.NULL);
            println(data.getElemString());
        }
        println();
    }

    private void collectTiePointRaster(ArrayList rasterList) {
        final Product product = getSourceProduct();
        final TiePointGrid[] tiePointGrids = product.getTiePointGrids();
        for (int i = 0; i < tiePointGrids.length; i++) {
            final TiePointGrid tiePointGrid = tiePointGrids[i];
            rasterList.add(tiePointGrid);
        }
    }

    private void collectBands(final ArrayList rasterList) {
        final Product product = getSourceProduct();
        final Band[] bands = product.getBands();
        for (int i = 0; i < bands.length; i++) {
            final Band band = bands[i];
            rasterList.add(band);
        }
    }

    private void printGeocodingInfo() throws IOException {
        final GeoCoding geoCoding = getSourceProduct().getGeoCoding();
        if (geoCoding instanceof TiePointGeoCoding) {
            final TiePointGeoCoding tiePointGeoCoding = (TiePointGeoCoding) geoCoding;
            println("LatitudeRasterName\t  " + tiePointGeoCoding.getLatGrid().getName());
            println("LongitudeRasterName\t  " + tiePointGeoCoding.getLonGrid().getName());
        } else if (geoCoding instanceof PixelGeoCoding) {
            final PixelGeoCoding pixelGeoCoding = (PixelGeoCoding) geoCoding;
            println("LatitudeRasterName\t  " + pixelGeoCoding.getLatBand().getName());
            println("LongitudeRasterName\t  " + pixelGeoCoding.getLonBand().getName());
        } else {
            println("LatitudeRasterName\t  " + AsciiProductFormatConstants.LAT_MAP_COORDS_NAME);
            println("LongitudeRasterName\t  " + AsciiProductFormatConstants.LON_MAP_COORDS_NAME);
        }
        println();
    }

    public void writeBandRasterData(Band sourceBand, int sourceOffsetX, int sourceOffsetY, int sourceWidth,
                                    int sourceHeight, ProductData sourceBuffer) throws IOException {
        //does Nothing
    }

    public void writeBandRasterData(Band sourceBand, int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, ProductData sourceBuffer, ProgressMonitor pm) throws IOException {
        //does Nothing
    }

    public void flush() throws IOException {
        _writer.flush();
    }

    public void close() throws IOException {
        _writer.close();
    }

    public void deleteOutput() throws IOException {
        //does Nothing
    }

    private void println() throws IOException {
        println("");
    }

    private void println(String str) throws IOException {
        println("", str);
    }

    private void println(String str, int lineNumber) throws IOException {
        while (_linesWritten < lineNumber - 1) {
            println();
        }
        println(str);
    }

    private void println(String prefix, String str) throws IOException {
        _writer.write(prefix);
        _writer.write(str);
        _writer.write("\n");
        _linesWritten++;
    }
}
