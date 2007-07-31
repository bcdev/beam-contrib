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

    final Product product;
    final double[] wavelengths;
    final List<Band> bandList;

    public SpectralBandFinder(Product product, double[] wavelengths) {
        this.product = product;
        this.wavelengths = wavelengths;

        bandList = new ArrayList<Band>(wavelengths.length);
        findSpectralBands();
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

    public Band getBand(int i) {
        return bandList.get(i);
    }

    private void findSpectralBands() {
        search:
        for (final double wavelength : wavelengths) {
            for (final Band band : product.getBands()) {
                if (band.getSpectralBandIndex() != -1) {
                    if (wavelength == band.getSpectralWavelength()) {
                        bandList.add(band);
                        continue search;
                    }
                }
            }
        }
    }
}
