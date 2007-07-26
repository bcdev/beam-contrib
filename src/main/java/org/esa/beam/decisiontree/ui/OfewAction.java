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

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.SubProgressMonitor;
import com.bc.ceres.swing.progress.DialogProgressMonitor;

import org.esa.beam.decisiontree.DecisionTreeConfiguration;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.datamodel.ROIDefinition;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.operators.common.BandArithmeticOp;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.command.ExecCommand;
import org.esa.beam.visat.VisatApp;

import java.awt.Dialog;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

/**
 * Noise reduction action.
 *
 * @author Marco Peters
 * @author Ralf Quast
 * @version $Revision:$ $Date:$
 */
public class OfewAction extends ExecCommand {

    static List<String> CHRIS_TYPES;

    static {
        CHRIS_TYPES = new ArrayList<String>();
        Collections.addAll(CHRIS_TYPES, "CHRIS_M1", "CHRIS_M2", "CHRIS_M3", "CHRIS_M3A", "CHRIS_M4", "CHRIS_M5");
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
        
        final OfewPresenter presenter = new OfewPresenter(selectedProduct, configuration);
        OfewPanel ofewPanel = new OfewPanel(presenter);
		dialog.setContent(ofewPanel);

        if (dialog.show() == ModalDialog.ID_OK) {
            final DialogProgressMonitor pm = new DialogProgressMonitor(VisatApp.getApp().getMainFrame(),
                                                                       "OFEW Klassifikation",
                                                                       Dialog.ModalityType.APPLICATION_MODAL);

            ofewPanel.postActionEvent();
            try {
                performOfewAction(presenter, pm);
            } catch (OperatorException e) {
                dialog.showErrorDialog(e.getMessage());
                VisatApp.getApp().getLogger().log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    @Override
    public void updateState() {
        final Product selectedProduct = VisatApp.getApp().getSelectedProduct();
        final boolean enabled = selectedProduct != null;
        // TODO LandSat only ???
        // && CHRIS_TYPES.contains(selectedProduct.getProductType());
        setEnabled(enabled);
    }

    private void performOfewAction(OfewPresenter presenter, ProgressMonitor pm)
            throws OperatorException {
        try {
            pm.beginTask("Performing OFEW Classification", 42);
            
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
            
            Map<String, Object> classificationParameter = getClassificationParameter(presenter);
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
		
		String[] sourceBandNames = {"band1", "band2", "band3",
				"band4", "band5", "band6"};
		parameter.put("sourceBandNames", sourceBandNames);
		parameter.put("unmixingModelName", "Constrained LSU");
		parameter.put("targetBandNameSuffix", "");
		return parameter;
	}
	
	private Map<String, Object> getClassificationParameter(OfewPresenter presenter) {
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("configuration", presenter.getConfiguration());
		parameter.put("roi", presenter.getInputProduct().getBand("band4").getROIDefinition());
		return parameter;
	}
}
