package org.esa.beam.ofew.ui;

import java.util.HashMap;
import java.util.Map;

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
	
	private Map<String,AtmCorrModel.Session> sessionMap = new HashMap<String, AtmCorrModel.Session>();

    @Override
    public void actionPerformed(CommandEvent commandEvent) {
        final Product selectedProduct = VisatApp.getApp().getSelectedProduct();
        final SpectralBandFinder bandFinder = new SpectralBandFinder(selectedProduct, SpectralBandFinder.OFEW_WAVELENGTHS);

        AtmCorrModel.Session session = sessionMap.get(selectedProduct.getName());
        if (session == null) {
        	session = new AtmCorrModel.Session();
        	sessionMap.put(selectedProduct.getName(), session);
        }

        new AtmCorrDialog(VisatApp.getApp().getMainFrame(), selectedProduct, bandFinder.getBands(), session).show();
    }

    @Override
    public void updateState() {
        final Product product = VisatApp.getApp().getSelectedProduct();
        setEnabled(product != null && new SpectralBandFinder(product, SpectralBandFinder.OFEW_WAVELENGTHS).hasFoundAll());
    }
}