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
package org.esa.beam.ofew.ui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.JOptionPane;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.command.ExecCommand;
import org.esa.beam.ofew.SpectralBandFinder;
import org.esa.beam.visat.VisatApp;

/**
 * OFEW Classification Action
 *
 * @author Marco Zuehlke
 * @author Ralf Quast
 * @version $Revision:$ $Date:$
 */
public class ClassificationAction extends ExecCommand {

	private Map<String, ClassificationModel.Session> sessionMap = new HashMap<String, ClassificationModel.Session>();
	
	@Override
    public void actionPerformed(CommandEvent commandEvent) {
        final Product selectedProduct = VisatApp.getApp().getSelectedProduct();

        ClassificationModel.Session session = sessionMap.get(selectedProduct.getName());
        if (session == null) {
        	session = new ClassificationModel.Session();
        	sessionMap.put(selectedProduct.getName(), session);
        }
        try {
        	final ClassificationDialog dialog = new ClassificationDialog(
        		VisatApp.getApp().getMainFrame(), selectedProduct, session);
        	dialog.show();
        } catch (IOException e) {
        	JOptionPane.showMessageDialog(VisatApp.getApp().getMainFrame(),
        			e.getMessage(), ClassificationDialog.TITLE, JOptionPane.ERROR_MESSAGE);
        	VisatApp.getApp().getLogger().log(Level.SEVERE, e.getMessage(), e);
        	return;
        }
    }

    @Override
    public void updateState() {
        final Product selectedProduct = VisatApp.getApp().getSelectedProduct();
        setEnabled(selectedProduct != null && new SpectralBandFinder(selectedProduct, SpectralBandFinder.OFEW_WAVELENGTHS).hasFoundAll());
    }
}