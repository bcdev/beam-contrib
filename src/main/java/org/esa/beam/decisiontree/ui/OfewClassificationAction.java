/* $Id: $
 *
 * Copyright (C) 2002-2007 by Brockmann Consult
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

import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.media.jai.ROI;

import org.esa.beam.decisiontree.Decision;
import org.esa.beam.decisiontree.DecisionTreeConfiguration;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.dataop.barithm.BandArithmetic;
import org.esa.beam.framework.dataop.barithm.RasterDataEvalEnv;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.operators.common.BandArithmeticOp;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.command.ExecCommand;
import org.esa.beam.visat.VisatApp;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.SubProgressMonitor;
import com.bc.ceres.swing.progress.DialogProgressMonitor;
import com.bc.jexp.EvalEnv;
import com.bc.jexp.EvalException;
import com.bc.jexp.Symbol;
import com.bc.jexp.impl.AbstractSymbol;

/**
 * Noise reduction action.
 *
 * @author Marco Peters
 * @author Ralf Quast
 * @version $Revision:$ $Date:$
 */
public class OfewClassificationAction extends ExecCommand {

    private final String[] sourceBandNames;

	public OfewClassificationAction() {
		sourceBandNames = new String[] { "band1", "band2", "band3", "band4",
				"band5", "band6" };
	}

