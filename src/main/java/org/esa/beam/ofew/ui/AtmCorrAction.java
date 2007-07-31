package org.esa.beam.ofew.ui;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.command.ExecCommand;
import org.esa.beam.ofew.SpectralBandFinder;
import org.esa.beam.visat.VisatApp;

/**
 * Action for OFEW atmospheric correction.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 */
public class AtmCorrAction extends ExecCommand {

    private final double[] wavelengths = {478.0, 560.0, 660.0, 835.0, 1650.0, 2208.0};

    @Override
    public void actionPerformed(CommandEvent commandEvent) {
        final Product selectedProduct = VisatApp.getApp().getSelectedProduct();
        final SpectralBandFinder bandFinder = new SpectralBandFinder(selectedProduct, wavelengths);

        new AtmCorrDialog(VisatApp.getApp().getMainFrame(), selectedProduct, bandFinder.getBands()).show();
    }

    @Override
    public void updateState() {
        final Product product = VisatApp.getApp().getSelectedProduct();
        setEnabled(product != null && new SpectralBandFinder(product, wavelengths).hasFoundAll());
    }

}
