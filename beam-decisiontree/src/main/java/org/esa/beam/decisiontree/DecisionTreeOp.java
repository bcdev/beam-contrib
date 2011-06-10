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

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Mask;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.datamodel.ProductNodeGroup;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.Tile;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.gpf.operators.standard.BandMathsOp;
import org.esa.beam.util.ProductUtils;
import org.esa.beam.util.StringUtils;

import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements a classification based on a decision tree.
 */
@OperatorMetadata(alias = "DecisionTree",
                  internal = true,
                  version = "1.0",
                  authors = "Marco Zuehlke",
                  copyright = "(c) 2007 by Brockmann Consult",
                  description = "Performs a classification based on a decision tree.")
public class DecisionTreeOp extends Operator {

    public static final String CLASSIFICATION_BAND = "classification";

    @SourceProducts
    private Product[] sourceProducts;
    @TargetProduct
    private Product targetProduct;
    @Parameter
    private String decisionConfigFile;
    @Parameter(domConverter = DecisionTreeDomConverter.class)
    private DecisionTreeConfiguration configuration;
    @Parameter(defaultValue = CLASSIFICATION_BAND)
    private String bandName;

    private DecisionData[] dds;

    private static class DecisionData {

        Decision decision;
        Band band;
    }

    @Override
    public void initialize() throws OperatorException {
        Product sourceProduct = sourceProducts[0];
        targetProduct = new Product("name", "type",
                                    sourceProduct.getSceneRasterWidth(), sourceProduct.getSceneRasterHeight());

        if (StringUtils.isNotNullAndNotEmpty(decisionConfigFile)) {
            try {
                FileReader reader = new FileReader(decisionConfigFile);
                configuration = DecisionTreeConfiguration.fromXML(reader);
                if (configuration == null) {
                    throw new OperatorException("Could not parse config file: " + decisionConfigFile);
                }
            } catch (FileNotFoundException e) {
                throw new OperatorException("Could not open config file: " + decisionConfigFile, e);
            }
        }

        targetProduct.addBand(bandName, ProductData.TYPE_UINT8);
        sourceProduct.transferGeoCodingTo(targetProduct, null);
        ProductUtils.copyMetadata(sourceProduct, targetProduct);

        addMasks();


        Map<String, Object> parameters = new HashMap<String, Object>();
        Decision[] decisions = configuration.getAllDecisions();

        BandMathsOp.BandDescriptor[] bandDescriptions = new BandMathsOp.BandDescriptor[decisions.length];
        for (int i = 0; i < decisions.length; i++) {
            BandMathsOp.BandDescriptor bandDescriptor = new BandMathsOp.BandDescriptor();
            bandDescriptor.name = "b" + i;
            bandDescriptor.expression = decisions[i].getTerm();
            bandDescriptor.type = ProductData.TYPESTRING_INT8;
            bandDescriptions[i] = bandDescriptor;
        }
        parameters.put("targetBands", bandDescriptions);

        DecisionVariable[] decisionVariables = configuration.getVariables();
        if (decisionVariables != null) {
            BandMathsOp.Variable[] variables = new BandMathsOp.Variable[decisionVariables.length];
            for (int i = 0; i < decisionVariables.length; i++) {
                BandMathsOp.Variable variable = new BandMathsOp.Variable();
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
        Product expressionProduct = GPF.createProduct("BandMaths", parameters, products);

        dds = new DecisionData[decisions.length];
        for (int i = 0; i < decisions.length; i++) {
            DecisionData decisionData = new DecisionData();
            decisionData.decision = decisions[i];
            decisionData.band = expressionProduct.getBand("b" + i);
            dds[i] = decisionData;
        }
    }

    private void addMasks() {
        Classification[] classes = configuration.getClasses();
        int width = targetProduct.getSceneRasterWidth();
        int height = targetProduct.getSceneRasterHeight();
        ProductNodeGroup<Mask> maskGroup = targetProduct.getMaskGroup();
        ProductNodeGroup<Mask> overlayMaskGroup = targetProduct.getBand(bandName).getOverlayMaskGroup();
        for (Classification aClass : classes) {
            Mask mask = Mask.BandMathsType.create(aClass.getName(), "", width, height,
                                                  bandName + " == " + aClass.getValue(), aClass.getColor(),
                                                  0.0f);
            maskGroup.add(mask);
            overlayMaskGroup.add(mask);
        }
    }

    @Override
    public void computeTile(Band band, Tile targetTile, ProgressMonitor pm) throws OperatorException {

        Rectangle rect = targetTile.getRectangle();
        pm.beginTask("Processing frame...", rect.height);
        try {
            Map<Decision, Tile> sourceTileMap = new HashMap<Decision, Tile>(dds.length);
            for (DecisionData decisionData : dds) {
                Tile tile = getSourceTile(decisionData.band, rect);
                sourceTileMap.put(decisionData.decision, tile);
            }

            for (int y = rect.y; y < rect.y + rect.height; y++) {
                if (pm.isCanceled()) {
                    break;
                }
                for (int x = rect.x; x < rect.x + rect.width; x++) {
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
            super(DecisionTreeOp.class);
        }
    }
}