	@Override
    public void actionPerformed(CommandEvent commandEvent) {
        final Product selectedProduct = VisatApp.getApp().getSelectedProduct();

        final ModalDialog dialog = new ModalDialog(VisatApp.getApp().getMainFrame(),
                "OFEW Klassifikation",
                ModalDialog.ID_OK_CANCEL_HELP,
                "ofewClassificationTool");
        
        DecisionTreeConfiguration configuration;
        try {
        	InputStream inputStream = this.getClass().getResourceAsStream("ofew_dt.xml");
        	Reader reader = new InputStreamReader(inputStream);
        	configuration = new DecisionTreeConfiguration(reader);
        } catch (Exception e) {
        	dialog.showErrorDialog(e.getMessage());
            VisatApp.getApp().getLogger().log(Level.SEVERE, e.getMessage(), e);
            return;
        }
        
        final OfewClassificationPresenter presenter = new OfewClassificationPresenter(selectedProduct, configuration);
        OfewClassificationPanel ofewClassificationPanel = new OfewClassificationPanel(presenter);
		dialog.setContent(ofewClassificationPanel);

        if (dialog.show() == ModalDialog.ID_OK) {
            final DialogProgressMonitor pm = new DialogProgressMonitor(VisatApp.getApp().getMainFrame(),
                                                                       "OFEW Klassifikation",
                                                                       Dialog.ModalityType.APPLICATION_MODAL);

            ofewClassificationPanel.postActionEvent();
            String roiBandName = ofewClassificationPanel.getRoiBandName();
            try {
                performOfewAction(presenter, roiBandName, pm);
            } catch (OperatorException e) {
                dialog.showErrorDialog(e.getMessage());
                VisatApp.getApp().getLogger().log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    @Override
    public void updateState() {
        final Product selectedProduct = VisatApp.getApp().getSelectedProduct();
        boolean enabled = false;
        if (selectedProduct != null) {
        	enabled = true;
        	for (String bandName : sourceBandNames) {
        		if (!selectedProduct.containsBand(bandName)) {
        			enabled = false;
        		}
			}
        }
        setEnabled(enabled);
    }

    private void performOfewAction(OfewClassificationPresenter presenter, String roiBandName, ProgressMonitor pm)
            throws OperatorException {
        try {
            pm.beginTask("Performing OFEW Classification", 30);
            
            Map<String, Object> unmixingParameter = getUnmixingParameter();
            final Product endmemberProduct = GPF.createProduct("SpectralUnmixing",
					unmixingParameter, presenter.getInputProduct(),
					new SubProgressMonitor(pm, 10));

            Map<String, Object> indexParameter = getIndexParameter();
            Map<String, Product> indexInputProducts = new HashMap<String, Product>();
            indexInputProducts.put("l5", presenter.getInputProduct());
            indexInputProducts.put("em", endmemberProduct);
            final Product indexProduct = GPF.createProduct("BandArithmetic",
					indexParameter, indexInputProducts,
					new SubProgressMonitor(pm, 10));
            
            Map<String, Object> classificationParameter = getClassificationParameter(presenter, roiBandName);
            Map<String, Product> classificationInputProducts = new HashMap<String, Product>();
            classificationInputProducts.put("f3", presenter.getInputProduct());
            classificationInputProducts.put("f1", endmemberProduct);
            classificationInputProducts.put("f2", indexProduct);
            final Product classificationProducts = GPF.createProduct("DecisionTree",
            		classificationParameter, classificationInputProducts,
					new SubProgressMonitor(pm, 10));
            

            
            VisatApp.getApp().addProduct(endmemberProduct);
            VisatApp.getApp().addProduct(indexProduct);
            VisatApp.getApp().addProduct(classificationProducts);
        } catch (URISyntaxException e) {
			throw new OperatorException(e);
		} finally {
            pm.done();
        }
    }

	private Map<String, Object> getIndexParameter() {
		Map<String, Object> parameter = new HashMap<String, Object>();
		BandArithmeticOp.BandDescriptor[] bandDesc = new BandArithmeticOp.BandDescriptor[4];
		bandDesc[0] = new BandArithmeticOp.BandDescriptor();
		bandDesc[0].name = "ndvi";
		bandDesc[0].expression = "($l5.band4 - $l5.band3)/($l5.band4 + $l5.band3)";
		bandDesc[0].type = ProductData.TYPESTRING_FLOAT32;
		
		bandDesc[1] = new BandArithmeticOp.BandDescriptor();
		bandDesc[1].name = "Steigung_3_4";
		bandDesc[1].expression = "($l5.band3 - $l5.band4)/(0.66-0.835)";
		bandDesc[1].type = ProductData.TYPESTRING_FLOAT32;
		
		bandDesc[2] = new BandArithmeticOp.BandDescriptor();
		bandDesc[2].name = "Steigung_4_5";
		bandDesc[2].expression = "($l5.band4 - $l5.band5)/(0.835-1.65)";
		bandDesc[2].type = ProductData.TYPESTRING_FLOAT32;
		
		bandDesc[3] = new BandArithmeticOp.BandDescriptor();
		bandDesc[3].name = "schlick_corr";
		bandDesc[3].expression = "($em.Sand_wc < 0.0) ? $em.Sand_wc + $em.schlick : $em.schlick";
		bandDesc[3].type = ProductData.TYPESTRING_FLOAT32;
		parameter.put("bandDescriptors", bandDesc);
		return parameter;
	}

	private Map<String, Object> getUnmixingParameter() throws URISyntaxException {
		Map<String, Object> parameter = new HashMap<String, Object>();
		URL resource = this.getClass().getResource("em.csv");
		File file = new File(resource.toURI());
		parameter.put("endmemberFile", file);
		
		parameter.put("sourceBandNames", sourceBandNames);
		parameter.put("unmixingModelName", "Constrained LSU");
		parameter.put("targetBandNameSuffix", "");
		return parameter;
	}
	
	private Map<String, Object> getClassificationParameter(OfewClassificationPresenter presenter, String roiBandName) throws OperatorException {
		Map<String, Object> parameter = new HashMap<String, Object>();
		DecisionTreeConfiguration configuration = presenter.getConfiguration();
		if (roiBandName.length()>0) {
			registerRoiSymbol(presenter.getInputProduct().getBand(roiBandName), ProgressMonitor.NULL);
			Decision inRoiDecision = new Decision("inRoi", "inROI");
			inRoiDecision.setYesDecision(configuration.getRootDecisions());
			inRoiDecision.setNoClass(configuration.getClass("nodata"));
			configuration.setRootDecisions(inRoiDecision);
		}
		parameter.put("configuration", configuration);
		return parameter;
	}
	
	private void registerRoiSymbol(Band band, ProgressMonitor pm) throws OperatorException {
		if (band.isROIUsable()) {
			final String symbolName = "inROI";
			try {
				final ROI roi = band.createROI(pm);
				Symbol s = new AbstractSymbol.B(symbolName) {
					public boolean evalB(EvalEnv env) throws EvalException {
						RasterDataEvalEnv eEnv = (RasterDataEvalEnv) env;
						return roi.contains(eEnv.getPixelX(), eEnv.getPixelY());
					}
				};
				BandArithmetic.registerSymbol(s);
			} catch (IOException e) {
				throw new OperatorException("Couldn't create ROI", e);
			}
		}
	}
}
