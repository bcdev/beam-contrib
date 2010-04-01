package org.esa.beam.ofew.ui;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.ParameterDescriptorFactory;
import org.esa.beam.ofew.ProductNameValidator;

import com.bc.ceres.binding.ClassFieldDescriptorFactory;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.ValueContainer;

/**
 * Presenter for OFEW atmospheric correction form.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 */
class AtmCorrModel {

	public static class Session {
		private CoefficientPair[] values;
		
		private double getCoefficientA(int i) {
			return values[i].a;
		}
		
		private double getCoefficientB(int i) {
			return values[i].b;
		}
		
		private void setCoefficientA(int i, double value) {
			values[i].a = value;
		}
		
		private void setCoefficientB(int i, double value) {
			values[i].b = value;
		}
	}

	private static class CoefficientPair {
        @Parameter
        double a;
        @Parameter
        double b;
        
		public CoefficientPair(double a, double b) {
			this.a = a;
			this.b = b;
		}
    }

    @Parameter(validator= ProductNameValidator.class, label="Ausgabe-Product")
    String targetProductName;

    private Product sourceProduct;
    private Band[] sourceBands;

    private ValueContainer[] coefficientPairContainers;
    private ValueContainer targetProductNameContainer;
    private Session session;

    public AtmCorrModel(Product sourceProduct, Band[] sourceBands, Session session) {
        this.sourceProduct = sourceProduct;
        this.sourceBands = sourceBands;
		this.session = session;

		ClassFieldDescriptorFactory descriptorFactory = new ParameterDescriptorFactory();
        try {
            coefficientPairContainers = new ValueContainer[sourceBands.length];
            
            if (session.values == null) {
            	session.values = new CoefficientPair[sourceBands.length];
            	for (int i = 0; i < sourceBands.length; i++) {
            		session.values[i] = new CoefficientPair(1.0, 0.0);
            	}
            }
            for (int i = 0; i < sourceBands.length; i++) {
                CoefficientPair coefficientPair = new CoefficientPair(session.getCoefficientA(i), session.getCoefficientB(i));
				coefficientPairContainers[i] = ValueContainer.createObjectBacked(coefficientPair, descriptorFactory);
            }
            
            targetProductNameContainer = ValueContainer.createObjectBacked(this, descriptorFactory);
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

	public void persistSession() {
		for (int i = 0; i < sourceBands.length; i++) {
			session.setCoefficientA(i, getCoefficientA(i));
			session.setCoefficientB(i, getCoefficientB(i));
        }
	}

}
