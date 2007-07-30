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
package org.esa.beam.decisiontree.ui;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.esa.beam.decisiontree.DecisionTreeConfiguration;
import org.esa.beam.decisiontree.DecisionVariable;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.annotations.ParameterDefinitionFactory;

import com.bc.ceres.binding.Factory;
import com.bc.ceres.binding.ValueContainer;

/**
 * Created by marcoz.
 *
 * @author marcoz
 * @version $Revision: $ $Date: $
 */
public class OfewClassificationModel {

	private final Product inputProduct;
	private final DecisionTreeConfiguration configuration;
	private final String classificationProductName;
	private final String indexProductName;
	private final String endmemberProductName;
	private ValueContainer[] variableValueContainer;

	public OfewClassificationModel(Product selectedProduct, Reader reader) throws IOException {
		inputProduct = selectedProduct;
		configuration = loadDecisionTreeConfiguration(reader);
		initValueContainers();
		classificationProductName = inputProduct.getName() + "_klassifikation";
		indexProductName = inputProduct.getName() + "_indizes";
		endmemberProductName = inputProduct.getName() + "_entmischung";
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
        variableValueContainer = new ValueContainer[variables.length];
        
        for (int i = 0; i < variables.length; i++) {
        	variableValueContainer[i] = factory.createObjectBackedValueContainer(variables[i]);
		}
    }
	
	public String[] getBandsWithRoi() {
		Band[] bands = inputProduct.getBands();
		List<String> nameList = new ArrayList<String>(bands.length);
		for (Band band : bands) {
			if (band.isROIUsable()) {
				nameList.add(band.getName());
			}
		}
		return nameList.toArray(new String[nameList.size()]);
	}

	public ValueContainer getParamValueContainer(String name) {
		for (ValueContainer valueContainer : variableValueContainer) {
			if (valueContainer.getValue("name").equals(name)) {
				return valueContainer;
			}
		}
		return null;
	}

	public Product getInputProduct() {
		return inputProduct;
	}
	
	public String getClassificationProductNameSuggestion() {
		return classificationProductName;
	}
	
	public String getEndmemberProductNameSuggestion() {
		return endmemberProductName;
	}

	public String getIndexProductNameSuggestion() {
		return indexProductName;
	}
	
	public DecisionTreeConfiguration getConfiguration() {
		return configuration;
	}

	public ValueContainer[] getVariableValueContainers() {
		return variableValueContainer;
	}
}
