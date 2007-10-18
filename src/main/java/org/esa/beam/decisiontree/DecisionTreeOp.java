/*
 * $Id: BlueBandOp.java,v 1.1 2007/03/27 12:52:22 marcoz Exp $
 *
 * Copyright (C) 2006 by Brockmann Consult (info@brockmann-consult.de)
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
package org.esa.beam.decisiontree;

import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.BitmaskDef;
import org.esa.beam.framework.datamodel.BitmaskOverlayInfo;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.Tile;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.framework.gpf.operators.common.BandArithmeticOp;
import org.esa.beam.util.StringUtils;

import com.bc.ceres.core.ProgressMonitor;


public class DecisionTreeOp extends Operator {

    public static final String CLASSIFICATION_BAND = "classification";

    @SourceProducts
    private Product[] sourceProducts;
    @TargetProduct
    private Product targetProduct;
    @Parameter
    private String decisionConfigFile;
    @Parameter
    private DecisionTreeConfiguration configuration;
    @Parameter
    private String bandName = CLASSIFICATION_BAND;

	private DecisionData[] dds;
	
	private static class DecisionData {
		Decision decision;
		Band band;
	}
    
    @Override
	public Product initialize() throws OperatorException {
        targetProduct = new Product("name", "type",
        		sourceProducts[0].getSceneRasterWidth(), sourceProducts[0].getSceneRasterHeight());
        
        if (StringUtils.isNotNullAndNotEmpty(decisionConfigFile)) {
        	try {
        		FileReader reader = new FileReader(decisionConfigFile);
        		configuration = DecisionTreeConfiguration.fromXML(reader);
        		if (configuration == null) {
        			throw new OperatorException("Could not parse config file: "+decisionConfigFile);
        		}
        	} catch (FileNotFoundException e) {
				throw new OperatorException("Could not open config file: "+decisionConfigFile, e);
			}
        }
        
        targetProduct.addBand(bandName, ProductData.TYPE_UINT8);
        addBitmaskDefs();

        
        Map<String, Object> parameters = new HashMap<String, Object>();
        Decision[] decisions = configuration.getAllDecisions();
        
        BandArithmeticOp.BandDescriptor[] bandDescriptions = new BandArithmeticOp.BandDescriptor[decisions.length];
        for (int i = 0; i < decisions.length; i++) {
        	BandArithmeticOp.BandDescriptor bandDescriptor = new BandArithmeticOp.BandDescriptor();
        	bandDescriptor.name = "b"+i;
			bandDescriptor.expression = decisions[i].getTerm();
			bandDescriptor.type = ProductData.TYPESTRING_BOOLEAN;
			bandDescriptions[i] = bandDescriptor;
    	}
		parameters.put("bandDescriptors", bandDescriptions);
		
		DecisionVariable[] decisionVariables = configuration.getVariables();
		if (decisionVariables != null) {
			BandArithmeticOp.Variable[] variables = new BandArithmeticOp.Variable[decisionVariables.length];
			for (int i = 0; i < decisionVariables.length; i++) {
				BandArithmeticOp.Variable variable = new BandArithmeticOp.Variable();
				variable.name = decisionVariables[i].getName();
				variable.type = ProductData.TYPESTRING_FLOAT32;
				variable.value = Double.toString(decisionVariables[i].getValue());
				variables[i] = variable;
			}
			parameters.put("variables", variables);
		}
		
		Map<String, Product> products = new HashMap<String, Product>();
		for (Product product : sourceProducts) {
			products.put(getSourceProductId(product), product);	
		}
		Product expressionProduct = GPF.createProduct("BandArithmetic", parameters, products);
		addSourceProduct("x", expressionProduct);
		
		dds = new DecisionData[decisions.length];
		for (int i = 0; i < decisions.length; i++) {
			DecisionData dd = new DecisionData();
        	dd.decision = decisions[i];
        	dd.band = expressionProduct.getBand("b"+i);
        	dds[i] = dd;	
		}
		
        return targetProduct;
    }

	private void addBitmaskDefs() {
		Classification[] classes = configuration.getClasses();
		BitmaskOverlayInfo bitmaskOverlayInfo = new BitmaskOverlayInfo();
		for (Classification aClass : classes) {
			BitmaskDef bitmaskDef = new BitmaskDef(aClass.getName(), "",
					bandName + " == " + aClass.getValue(), aClass.getColor(),
					0.0f);
			targetProduct.addBitmaskDef(bitmaskDef);
			bitmaskOverlayInfo.addBitmaskDef(bitmaskDef);
		}
		targetProduct.getBand(bandName).setBitmaskOverlayInfo(bitmaskOverlayInfo);
	}

	@Override
    public void computeTile(Band band, Tile targetTile, ProgressMonitor pm) throws OperatorException {
    	
    	Rectangle rect = targetTile.getRectangle();
        pm.beginTask("Processing frame...", rect.height);
        try {
        	Map<Decision, Tile> sourceTileMap = new HashMap<Decision, Tile>(dds.length);
        	for (int i = 0; i < dds.length; i++) {
        		DecisionData decisionData = dds[i];
				Tile tile = getSourceTile(decisionData.band, rect, pm);
				sourceTileMap.put(decisionData.decision, tile);
        	}
        	
        	for (int y = rect.y; y < rect.y + rect.height; y++) {
				if (pm.isCanceled()) {
					break;
				}
				for (int x = rect.x; x < rect.x+rect.width; x++) {
					Decision decision = configuration.getRootDecisions();
					int value = evaluateDecision(x, y, decision, sourceTileMap);
					targetTile.setSample(x, y, value);
				}
				pm.worked(1);
			}
		} finally {
            pm.done();
        }
    }

	private int evaluateDecision(int x, int y, Decision decision, Map<Decision, Tile> tileMap) {
		Tile tile = tileMap.get(decision);
		boolean b = tile.getSampleBoolean(x, y);
		if (b) {
			if (decision.getYesDecision() != null) {
				Decision yesDecision = decision.getYesDecision();
				return evaluateDecision(x, y, yesDecision, tileMap);
			} else {
				return decision.getYesClass().getValue();
			}
		} else {
			if (decision.getNoDecision() != null) {
				Decision noDecision = decision.getNoDecision();
				return evaluateDecision(x, y, noDecision, tileMap);
			} else {
				return decision.getNoClass().getValue();
			}
		}
	}

	
	public static class Spi extends OperatorSpi {
        public Spi() {
            super(DecisionTreeOp.class, "DecisionTree");
        }
    }
}