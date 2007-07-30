package org.esa.beam.ofew.ui;

import com.bc.ceres.core.SubProgressMonitor;
import com.bc.ceres.swing.progress.DialogProgressMonitor;
import com.bc.ceres.binding.ValidationException;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.operators.common.BandArithmeticOp.BandDescriptor;
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

    private final double[] wavelengths = {490.0, 560.0, 660.0, 830.0, 1670.0, 2240.0};

    @Override
    public void actionPerformed(CommandEvent commandEvent) {
        final Product selectedProduct = VisatApp.getApp().getSelectedProduct();

        final ModalDialog dialog =
                new ModalDialog(VisatApp.getApp().getMainFrame(),
                                "OFEW Atmosphärenkorrektur",
                                ModalDialog.ID_OK_CANCEL_HELP,
                                "ofewAtmCorrTool");

        AtmCorrPresenter presenter = null;
        try {
            presenter = new AtmCorrPresenter(selectedProduct.getName(), findSpectralBands(selectedProduct));
        } catch (ValidationException e) {
            // todo - action
        }
        JPanel ofewAtmCorrPanel = new AtmCorrPanel(presenter);
        dialog.setContent(ofewAtmCorrPanel);

        if (dialog.show() == ModalDialog.ID_OK) {
            final DialogProgressMonitor pm =
                    new DialogProgressMonitor(VisatApp.getApp().getMainFrame(),
                                              "OFEW Atmosphärenkorrektur",
                                              Dialog.ModalityType.APPLICATION_MODAL);

            try {
                BandDescriptor[] bds = (BandDescriptor[]) getBandDescriptorMap(presenter).get("bandDescriptors");
                for (BandDescriptor bd : bds) {
                    System.out.println(bd.expression);
                }
                pm.beginTask("Performing OFEW atmospheric correction", 42);

                final Product atmCorrProduct = GPF.createProduct("BandArithmetic",
                                                               getBandDescriptorMap(presenter),
                                                               selectedProduct,
                                                               new SubProgressMonitor(pm, 10));
                atmCorrProduct.setName(presenter.getOutputProduct());

                VisatApp.getApp().addProduct(atmCorrProduct);
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
        setEnabled(product != null && verifySpectralWavelengths(product));
    }

    private boolean verifySpectralWavelengths(Product product) {
        search:
        for (double wavelength : wavelengths) {
            for (final Band band : product.getBands()) {
                if (band.getSpectralBandIndex() != -1) {
                    if (wavelength == band.getSpectralWavelength()) {
                        continue search;
                    }
                }
            }
            return false;
        }

        return true;
    }

    private String[] findSpectralBands(Product product) {
        final String[] bandNames = new String[wavelengths.length];

        search:
        for (int i = 0; i < wavelengths.length; i++) {
            for (final Band band : product.getBands()) {
                if (band.getSpectralBandIndex() != -1) {
                    if (wavelengths[i] == band.getSpectralWavelength()) {
                        bandNames[i] = band.getName();
                        continue search;
                    }
                }
            }
        }

        return bandNames;
    }

    private static Map<String, Object> getBandDescriptorMap(AtmCorrPresenter presenter) {
        final Map<String, Object> map = new HashMap<String, Object>();
        final BandDescriptor[] bandDescriptors =
                new BandDescriptor[presenter.getBandCount()];

        for (int i = 0; i < presenter.getBandCount(); ++i) {
            final BandDescriptor bandDescriptor = new BandDescriptor();
            final double a = presenter.getCoefficientA(i);
            final double b = presenter.getCoefficientB(i);

            bandDescriptor.name = presenter.getBandName(i);
            bandDescriptor.expression = a + " * " + presenter.getBandName(i) + " + " + b;
            bandDescriptor.type = ProductData.TYPESTRING_FLOAT32;

            bandDescriptors[i] = bandDescriptor;
        }
        map.put("bandDescriptors", bandDescriptors);

        return map;
    }

}
