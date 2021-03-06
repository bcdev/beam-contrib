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

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.PropertyDescriptorFactory;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.binding.ValueSet;
import org.esa.beam.decisiontree.DecisionTreeConfiguration;
import org.esa.beam.decisiontree.DecisionVariable;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Mask;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductNodeGroup;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.ParameterDescriptorFactory;
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
public class ClassificationModel {

	public static class Session {
		private double[] values;
	}

	@Parameter(validator=ProductNameValidator.class, label="Klassifikations-Product")
	private String classification;
	@Parameter(validator=ProductNameValidator.class, label="Index-Product")
	private String index;
	@Parameter(validator=ProductNameValidator.class, label="Endmember-Product")
	private String endmember;
	@Parameter
	private String maskName;
	@Parameter
	private boolean useRoi;
	private final DecisionTreeConfiguration configuration;
	private final Product inputProduct;
	
	private PropertySet[] variableVC;
	private PropertySet modelVC;
	private Session session;

	public ClassificationModel(Product selectedProduct, Reader reader, Session session) throws IOException {
		inputProduct = selectedProduct;
		this.session = session;
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
        PropertyDescriptorFactory descriptorFactory = new ParameterDescriptorFactory();
        DecisionVariable[] variables = configuration.getVariables();
        variableVC = new PropertySet[variables.length];
        
        for (int i = 0; i < variables.length; i++) {
        	variableVC[i] = PropertyContainer.createObjectBacked(variables[i], descriptorFactory);
		}
        if (session.values == null) {
        	session.values = new double[variables.length];
        } else {
        	for (int i = 0; i < variables.length; i++) {
        		try {
					variableVC[i].setValue("value", session.values[i]);
				} catch (IllegalArgumentException e) {
					//ignore
				}
        	}
        }
        modelVC = PropertyContainer.createObjectBacked(this, descriptorFactory);
        
        String[] maskNames = getMaskNames();
		ValueSet valueSet = new ValueSet(maskNames);
        PropertyDescriptor valueDescriptor = modelVC.getDescriptor("maskName");
        valueDescriptor.setValueSet(valueSet);
		if (useRoi) {
		    valueDescriptor.setDefaultValue(maskName);
		}
    }
	
	private String[] getMaskNames() {
        ProductNodeGroup<Mask> maskGroup = inputProduct.getMaskGroup();
        String[] maskNames = maskGroup.getNodeNames();
		if (maskNames.length > 0) {
			useRoi = true;
			maskName = maskNames[0];
		}
		return maskNames;
	}

	public PropertySet getParamValueContainer(String name) {
		for (PropertySet valueContainer : variableVC) {
			if (valueContainer.getValue("name").equals(name)) {
				return valueContainer;
			}
		}
		return null;
	}
	
	public PropertySet getModelValueContainer() {
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
	
	public String getMaskName() {
		return maskName;
	}
	
	public boolean useRoi() {
		return useRoi;
	}
	
	public DecisionTreeConfiguration getConfiguration() {
		return configuration;
	}

	public PropertySet[] getVariableValueContainers() {
		return variableVC;
	}

	public void persistSession() {
		DecisionVariable[] variables = configuration.getVariables();
		for (int i = 0; i < variables.length; i++) {
			session.values[i] = variables[i].getValue();
        }
	}

	
}
