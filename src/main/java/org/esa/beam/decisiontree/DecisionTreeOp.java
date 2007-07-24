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
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.AbstractOperator;
import org.esa.beam.framework.gpf.AbstractOperatorSpi;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.Raster;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.framework.gpf.internal.DefaultOperatorContext;
import org.esa.beam.framework.gpf.operators.common.BandArithmeticOp;
import org.esa.beam.util.StringUtils;

import com.bc.ceres.core.ProgressMonitor;

public class DecisionTreeOp extends AbstractOperator {

    public static final String CLASSIFICATION_BAND = "classification";

    @SourceProduct(alias="input")
    private Product sourceProduct;
    @TargetProduct
    private Product targetProduct;
    @Parameter
    private String decisionConfigFile;
    @Parameter
    private DecisionTreeConfiguration configuration;

	private DecisionData[] dds;
	
	private static class DecisionData {
		Decision decision;
		Band band;
	}
    
    public DecisionTreeOp(OperatorSpi spi) {
        super(spi);
    }

    @Override
	protected Product initialize(ProgressMonitor pm) throws OperatorException {
        targetProduct = new Product("name", "type",
        		sourceProduct.getSceneRasterWidth(), sourceProduct.getSceneRasterHeight());
        
        Band classBand = targetProduct.addBand(CLASSIFICATION_BAND, ProductData.TYPE_UINT8);
        classBand.setDescription("decisions");

        if (StringUtils.isNotNullAndNotEmpty(decisionConfigFile)) {
        	try {
        	FileReader reader = new FileReader(decisionConfigFile);
        	configuration = new DecisionTreeConfiguration(reader);
        	} catch (FileNotFoundException e) {
				throw new OperatorException("Could not open config file: "+decisionConfigFile, e);
			} catch (Exception e) {
				throw new OperatorException("Could not parse config file: "+decisionConfigFile, e);
			}
        }
        
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
		
		Map<String, Product> products = new HashMap<String, Product>();
		for (Product product : getContext().getSourceProducts()) {
			products.put(getContext().getIdForSourceProduct(product), product);	
		}
		Product expressionProduct = GPF.createProduct("BandArithmetic", parameters, products, pm);
		DefaultOperatorContext context = (DefaultOperatorContext) getContext();
		context.addSourceProduct("x", expressionProduct);
		
		dds = new DecisionData[decisions.length];
		for (int i = 0; i < decisions.length; i++) {
			DecisionData dd = new DecisionData();
        	dd.decision = decisions[i];
        	dd.band = expressionProduct.getBand("b"+i);
        	dds[i] = dd;	
		}
		
        return targetProduct;
    }

	@Override
    public void computeBand(Raster targetRaster,
            ProgressMonitor pm) throws OperatorException {
    	
    	Rectangle rect = targetRaster.getRectangle();
        final int size = rect.height * rect.width;
        pm.beginTask("Processing frame...", size);
        try {
        	Map<Decision, Raster> rasterMap = new HashMap<Decision, Raster>(dds.length);
        	for (int i = 0; i < dds.length; i++) {
        		DecisionData decisionData = dds[i];
				Raster raster = getRaster(decisionData.band, rect);
				rasterMap.put(decisionData.decision, raster);
        	}
        	
        	for (int y = rect.y; y < rect.y + rect.height; y++) {
				if (pm.isCanceled()) {
					break;
				}
				for (int x = rect.x; x < rect.x+rect.width; x++) {
					Decision decision = configuration.getRootDecisions();
					int value = evaluateDecision(x, y, decision, rasterMap);
					targetRaster.setInt(x, y, value);
				}
				pm.worked(1);
			}
		} finally {
            pm.done();
        }
    }

	private int evaluateDecision(int x, int y, Decision decision, Map<Decision, Raster> rasterMap) {
		Raster raster = rasterMap.get(decision);
		boolean b = raster.getBoolean(x, y);
		if (b) {
			if (decision.getYesDecision() != null) {
				Decision yesDecision = decision.getYesDecision();
				return evaluateDecision(x, y, yesDecision, rasterMap);
			} else {
				return decision.getYesClass().getValue();
			}
		} else {
			if (decision.getNoDecision() != null) {
				Decision noDecision = decision.getNoDecision();
				return evaluateDecision(x, y, noDecision, rasterMap);
			} else {
				return decision.getNoClass().getValue();
			}
		}
	}

	
	public static class Spi extends AbstractOperatorSpi {
        public Spi() {
            super(DecisionTreeOp.class, "DecisionTree");
        }
    }
}