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
    String targetProductName;

    private Product sourceProduct;
    private Band[] sourceBands;

    private ValueContainer[] coefficientPairContainers;
    private ValueContainer targetProductNameContainer;

    public AtmCorrFormPresenter(Product sourceProduct, Band[] sourceBands) {
        this.sourceProduct = sourceProduct;
        this.sourceBands = sourceBands;

        final Factory factory = new Factory(new ParameterDefinitionFactory());

        try {
            coefficientPairContainers = new ValueContainer[sourceBands.length];
            for (int i = 0; i < sourceBands.length; i++) {
                coefficientPairContainers[i] = factory.createObjectBackedValueContainer(new CoefficientPair());
                coefficientPairContainers[i].setValue("a", 1.0);
                coefficientPairContainers[i].setValue("b", 0.0);
            }

            targetProductNameContainer = factory.createObjectBackedValueContainer(this);
            targetProductNameContainer.setValue("targetProductName", sourceProduct.getName() + "_atmo");
        } catch (ValidationException e) {
            // ignore, can never happen
        }
    }

    public String getSourceProductName() {
        return sourceProduct.getName();
    }

    public String getTargetProductName() {
        return (String) targetProductNameContainer.getValue("targetProductName");
    }

    public ValueContainer getTargetProductNameContainer() {
        return targetProductNameContainer;
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
        return sourceBands.length;
    }

    public String getBandName(int i) {
        return sourceBands[i].getName();
    }

    public String getDisplayBandName(int i) {
        String name = sourceBands[i].getName();
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
