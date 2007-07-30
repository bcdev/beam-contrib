package org.esa.beam.ofew.ui;

import com.bc.ceres.binding.Factory;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.ValueContainer;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.ParameterDefinitionFactory;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 */
class AtmCorrPresenter {

    private static class CoefficientPair {

        @Parameter(defaultValue = "1.0")
        Double a;
        @Parameter(defaultValue = "0.0")
        Double b;

    }

    private static class Product {

        @Parameter
        String name;
    }

    private String inputProduct;
    private String[] bandNames;

    private ValueContainer[] coefficientPairContainers;
    private ValueContainer outputProductContainer;

    public AtmCorrPresenter(String inputProduct, String[] bandNames) throws ValidationException {
        this.inputProduct = inputProduct;
        this.bandNames = bandNames;

        final Factory factory = new Factory(new ParameterDefinitionFactory());

        coefficientPairContainers = new ValueContainer[bandNames.length];
        for (int i = 0; i < bandNames.length; i++) {
            coefficientPairContainers[i] = factory.createObjectBackedValueContainer(new CoefficientPair());
        }

        outputProductContainer = factory.createObjectBackedValueContainer(new Product());
        outputProductContainer.setValue("name", inputProduct + "_atmo");
    }

    public String getInputProduct() {
        return inputProduct;
    }

    public String getOutputProduct() {
        return (String) outputProductContainer.getValue("name");
    }

    public ValueContainer getOutputProductContainer() {
        return outputProductContainer;
    }

    public ValueContainer getCoefficientPairContainer(int i) {
        return coefficientPairContainers[i];
    }

    public double getCoefficientA(int i) {
        return (Double) coefficientPairContainers[i].getValue("a");
    }

    public double getCoefficientB(int i) {
        return (Double) coefficientPairContainers[i].getValue("b");
    }

    public int getBandCount() {
        return bandNames.length;
    }

    public String getBandName(int i) {
        return bandNames[i];
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
