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

import com.bc.jexp.EvalEnv;
import com.bc.jexp.EvalException;
import com.bc.jexp.Symbol;
import com.bc.jexp.impl.AbstractSymbol;
import org.esa.beam.decisiontree.Decision;
import org.esa.beam.decisiontree.DecisionTreeConfiguration;
import org.esa.beam.decisiontree.DecisionTreeOp;
import org.esa.beam.decisiontree.DecisionVariable;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Mask;
import org.esa.beam.framework.datamodel.MetadataAttribute;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.dataop.barithm.BandArithmetic;
import org.esa.beam.framework.dataop.barithm.RasterDataEvalEnv;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.framework.ui.diagram.DiagramGraph;
import org.esa.beam.framework.ui.diagram.DiagramGraphIO;
import org.esa.beam.gpf.operators.standard.BandMathsOp;
import org.esa.beam.ofew.SpectralBandFinder;
import org.esa.beam.ofew.ui.ClassificationModel.Session;
import org.esa.beam.unmixing.Endmember;
import org.esa.beam.unmixing.SpectralUnmixingOp;
import org.esa.beam.util.Guardian;
import org.esa.beam.visat.VisatApp;

import javax.media.jai.ROI;
import java.awt.Window;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by marcoz.
 *
 * @author marcoz
 * @version $Revision: $ $Date: $
 */
public class ClassificationDialog extends ModalDialog {

    public static final String TITLE = "OFEW Klassifikation";
    private static final String EM_CSV = "em.csv";
    private static final String OFEW_DT_XML = "ofew_dt.xml";


    private final ClassificationModel model;
    private final ClassificationForm form;
    private final SpectralBandFinder bandFinder;


    public ClassificationDialog(final Window parent,
                                final Product inputProduct, Session session) throws IOException {
        super(parent, TITLE, ModalDialog.ID_OK_CANCEL, null);
        Guardian.assertNotNull("inputProduct", inputProduct);

        InputStream inputStream = this.getClass().getResourceAsStream(OFEW_DT_XML);
        Reader reader = new InputStreamReader(inputStream);
        try {
            model = new ClassificationModel(inputProduct, reader, session);
        } finally {
            reader.close();
        }
        form = new ClassificationForm(model);
        bandFinder = new SpectralBandFinder(inputProduct, SpectralBandFinder.OFEW_WAVELENGTHS);
    }

    @Override
    public int show() {
        setContent(form);
        // form.outputProductName.requestFocus();
        return super.show();
    }

    @Override
    protected boolean verifyUserInput() {
        return form.hasValidValues();
    }

