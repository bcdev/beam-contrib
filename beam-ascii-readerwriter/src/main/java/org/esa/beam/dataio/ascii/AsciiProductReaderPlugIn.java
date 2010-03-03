/*
 * $Id: AsciiProductReaderPlugIn.java,v 1.1 2007/04/16 13:24:25 marcop Exp $
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

import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.util.io.BeamFileFilter;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class AsciiProductReaderPlugIn implements ProductReaderPlugIn {

    public DecodeQualification getDecodeQualification(Object input) {
        final ImageInputStream stream;
        try {
            stream = createStream(input);
        } catch (IOException e) {
            return DecodeQualification.UNABLE;
        }
        if (stream == null) {
            return DecodeQualification.UNABLE;
        }

        final String line;
        try {
            line = stream.readLine();
        } catch (IOException e) {
            return DecodeQualification.UNABLE;
        } finally {
            if (input != stream) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // does nothing because nothing can be done after close
                }
            }
        }
        if (line != null && line.startsWith(AsciiProductFormatConstants.FIRST_LINE)) {
            return DecodeQualification.INTENDED;
        }
        return DecodeQualification.UNABLE;
    }

    public ImageInputStream createStream(Object input) throws IOException {
        final ImageInputStream reader;
        if (input instanceof String) {
            reader = new FileImageInputStream(new File((String) input));
        } else if (input instanceof File) {
            reader = new FileImageInputStream((File) input);
        } else if (input instanceof ImageInputStream) {
            reader = (ImageInputStream) input;
        } else {
            reader = null;
        }
        return reader;
    }

    public Class[] getInputTypes() {
        return new Class[]{String.class, File.class, ImageInputStream.class};
    }

    public ProductReader createReaderInstance() {
        return new AsciiProductReader(this);
    }

    public String[] getFormatNames() {
        return new String[]{AsciiProductFormatConstants.FORMAT_NAME};
    }

    public String[] getDefaultFileExtensions() {
        return new String[]{AsciiProductFormatConstants.FILE_EXTENSION};
    }

    public String getDescription(Locale locale) {
        return AsciiProductFormatConstants.READER_DESCRIPTION;
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
