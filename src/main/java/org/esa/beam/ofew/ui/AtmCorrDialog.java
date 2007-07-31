package org.esa.beam.ofew.ui;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.progress.DialogProgressMonitor;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.FlagCoding;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.operators.common.BandArithmeticOp;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.util.ProductUtils;
import org.esa.beam.visat.VisatApp;

import java.awt.Dialog;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Dialog for OFEW atmospheric correction.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 */
public class AtmCorrDialog extends ModalDialog {

    public static final String TITLE = "OFEW Atmosphärenkorrektur";

    private final AtmCorrFormPresenter presenter;
    private final AtmCorrForm form;

    private final Product sourceProduct;
    private final Band[] sourceBands;

    AtmCorrDialog(Window parent, Product sourceProduct, Band[] sourceBands) {
        super(parent, TITLE, ModalDialog.ID_OK_CANCEL, null);

        this.sourceProduct = sourceProduct;
        this.sourceBands = sourceBands;

        presenter = new AtmCorrFormPresenter(sourceProduct, sourceBands);
        form = new AtmCorrForm(presenter);
    }

    @Override
    public int show() {
        setContent(form);
        return super.show();
    }

    @Override
    protected boolean verifyUserInput() {
        return form.hasValidValues();
    }

    @Override
    protected void onOK() {
        final DialogProgressMonitor pm = new DialogProgressMonitor(
                VisatApp.getApp().getMainFrame(), TITLE, Dialog.ModalityType.APPLICATION_MODAL);

        try {
            VisatApp.getApp().addProduct(createTargetProduct(pm));
        } catch (OperatorException e) {
            showErrorDialog(e.getMessage());
            VisatApp.getApp().getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        super.onOK();
    }

    private Product createTargetProduct(ProgressMonitor pm) throws OperatorException {
        try {
            pm.beginTask("Performing OFEW atmospheric correction", 10);

            final Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("productName", presenter.getTargetProductName());

            final BandArithmeticOp.BandDescriptor[] bandDescriptors =
                    new BandArithmeticOp.BandDescriptor[sourceBands.length];

            for (int i = 0; i < sourceBands.length; ++i) {
                final BandArithmeticOp.BandDescriptor bandDescriptor = new BandArithmeticOp.BandDescriptor();
                final double a = presenter.getCoefficientA(i);
                final double b = presenter.getCoefficientB(i);

                bandDescriptor.name = sourceBands[i].getName();
                bandDescriptor.expression = a + " * " + sourceBands[i].getName() + " + " + b;
                bandDescriptor.type = ProductData.TYPESTRING_FLOAT32;
                bandDescriptor.validExpression = sourceBands[i].getValidPixelExpression();
                bandDescriptor.spectralBandIndex = i;
                bandDescriptor.spectralWavelength = sourceBands[i].getSpectralWavelength();
                bandDescriptor.spectralBandwidth = sourceBands[i].getSpectralBandwidth();

                bandDescriptors[i] = bandDescriptor;
            }
            parameterMap.put("bandDescriptors", bandDescriptors);

            final Product targetProduct = GPF.createProduct("BandArithmetic", parameterMap, sourceProduct, pm);
            targetProduct.setStartTime(sourceProduct.getStartTime());
            targetProduct.setEndTime(sourceProduct.getEndTime());
            ProductUtils.copyFlagCodings(sourceProduct, targetProduct);

            for (final Band sourceBand : sourceProduct.getBands()) {
                final FlagCoding flagCoding = sourceBand.getFlagCoding();
                if (flagCoding != null) {
                    targetProduct.getBand(sourceBand.getName()).setFlagCoding(targetProduct.getFlagCoding(flagCoding.getName()));
                }
            }
            ProductUtils.copyBitmaskDefs(sourceProduct, targetProduct);
            ProductUtils.copyElementsAndAttributes(sourceProduct.getMetadataRoot(), targetProduct.getMetadataRoot());

            return targetProduct;
        } finally {
            pm.done();
        }
    }

}