    @Override
    protected void onOK() {

        try {
            model.persistSession();
            performClassification();
        } catch (Exception e) {
            showErrorDialog(e.getMessage());
            VisatApp.getApp().getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        super.onOK();
    }

    private void performClassification() throws Exception {

        Map<String, Object> unmixingParameter = getUnmixingParameter();
        Product landsatProduct = model.getInputProduct();
        String unmixingAlias = OperatorSpi.getOperatorAlias(SpectralUnmixingOp.class);
        final Product endmemberProduct = GPF.createProduct(unmixingAlias, unmixingParameter, landsatProduct);
        endmemberProduct.setName(model.getEndmemberProductName());
        copySensingStartAndStopTime(landsatProduct, endmemberProduct);

        Map<String, Object> indexParameter = getIndexParameter();
        Map<String, Object> classificationParameter = getClassificationParameter();

        Map<String, Product> indexInputProducts = new HashMap<String, Product>();
        indexInputProducts.put("landsat", landsatProduct);
        indexInputProducts.put("endmember", endmemberProduct);
        String bandMathsAlias = OperatorSpi.getOperatorAlias(BandMathsOp.class);
        final Product indexProduct = GPF.createProduct(bandMathsAlias, indexParameter, indexInputProducts);
        indexProduct.setName(model.getIndexProductName());
        copySensingStartAndStopTime(landsatProduct, indexProduct);

        Map<String, Product> classificationInputProducts = new HashMap<String, Product>();
        classificationInputProducts.put("landsat", landsatProduct);
        classificationInputProducts.put("endmember", endmemberProduct);
        classificationInputProducts.put("index", indexProduct);
        String decisiontreeAlias = OperatorSpi.getOperatorAlias(DecisionTreeOp.class);
        final Product classificationProduct = GPF.createProduct(decisiontreeAlias, classificationParameter,
                                                                classificationInputProducts);
        classificationProduct.setName(model.getClassificationProductName());
        copySensingStartAndStopTime(landsatProduct, classificationProduct);

        MetadataElement metadataElement = new MetadataElement("Variablen");
        for (DecisionVariable variable : model.getConfiguration().getVariables()) {
            String name = variable.getName();
            ProductData data = ProductData.createInstance(new double[]{variable.getValue()});
            MetadataAttribute attribute = new MetadataAttribute(name, data, true);
            attribute.setDescription(variable.getDescription());
            metadataElement.addAttribute(attribute);
        }
        classificationProduct.getMetadataRoot().addElement(metadataElement);

        VisatApp.getApp().addProduct(endmemberProduct);
        VisatApp.getApp().addProduct(indexProduct);
        VisatApp.getApp().addProduct(classificationProduct);
    }

    private void copySensingStartAndStopTime(Product sourceProduct, Product targetProduct) {
        targetProduct.setStartTime(sourceProduct.getStartTime());
        targetProduct.setEndTime(sourceProduct.getEndTime());
    }

    private Map<String, Object> getIndexParameter() {
        String band3 = bandFinder.getBand(2).getName();
        String band4 = bandFinder.getBand(3).getName();
        String band5 = bandFinder.getBand(4).getName();
        Map<String, Object> parameter = new HashMap<String, Object>();
        BandMathsOp.BandDescriptor[] bandDesc = new BandMathsOp.BandDescriptor[4];
        bandDesc[0] = new BandMathsOp.BandDescriptor();
        bandDesc[0].name = "NDVI";
        bandDesc[0].expression = "($landsat." + band4 + " - $landsat." + band3 + ")/($landsat." + band4 + " + $landsat." + band3 + ")";
        bandDesc[0].type = ProductData.TYPESTRING_FLOAT32;

        bandDesc[1] = new BandMathsOp.BandDescriptor();
        bandDesc[1].name = "Steigung_3_4";
        bandDesc[1].expression = "($landsat." + band3 + " - $landsat." + band4 + ")/(0.66-0.835)";
        bandDesc[1].type = ProductData.TYPESTRING_FLOAT32;

        bandDesc[2] = new BandMathsOp.BandDescriptor();
        bandDesc[2].name = "Steigung_4_5";
        bandDesc[2].expression = "($landsat." + band4 + " - $landsat." + band5 + ")/(0.835-1.65)";
        bandDesc[2].type = ProductData.TYPESTRING_FLOAT32;

        bandDesc[3] = new BandMathsOp.BandDescriptor();
        bandDesc[3].name = "Schlick_corr";
        bandDesc[3].expression = "($endmember.Sand_wc < 0.0) ? $endmember.Sand_wc + $endmember.Schlick : $endmember.Schlick";
        bandDesc[3].type = ProductData.TYPESTRING_FLOAT32;
        parameter.put("targetBands", bandDesc);
        return parameter;
    }

    private Map<String, Object> getUnmixingParameter() throws IOException {
        Map<String, Object> parameter = new HashMap<String, Object>();
        InputStream inputStream = this.getClass().getResourceAsStream(EM_CSV);
        InputStreamReader reader = new InputStreamReader(inputStream);
        DiagramGraph[] diagramGraphs = DiagramGraphIO.readGraphs(reader);
        Endmember[] endmembers = convertGraphsToEndmembers(diagramGraphs);
        Endmember[] realEndmembers = new Endmember[endmembers.length];
        double[] realWavelengths = new double[bandFinder.getBandCount()];
        for (int i = 0; i < realWavelengths.length; i++) {
            Band band = bandFinder.getBand(i);
            realWavelengths[i] = band.getSpectralWavelength();
        }
        for (int i = 0; i < endmembers.length; i++) {
            Endmember endmember = endmembers[i];
            realEndmembers[i] = new Endmember(endmember.getName(), realWavelengths, endmember.getRadiations());
        }
        parameter.put("endmembers", realEndmembers);

        parameter.put("sourceBandNames", bandFinder.getBandNames());
        parameter.put("unmixingModelName", "Constrained LSU");
        parameter.put("abundanceBandNameSuffix", "");
        return parameter;
    }

    private Endmember[] convertGraphsToEndmembers(DiagramGraph[] diagramGraphs) {
        Endmember[] endmembers = new Endmember[diagramGraphs.length];
        for (int i = 0; i < diagramGraphs.length; i++) {
            DiagramGraph diagramGraph = diagramGraphs[i];
            int numValues = diagramGraph.getNumValues();
            double[] wavelengths = new double[numValues];
            double[] radiations = new double[numValues];
            for (int j = 0; j < numValues; j++) {
                wavelengths[j] = diagramGraph.getXValueAt(j);
                radiations[j] = diagramGraph.getYValueAt(j);
            }
            endmembers[i] = new Endmember(diagramGraph.getYName(), wavelengths, radiations);
        }
        return endmembers;
    }

    private Map<String, Object> getClassificationParameter() throws OperatorException {
        Map<String, Object> parameter = new HashMap<String, Object>();
        DecisionTreeConfiguration configuration = model.getConfiguration();
        if (model.useRoi()) {
            registerRoiMaskSymbol(model.getInputProduct().getMaskGroup().get(model.getMaskName()));
            Decision inRoiDecision = new Decision("inRoi", "inROI");
            inRoiDecision.setYesDecision(configuration.getRootDecisions());
            inRoiDecision.setNoClass(configuration.getClass("nodata"));
            configuration.setRootDecisions(inRoiDecision);
        }
        replaceBandNamesinTerms(configuration.getAllDecisions());
        parameter.put("configuration", configuration);
        parameter.put("bandName", "Klassifikation");
        return parameter;
    }

    private void replaceBandNamesinTerms(Decision[] allDecisions) {
        for (Decision decision : allDecisions) {
            String term = decision.getTerm();
            for (int i = 0; i < 6; i++) {
                term = term.replace("band" + (i + 1), bandFinder.getBand(i).getName());
            }
            decision.setTerm(term);
        }
    }

    private void registerRoiMaskSymbol(Mask band) throws OperatorException {
        final String symbolName = "inROI";
        final ROI roi = new ROI(band.getSourceImage());
        Symbol s = new AbstractSymbol.B(symbolName) {
            @Override
            public boolean evalB(EvalEnv env) throws EvalException {
                RasterDataEvalEnv eEnv = (RasterDataEvalEnv) env;
                return roi.contains(eEnv.getPixelX(), eEnv.getPixelY());
            }
        };
        BandArithmetic.registerSymbol(s);
    }
}
