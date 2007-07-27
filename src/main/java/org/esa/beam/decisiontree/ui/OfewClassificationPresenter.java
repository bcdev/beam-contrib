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

import org.esa.beam.decisiontree.DecisionTreeConfiguration;
import org.esa.beam.decisiontree.DecisionVariable;
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
public class OfewClassificationPresenter {

	private final Product inputProduct;
	private final DecisionTreeConfiguration configuration;
	private ValueContainer[] variableValueContainer;

	public OfewClassificationPresenter(Product selectedProduct, DecisionTreeConfiguration configuration) {
		this.inputProduct = selectedProduct;
		this.configuration = configuration;
		
		initValueContainers();
	}
	
	private void initValueContainers() {
        final Factory factory = new Factory(new ParameterDefinitionFactory());
        DecisionVariable[] variables = configuration.getVariables();
        variableValueContainer = new ValueContainer[variables.length];
        
        for (int i = 0; i < variables.length; i++) {
        	variableValueContainer[i] = factory.createObjectBackedValueContainer(variables[i]);
		}
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
	
	public DecisionTreeConfiguration getConfiguration() {
		return configuration;
	}

	public ValueContainer[] getVariableValueContainers() {
		return variableValueContainer;
	}
}
