/*
 * $Id: AsciiProductWriterPlugIn.java,v 1.1 2007/04/16 13:24:25 marcop Exp $
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

import org.esa.beam.framework.dataio.ProductWriter;
import org.esa.beam.framework.dataio.ProductWriterPlugIn;
import org.esa.beam.util.io.BeamFileFilter;

import java.io.File;
import java.io.Writer;
import java.util.Locale;

public class AsciiProductWriterPlugIn implements ProductWriterPlugIn {

    public Class[] getOutputTypes() {
        return new Class[]{String.class, File.class, Writer.class};
    }

    public ProductWriter createWriterInstance() {
        return new AsciiProductWriter(this);
    }

    public String[] getFormatNames() {
        return new String[]{AsciiProductFormatConstants.FORMAT_NAME};
    }

    public String[] getDefaultFileExtensions() {
        return new String[]{AsciiProductFormatConstants.FILE_EXTENSION};
    }

    public String getDescription(Locale locale) {
        return AsciiProductFormatConstants.WRITER_DESCRIPTION;
    }

    public BeamFileFilter getProductFileFilter() {
        String[] formatNames = getFormatNames();
        String formatName = "";
        if (formatNames.length > 0) {
            formatName = formatNames[0];
        }
        return new BeamFileFilter(formatName, getDefaultFileExtensions(), getDescription(null));
    }

}
