package org.esa.beam.ofew;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.Band;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 */
public class SpectralBandFinder {

	public static final double[] OFEW_WAVELENGTHS = {478.0, 560.0, 660.0, 835.0, 1650.0, 2208.0};

	private final double[] wavelengths;
    private final List<Band> bandList;

    public SpectralBandFinder(Product product, double[] wavelengths) {
        this.wavelengths = wavelengths;

        bandList = new ArrayList<Band>(wavelengths.length);
        findSpectralBands(product);
    }

    public int getBandCount() {
        return bandList.size();
    }

    public boolean hasFoundAll() {
        return bandList.size() == wavelengths.length;
    }

    public boolean hasFound(double wavelength) {
        for (final Band band : bandList) {
            if (band.getSpectralWavelength() == wavelength) {
                return true;
            }
        }
        return false;
    }

    public Band[] getBands() {
        return bandList.toArray(new Band[bandList.size()]);
    }

    public String[] getBandNames() {
    	String[] bandNames = new String[bandList.size()];
    	for (int i = 0; i < bandNames.length; i++) {
			bandNames[i] = bandList.get(i).getName();
		}
        return bandNames;
    }

    public Band getBand(int i) {
        return bandList.get(i);
    }

    private void findSpectralBands(Product product) {
        search:
        for (final double wavelength : wavelengths) {
            for (final Band band : product.getBands()) {
                if (band.getSpectralBandIndex() != -1) {
                    float spectralWavelength = band.getSpectralWavelength();
                    float spectralBandwidth = band.getSpectralBandwidth();
					if (wavelength >= spectralWavelength - (spectralBandwidth/2) &&
							wavelength < spectralWavelength + (spectralBandwidth/2)) {
                        bandList.add(band);
                        continue search;
                    }
                }
            }
        }
    }
}
