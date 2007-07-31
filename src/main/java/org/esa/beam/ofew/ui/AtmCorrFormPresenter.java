package org.esa.beam.ofew.ui;

import com.bc.ceres.binding.Factory;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.ValueContainer;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.ParameterDefinitionFactory;
import org.esa.beam.ofew.ProductNameValidator;

/**
 * Presenter for OFEW atmospheric correction form.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 */
class AtmCorrFormPresenter {

    private static class CoefficientPair {
        @Parameter
        double a;
        @Parameter
        double b;
    }

    @Parameter(validator= ProductNameValidator.class, label="Ausgabe-Product")
    String outputProductName;

    private Product inputProduct;
    private Band[] inputBands;

    private ValueContainer[] coefficientPairContainers;
    private ValueContainer outputProductNameContainer;

    public AtmCorrFormPresenter(Product inputProduct, Band[] inputBands) {
        this.inputProduct = inputProduct;
        this.inputBands = inputBands;

        final Factory factory = new Factory(new ParameterDefinitionFactory());

        try {
            coefficientPairContainers = new ValueContainer[inputBands.length];
            for (int i = 0; i < inputBands.length; i++) {
                coefficientPairContainers[i] = factory.createObjectBackedValueContainer(new CoefficientPair());
                coefficientPairContainers[i].setValue("a", 1.0);
                coefficientPairContainers[i].setValue("b", 0.0);
            }

            outputProductNameContainer = factory.createObjectBackedValueContainer(this);
            outputProductNameContainer.setValue("outputProductName", inputProduct.getName() + "_atmo");
        } catch (ValidationException e) {
            // ignore, can never happen
        }
    }

    public String getInputProductName() {
        return inputProduct.getName();
    }

    public String getOutputProductName() {
        return (String) outputProductNameContainer.getValue("outputProductName");
    }

    public ValueContainer getOutputProductNameContainer() {
        return outputProductNameContainer;
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
        return inputBands.length;
    }

    public String getBandName(int i) {
        return inputBands[i].getName();
    }

    public String getDisplayBandName(int i) {
        String name = inputBands[i].getName();
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
