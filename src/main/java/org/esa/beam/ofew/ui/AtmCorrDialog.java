package org.esa.beam.ofew.ui;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.progress.DialogProgressMonitor;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.operators.common.BandArithmeticOp;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.visat.VisatApp;

import java.awt.Dialog;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 */
public class AtmCorrDialog extends ModalDialog {

    private static final String TITLE = "OFEW Atmosphärenkorrektur";

    private final AtmCorrFormPresenter presenter;
    private final AtmCorrForm form;

    private final Product inputProduct;
    private final Band[] inputBands;

    AtmCorrDialog(Window parent, Product inputProduct, Band[] inputBands) {
        super(parent, TITLE, ModalDialog.ID_OK_CANCEL, null);

        this.inputProduct = inputProduct;
        this.inputBands = inputBands;

        presenter = new AtmCorrFormPresenter(inputProduct, inputBands);
        form = new AtmCorrForm(presenter);
    }

    @Override
    public int show() {
        setContent(form);
        return super.show();
    }

    @Override
    protected void onOK() {
        final DialogProgressMonitor pm = new DialogProgressMonitor(
                VisatApp.getApp().getMainFrame(), TITLE, Dialog.ModalityType.APPLICATION_MODAL);

        try {
            VisatApp.getApp().addProduct(createAtmCorrProduct(pm));
        } catch (OperatorException e) {
            showErrorDialog(e.getMessage());
            VisatApp.getApp().getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        super.onOK();
    }

    private Product createAtmCorrProduct(ProgressMonitor pm) throws OperatorException {
        try {
            pm.beginTask("Performing OFEW atmospheric correction", 10);

            final Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("productName", presenter.getOutputProductName());

            final BandArithmeticOp.BandDescriptor[] bandDescriptors =
                    new BandArithmeticOp.BandDescriptor[inputBands.length];

            for (int i = 0; i < inputBands.length; ++i) {
                final BandArithmeticOp.BandDescriptor bandDescriptor = new BandArithmeticOp.BandDescriptor();
                final double a = presenter.getCoefficientA(i);
                final double b = presenter.getCoefficientB(i);

                bandDescriptor.name = inputBands[i].getName();
                bandDescriptor.expression = a + " * " + inputBands[i].getName() + " + " + b;
                bandDescriptor.type = ProductData.TYPESTRING_FLOAT32;
                bandDescriptor.spectralBandIndex = i;
                bandDescriptor.spectralWavelength = inputBands[i].getSpectralWavelength();
                bandDescriptor.spectralBandwidth = inputBands[i].getSpectralBandwidth();

                bandDescriptors[i] = bandDescriptor;
            }
            parameterMap.put("bandDescriptors", bandDescriptors);

            return GPF.createProduct("BandArithmetic", parameterMap, inputProduct, pm);
        } finally {
            pm.done();
        }
    }

}
