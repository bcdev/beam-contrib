/*
 * $Id: AsciiProductFormatConstants.java,v 1.1 2007/04/16 13:24:25 marcop Exp $
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

public class AsciiProductFormatConstants {

    public static final String FORMAT_NAME = "BEAM-ASCII";
    public static final String FILE_EXTENSION = ".txt";
    public static final String WRITER_DESCRIPTION = "BEAM Ascii product writer";
    public static final String READER_DESCRIPTION = "BEAM Ascii product reader";

    public static final String LAT_MAP_COORDS_NAME = "LatMapCoords";
    public static final String LON_MAP_COORDS_NAME = "LonMapCoords";
    public static final String LAT_MAP_COORDS_LINE_SUFFIX = "\t  degree\t  Latitude coordinate values";
    public static final String LON_MAP_COORDS_LINE_SUFFIX = "\t  degree\t  Longitude coordinate values";
    public static final String FIRST_LINE = "BEAM_ASCII_PRODUCT_FORMAT_V";
    public static final String VERSION_STRING = "001";
    public static final String KEY_PRODUCT_HEADER = " Product Header";
    public static final String KEY_PRODUCT_NAME = "ProductName";
    public static final String KEY_PRODUCT_TYPE = "ProductType";
    public static final String KEY_WIDTH = "Width";
    public static final String KEY_HEIGHT = "Height";
    public static final String KEY_START_TIME = "StartTime";
    public static final String KEY_END_TIME = "EndTime";
    public static final String KEY_NUM_BANDS = "NumBands";
    public static final String KEY_RASTER_DATA = " RasterData";
    public static final String KEY_GEOCODING = " Geocoding";
    public static final String KEY_LATITUDE_RASTER_NAME = "LatitudeRasterName";
    public static final String KEY_LONGITUDE_RASTER_NAME = "LongitudeRasterName";
    public static final String KEY_METADATA = " Metadata";
    public static final String KEY_METADATA_ROOT = "ME ++ metadata";
    public static final String KEY_METADATA_ELEMENT = "ME ++";
    public static final String KEY_METADATA_ATTRIBUTE = "MA --";

}
