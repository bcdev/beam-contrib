package org.esa.beam.ofew.ui;

import org.esa.beam.framework.datamodel.Product;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 */
class AtmCorrPresenter {

    private Product inputProduct;
    private String[] bandNames;

    private double[] a;
    private double[] b;

    public AtmCorrPresenter(Product inputProduct) {
        this.inputProduct = inputProduct;

        bandNames = inputProduct.getBandNames();
        a = new double[bandNames.length];
        b = new double[bandNames.length];
    }

    public Product getInputProduct() {
        return inputProduct;
    }

    public String[] getBandNames() {
        return bandNames;
    }

    public String getBandName(int i) {
        return bandNames[i];
    }

    public int getBandCount() {
        return bandNames.length;
    }

    public String getOutputProductName() {
        return inputProduct.getName() + "_atmo";
    }

    public double getA(int i) {
        return a[i];
    }

    public double getB(int i) {
        return b[i];
    }

    public String getShortBandName(int i) {
        String name = bandNames[i];
        int pos = name.indexOf("_");
        
        if (pos != -1 && pos < name.length() - 1) {
            pos = name.indexOf("_", pos + 1);
            if (pos != -1) {
                name = name.substring(0, pos);
            }
        }

        return name;
    }
}
