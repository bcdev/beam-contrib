/*
 * $Id: $
 *
 * Copyright (C) 2007 by Brockmann Consult (info@brockmann-consult.de)
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
package org.esa.beam.ofew.ui;

import com.bc.ceres.binding.Factory;
import com.bc.ceres.binding.ValueContainer;
import com.bc.ceres.binding.ValueDefinition;
import com.bc.ceres.binding.ValueSet;
import org.esa.beam.decisiontree.DecisionTreeConfiguration;
import org.esa.beam.decisiontree.DecisionVariable;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.ParameterDefinitionFactory;
import org.esa.beam.ofew.ProductNameValidator;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by marcoz.
 *
 * @author marcoz
 * @version $Revision: $ $Date: $
 */
public class OfewClassificationModel {

	@Parameter(validator=ProductNameValidator.class, label="Klassifikations-Product")
	private String classification;
	@Parameter(validator=ProductNameValidator.class, label="Index-Product")
	private String index;
	@Parameter(validator=ProductNameValidator.class, label="Endmember-Product")
	private String endmember;
	@Parameter
	private String roiBandName;
	@Parameter
	private boolean useRoi;
	private final DecisionTreeConfiguration configuration;
	private final Product inputProduct;
	
	private ValueContainer[] variableVC;
	private ValueContainer modelVC;

	public OfewClassificationModel(Product selectedProduct, Reader reader) throws IOException {
		inputProduct = selectedProduct;
		configuration = loadDecisionTreeConfiguration(reader);
		classification = inputProduct.getName() + "_klassifikation";
		index = inputProduct.getName() + "_indizes";
		endmember = inputProduct.getName() + "_entmischung";
		initValueContainers();
	}
	
	private DecisionTreeConfiguration loadDecisionTreeConfiguration(Reader reader) throws IOException {
      	DecisionTreeConfiguration newConfiguration = DecisionTreeConfiguration.fromXML(reader);
      	if (newConfiguration == null) {
      		throw new IOException("Could not read DecisionTreeConfiguration config file.");
      	} else {
      		return newConfiguration;
      	}
	}
	
	private void initValueContainers() {
        final Factory factory = new Factory(new ParameterDefinitionFactory());
        DecisionVariable[] variables = configuration.getVariables();
        variableVC = new ValueContainer[variables.length];
        
        for (int i = 0; i < variables.length; i++) {
        	variableVC[i] = factory.createObjectBackedValueContainer(variables[i]);
		}
        modelVC = factory.createObjectBackedValueContainer(this);
        
        String[] bandsWithRoi = getBandsWithRoi();
		ValueSet valueSet = new ValueSet(bandsWithRoi); 
        ValueDefinition valueDefinition = modelVC.getValueDefinition("roiBandName");
		valueDefinition.setValueSet(valueSet);
		if (useRoi) {
			valueDefinition.setDefaultValue(roiBandName);
		}
    }
	
	private String[] getBandsWithRoi() {
		Band[] bands = inputProduct.getBands();
		List<String> nameList = new ArrayList<String>(bands.length);
		for (Band band : bands) {
			if (band.isROIUsable()) {
				nameList.add(band.getName());
			}
		}
		if (nameList.size() > 0) {
			useRoi = true;
			roiBandName = nameList.get(0);
		}
		return nameList.toArray(new String[nameList.size()]);
	}

	public ValueContainer getParamValueContainer(String name) {
		for (ValueContainer valueContainer : variableVC) {
			if (valueContainer.getValue("name").equals(name)) {
				return valueContainer;
			}
		}
		return null;
	}
	
	public ValueContainer getModelValueContainer() {
		return modelVC;
	}

	public Product getInputProduct() {
		return inputProduct;
	}
	
	public String getClassificationProductName() {
		return classification;
	}
	
	public String getEndmemberProductName() {
		return endmember;
	}

	public String getIndexProductName() {
		return index;
	}
	
	public String getRoiBandName() {
		return roiBandName;
	}
	
	public boolean useRoi() {
		return useRoi;
	}
	
	public DecisionTreeConfiguration getConfiguration() {
		return configuration;
	}

	public ValueContainer[] getVariableValueContainers() {
		return variableVC;
	}

	
}
