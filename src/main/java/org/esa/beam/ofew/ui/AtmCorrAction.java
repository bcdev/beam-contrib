package org.esa.beam.ofew.ui;

import com.bc.ceres.core.SubProgressMonitor;
import com.bc.ceres.swing.progress.DialogProgressMonitor;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.operators.common.BandArithmeticOp;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.command.ExecCommand;
import org.esa.beam.visat.VisatApp;

import javax.swing.JPanel;
import java.awt.Dialog;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Action for OFEW atmospheric correction.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 */
public class AtmCorrAction extends ExecCommand {

    @Override
    public void actionPerformed(CommandEvent commandEvent) {
        final Product selectedProduct = VisatApp.getApp().getSelectedProduct();

        final ModalDialog dialog =
                new ModalDialog(VisatApp.getApp().getMainFrame(),
                                "OFEW Atmosphärenkorrektur",
                                ModalDialog.ID_OK_CANCEL_HELP,
                                "ofewAtmCorrTool");

        final AtmCorrPresenter presenter = new AtmCorrPresenter(selectedProduct);
        JPanel ofewAtmCorrPanel = new AtmCorrPanel(presenter);
        dialog.setContent(ofewAtmCorrPanel);

        if (dialog.show() == ModalDialog.ID_OK) {
            final DialogProgressMonitor pm =
                    new DialogProgressMonitor(VisatApp.getApp().getMainFrame(),
                                              "OFEW Atmosphärenkorrektur",
                                              Dialog.ModalityType.APPLICATION_MODAL);

            try {
                pm.beginTask("Performing OFEW atmospheric correction", 42);

                final Product indexProduct = GPF.createProduct("BandArithmetic",
                                                               getBandDescriptorMap(presenter),
                                                               presenter.getInputProduct(),
                                                               new SubProgressMonitor(pm, 10));

                VisatApp.getApp().addProduct(indexProduct);
            } catch (OperatorException e) {
                dialog.showErrorDialog(e.getMessage());
                VisatApp.getApp().getLogger().log(Level.SEVERE, e.getMessage(), e);
            } finally {
                pm.done();
            }
        }
    }

    @Override
    public void updateState() {
        final Product product = VisatApp.getApp().getSelectedProduct();
        setEnabled(product != null && product.getNumBands() == 6);
    }

    private static Map<String, Object> getBandDescriptorMap(AtmCorrPresenter presenter) {
        final Map<String, Object> map = new HashMap<String, Object>();
        final BandArithmeticOp.BandDescriptor[] bandDescriptors =
                new BandArithmeticOp.BandDescriptor[presenter.getBandCount()];

        for (int i = 0; i < presenter.getBandCount(); ++i) {
            final BandArithmeticOp.BandDescriptor bandDescriptor = new BandArithmeticOp.BandDescriptor();
            final double a = presenter.getA(i);
            final double b = presenter.getB(i);

            bandDescriptor.name = presenter.getBandName(i);
            bandDescriptor.expression = a + " * " + presenter.getBandName(i) + " + " + b;
            bandDescriptor.type = ProductData.TYPESTRING_FLOAT32;

            bandDescriptors[i] = bandDescriptor;
        }
        map.put("bandDescriptors", bandDescriptors);

        return map;
    }

}
