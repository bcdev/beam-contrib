package org.esa.beam.ofew.ui;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.annotations.ParameterDefinitionFactory;
import org.esa.beam.framework.gpf.annotations.Parameter;
import com.bc.ceres.binding.Factory;
import com.bc.ceres.binding.ValueContainer;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 */
class AtmCorrPresenter {

    private static class Coefficients {
        @Parameter(defaultValue = "0.0")
        Double a;
        @Parameter(defaultValue = "0.0")
        Double b;
    }

    private Product inputProduct;
    private String[] bandNames;

    private ValueContainer[] coefficientContainers;

    public AtmCorrPresenter(Product inputProduct) {
        this.inputProduct = inputProduct;

        bandNames = inputProduct.getBandNames();

        initValueContainers();
    }

    private void initValueContainers() {
        final Factory factory = new Factory(new ParameterDefinitionFactory());
        coefficientContainers = new ValueContainer[bandNames.length];

        for (int i = 0; i < bandNames.length; i++) {
            coefficientContainers[i] = factory.createObjectBackedValueContainer(new Coefficients());
        }
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

    public double getSlope(int i) {
        return Double.parseDouble(coefficientContainers[i].getModel("a").getValue().toString());
    }

    public double getIntercept(int i) {
        return Double.parseDouble(coefficientContainers[i].getModel("b").getValue().toString());
    }

    public ValueContainer[] getCoefficientContainers() {
        return coefficientContainers;
    }

    public ValueContainer getCoefficientContainer(int i) {
        return coefficientContainers[i];
    }

    public String getDisplayBandName(int i) {
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
