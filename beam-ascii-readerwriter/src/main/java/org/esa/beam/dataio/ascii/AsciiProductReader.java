/*
 * $Id: AsciiProductReader.java,v 1.2 2007/04/18 10:29:21 marcop Exp $
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
import org.esa.beam.framework.dataio.AbstractProductReader;
import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.IllegalFileFormatException;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.util.StringUtils;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AsciiProductReader extends AbstractProductReader {

    private ImageInputStream _stream;
    private Product _product;
    private HashMap _bandStreamPositionMap;
    private long _metadataPos;

    protected AsciiProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    protected Product readProductNodesImpl() throws IOException, IllegalFileFormatException {
        try {
            if (getReaderPlugIn().getDecodeQualification(getInput()) == DecodeQualification.UNABLE) {
                throw new IOException("Unsupported product format."); /*I18N*/
            }
            initStream();
            final Header header = new Header(_stream);
            _product = new Product(header.getProductName(),
                    header.getProductType(),
                    header.getWidth(),
                    header.getHeight());
            _product.setStartTime(getUTC(header.getStartTime()));

            _product.setEndTime(getUTC(header.getEndTime()));
            readBands();
            final int pNumBands = _product.getNumBands();
            final int hNumBands = header.getNumBands();
            if (pNumBands != hNumBands) {
                throw new IOException(
                        "File corrupted. Number of Bands (" + pNumBands + ") are not equal to the number (" + hNumBands + ") defined in the header."); /*I18N*/
            }
            _product.setProductReader(this);
            initGeocoding();
            readMetadata();
            return _product;
        } catch (IOException e) {
            if (_stream != null) {
                _stream.close();
            }
            throw e;
        }
    }

    private void readBands() throws IOException {
        final int height = _product.getSceneRasterHeight();
        _bandStreamPositionMap = new HashMap();
        String line = _stream.readLine();
        while (line != null
                && !line.startsWith(AsciiProductFormatConstants.KEY_GEOCODING)) {
            if (line.trim().length() > 0 && !line.trim().startsWith("***")) {
                final String[] strings = StringUtils.toStringArray(line, "\t");
                final BandProperties bandProperties = new BandProperties(strings);
                final String bandName = bandProperties.getBandName();
                _product.addBand(bandName, ProductData.TYPE_FLOAT64);
                _product.getBand(bandName).setUnit(bandProperties.getUnit());
                _product.getBand(bandName).setDescription(bandProperties.getDescription());
                _bandStreamPositionMap.put(bandProperties.getBandName(), new Long(_stream.getStreamPosition()));
                skipLines(height);
            }
            line = _stream.readLine();
        }
    }

    private void initGeocoding() throws IOException {
        String latBandName = null;
        String lonBandName = null;
        String line = _stream.readLine();
        while (line != null
                && !line.startsWith(AsciiProductFormatConstants.KEY_METADATA)) {
            final String[] strings = StringUtils.toStringArray(line, "\t");
            if (line.startsWith(AsciiProductFormatConstants.KEY_LATITUDE_RASTER_NAME)) {
                latBandName = strings[1];
            }
            if (line.startsWith(AsciiProductFormatConstants.KEY_LONGITUDE_RASTER_NAME)) {
                lonBandName = strings[1];
            }
            line = _stream.readLine();
        }
        _metadataPos = _stream.getStreamPosition();
        if (latBandName == null && lonBandName == null) {
            //init no Geocoding
            return;
        }
        final Band latBand = _product.getBand(latBandName);
        final Band lonBand = _product.getBand(lonBandName);
        if (latBand == null || lonBand == null) {
            throw new IOException(
                    "File corrupted. Geocoding parameters are not defined correctly."); /*I18N*/
        }
        final PixelGeoCoding geoCoding = new PixelGeoCoding(latBand, lonBand, null, 5, ProgressMonitor.NULL);
        _product.setGeoCoding(geoCoding);
    }

    private void readMetadata() throws IOException {
        _stream.seek(_metadataPos);
        String line = _stream.readLine();
        final List elemList = new ArrayList();
        final List depthList = new ArrayList();

        final MetadataElement metadataRoot = _product.getMetadataRoot();
        elemList.add(metadataRoot);
        depthList.add(new Integer(0));

        while (line != null) {
            final boolean isROOT = line.startsWith(AsciiProductFormatConstants.KEY_METADATA_ROOT);
            final boolean isME = line.startsWith(AsciiProductFormatConstants.KEY_METADATA_ELEMENT);
            final boolean isMA = line.startsWith(AsciiProductFormatConstants.KEY_METADATA_ATTRIBUTE);

            if (!isROOT && (isME || isMA)) {
                final String subLine = line.substring(5);
                final String[] strings = StringUtils.toStringArray(subLine, "\t");
                if (isME) {
                    final int depth = subLine.indexOf(strings[0]);
                    while (depthList.size() > 1) {
                        final int lastDepth = ((Integer) depthList.get(depthList.size() - 1)).intValue();
                        if (lastDepth < depth) {
                            break;
                        }
                        depthList.remove(depthList.size() - 1);
                        elemList.remove(elemList.size() - 1);
                    }
                    final MetadataElement element = new MetadataElement(strings[0]);
                    ((MetadataElement) elemList.get(elemList.size() - 1)).addElement(element);
                    depthList.add(new Integer(depth));
                    elemList.add(element);
                } else {
                    final String Name = strings[0];
                    final String typeString = strings[1];
                    final String value = strings[2];
                    final String unit = strings[3];
                    final String description = strings[4];
                    final ProductData data;
                    if (ProductData.TYPESTRING_ASCII.equalsIgnoreCase(typeString)) {
                        data = ProductData.createInstance(value);
                    } else if (ProductData.TYPESTRING_UTC.equalsIgnoreCase(typeString)) {
                        try {
                            data = ProductData.UTC.parse(value);
                        } catch (ParseException e) {
                            throw new IOException("Illegal UTC format");
                        }
                    } else {
                        final String[] values = StringUtils.toStringArray(value, ",");
                        data = ProductData.createInstance(ProductData.getType(typeString), values.length);
                        data.setElems(values);
                    }
                    final MetadataAttribute attrib = new MetadataAttribute(Name, data, true);
                    attrib.setUnit(unit);
                    attrib.setDescription(description);
                    ((MetadataElement) elemList.get(elemList.size() - 1)).addAttribute(attrib);
                }
            }
            line = _stream.readLine();
        }
    }

    private void skipLines(int height) throws IOException {
        for (int i = 0; i < height; i++) {
            _stream.readLine();
        }
    }

    private ProductData.UTC getUTC(String timeString) throws IOException {
        if (StringUtils.isNotNullAndNotEmpty(timeString)) {
            try {
                return ProductData.UTC.parse(timeString);
            } catch (ParseException e) {
                final IOException ioException = new IOException(e.getMessage());
                ioException.initCause(e);
                throw ioException;
            }
        }
        return null;
    }


    private void initStream() throws IOException {
        final AsciiProductReaderPlugIn in = (AsciiProductReaderPlugIn) getReaderPlugIn();
        _stream = in.createStream(getInput());
    }

    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY,
                                          int sourceWidth, int sourceHeight,
                                          int sourceStepX, int sourceStepY,
                                          Band destBand,
                                          int destOffsetX, int destOffsetY,
                                          int destWidth, int destHeight,
                                          ProductData destBuffer,
                                          ProgressMonitor pm) throws IOException {
////       because this reader can not handle with a subset definition
//       destWidth = sourceWidth;
//       destHeight = sourceHeight;
//       sourceStepX = 1;
//       sourceStepY = 1;
////          as width =5 heght =4 in test
////               sceneWidth =5     sourceMaxY=3 y.....3


        final int sceneWidth = _product.getSceneRasterWidth();
        final int sourceMaxY = sourceOffsetY + sourceHeight - 1;
        // here you are getting the band name... in the file and getting the stream position of this in the file
        final String bandName = destBand.getName();
        final Long streamPosition = (Long) _bandStreamPositionMap.get(bandName);
        _stream.seek(streamPosition.longValue());
        skipLines(sourceOffsetY);

        pm.beginTask("Reading band '" + destBand.getName() + "'...", sourceMaxY - sourceOffsetY);
        // For each scan in the data source

        try {
            final double[] destDoubles = (double[]) destBuffer.getElems();
            int destPos = 0;
            for (int sourceY = sourceOffsetY; sourceY <= sourceMaxY; sourceY += sourceStepY) {
                if (pm.isCanceled()) {
                    break;
                }
                //public void readFrom(int startPos, int numElems, ImageInputStream input, long inputPos)
                final double[] line = StringUtils.toDoubleArray(_stream.readLine(), ",");
                if (line.length != sceneWidth) {
                    throw new IOException(
                            "File corrupted. " +
                                    "Number of values in line #" + sourceY + " of band '" + bandName + "' are not the expected (" + sceneWidth + ") number of values defined in the file header."); /*I18N*/
                }
                System.arraycopy(line, sourceOffsetX, destDoubles, destPos, destWidth);
                destPos += destWidth;
                pm.worked(sourceStepY);
            }
            destBuffer.setElems(destDoubles);

        } finally {
            pm.done();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        _stream.close();
        _stream = null;
        _bandStreamPositionMap.clear();
        _bandStreamPositionMap = null;
    }

    static class Header {

        private String _productName;
        private String _productType;
        private int _width;
        private int _height;
        private String _startTime;
        private String _endTime;
        private int _numBands;

        public Header(final ImageInputStream stream) throws IOException {
            String line = stream.readLine();
            //iterate to RasterData
            while (line != null && !line.startsWith(AsciiProductFormatConstants.KEY_RASTER_DATA)) {
                if (line.startsWith(AsciiProductFormatConstants.KEY_PRODUCT_NAME)) {
                    String pName = line.substring(line.indexOf("\t")).trim();
                    if (StringUtils.isNotNullAndNotEmpty(pName)) {
                        _productName = pName;
                    }
                } else if (line.startsWith(AsciiProductFormatConstants.KEY_PRODUCT_TYPE)) {
                    String pType = line.substring(line.indexOf("\t")).trim();
                    if (StringUtils.isNotNullAndNotEmpty(pType)) {
                        _productType = pType;
                    }
                } else if (line.startsWith(AsciiProductFormatConstants.KEY_WIDTH)) {
                    String pWidth = line.substring(line.indexOf("\t")).trim();
                    if (StringUtils.isNotNullAndNotEmpty(pWidth)) {
                        _width = Integer.parseInt(pWidth);
                    }
                } else if (line.startsWith(AsciiProductFormatConstants.KEY_HEIGHT)) {
                    String pHeight = line.substring(line.indexOf("\t")).trim();
                    if (StringUtils.isNotNullAndNotEmpty(pHeight)) {
                        _height = Integer.parseInt(pHeight);
                    }
                } else if (line.startsWith(AsciiProductFormatConstants.KEY_START_TIME)) {
                    String startTime = line.substring(line.indexOf("\t")).trim();
                    if (StringUtils.isNotNullAndNotEmpty(startTime)) {
                        _startTime = startTime;
                    }
                } else if (line.startsWith(AsciiProductFormatConstants.KEY_END_TIME)) {
                    String endTime = line.substring(line.indexOf("\t")).trim();
                    if (StringUtils.isNotNullAndNotEmpty(endTime)) {
                        _endTime = endTime;
                    }
                } else if (line.startsWith(AsciiProductFormatConstants.KEY_NUM_BANDS)) {
                    String numBands = line.substring(line.indexOf("\t")).trim();
                    if (StringUtils.isNotNullAndNotEmpty(numBands)) {
                        _numBands = Integer.parseInt(numBands);
                    }
                }
                line = stream.readLine();
            }
        }

        public String getProductName() {
            return _productName;
        }

        public void setProductName(String productName) {
            _productName = productName;
        }

        public String getProductType() {
            return _productType;
        }

        public void setProductType(String productType) {
            _productType = productType;
        }

        public int getWidth() {
            return _width;
        }

        public void setWidth(int width) {
            _width = width;
        }

        public int getHeight() {
            return _height;
        }

        public void setHeight(int height) {
            _height = height;
        }

        public String getStartTime() {
            return _startTime;
        }

        public void setStartTime(String startTime) {
            _startTime = startTime;
        }

        public String getEndTime() {
            return _endTime;
        }

        public void setEndTime(String endTime) {
            _endTime = endTime;
        }

        public int getNumBands() {
            return _numBands;
        }

        public void setNumBands(int numBands) {
            _numBands = numBands;
        }
    }

    static class BandProperties {

        private String _bandName;
        private String _unit;
        private String _description;

        BandProperties(String[] strings) {
            for (int i = 0; i < strings.length; i++) {
                final String string = strings[i];
                if (i == 0) {
                    _bandName = string;
                } else if (i == 1) {
                    _unit = string;
                } else if (i == 2) {
                    _description = string;
                }
            }
        }

        public String getBandName() {
            return _bandName;
        }

        public String getUnit() {
            return _unit;
        }

        public String getDescription() {
            return _description;
        }
    }

}

