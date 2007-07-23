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
package org.esa.beam.gpf.decisiontree;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.AbstractOperator;
import org.esa.beam.framework.gpf.AbstractOperatorSpi;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.Raster;
import org.esa.beam.framework.gpf.Tile;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.framework.gpf.annotations.TargetProduct;

import com.bc.ceres.core.ProgressMonitor;

public class DecisionTreeOp extends AbstractOperator {

    public static final int FLAG_CLEAR = 1;
    public static final int FLAG_SNOW = 2;
    public static final int FLAG_DENSE_CLOUD = 4;
    public static final int FLAG_THIN_CLOUD = 8;

    public static final String DECISION_BAND = "blue_cloud";

    private DecisionNode rootNode;

    @SourceProduct(alias="input")
    private Product sourceProduct;
    @TargetProduct
    private Product targetProduct;
    @Parameter
    private DecisionNode[] nodes;
    
    public DecisionTreeOp(OperatorSpi spi) {
        super(spi);
    }

    @Override
	protected Product initialize(ProgressMonitor pm) throws OperatorException {
    	buildDecisionTree();
        targetProduct = new Product("name", "type",
        		sourceProduct.getSceneRasterWidth(), sourceProduct.getSceneRasterHeight());
        
        // create and add the flags dataset
        Band cloudFlagBand = targetProduct.addBand(DECISION_BAND, ProductData.TYPE_UINT8);
        cloudFlagBand.setDescription("decisions");

        return targetProduct;
    }

	private void buildDecisionTree() throws OperatorException {
		Map<String, DecisionNode> nodeMap = new HashMap<String, DecisionNode>(nodes.length);
		for (DecisionNode decisionNode : nodes) {
			nodeMap.put(decisionNode.name, decisionNode);
		}
		for (DecisionNode decisionNode : nodes) {
			decisionNode.initNode(sourceProduct);
			if (!decisionNode.isLeaf) {
				decisionNode.yesNode = nodeMap.get(decisionNode.yes);
				if (decisionNode.yesNode == null) {
					throw new OperatorException("Node " + decisionNode.name + " has not existing yes node: " + decisionNode.yes);
				}
				decisionNode.yesNode.parent = decisionNode;
				decisionNode.noNode = nodeMap.get(decisionNode.no);
				if (decisionNode.noNode == null) {
					throw new OperatorException("Node " + decisionNode.name + " has not existing no node: " + decisionNode.no);
				}
				decisionNode.noNode.parent = decisionNode;
			}
		}
		rootNode = nodes[0];
		while (rootNode.parent != null) {
			rootNode = rootNode.parent;
		}
	}

	@Override
    public void computeBand(Raster targetRaster,
            ProgressMonitor pm) throws OperatorException {
    	
    	Rectangle rect = targetRaster.getRectangle();
        final int size = rect.height * rect.width;
        pm.beginTask("Processing frame...", size);
        try {
        	for (DecisionNode decisionNode : nodes) {
        		decisionNode.data = new boolean[size];
        		sourceProduct.readBitmask(rect.x, rect.y, rect.width, rect.height,
        				decisionNode.term, decisionNode.data, ProgressMonitor.NULL);
        	}
            byte[] decisions = (byte[]) targetRaster.getDataBuffer().getElems();

            for (int i = 0; i < size; i++) {
                DecisionNode node = rootNode;
                while (!node.isLeaf) {
					if (node.data[i]) {
						node = node.yesNode;
					} else {
						node = node.noNode;
					}
				}
                decisions[i] = node.value;
                pm.worked(1);
            }
        } catch (IOException e) {
        	throw new OperatorException("Couldn't load bitmasks", e);
		} finally {
            pm.done();
        }
    }

    public static class Spi extends AbstractOperatorSpi {
        public Spi() {
            super(DecisionTreeOp.class, "DecisionTree");
        }
    }
}