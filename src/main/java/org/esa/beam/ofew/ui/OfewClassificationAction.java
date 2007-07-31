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
import java.util.logging.Level;

import javax.swing.JOptionPane;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.command.ExecCommand;
import org.esa.beam.visat.VisatApp;

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

        try {
        	final OfewClassificationDialog dialog = new OfewClassificationDialog(
        		VisatApp.getApp().getMainFrame(), selectedProduct);
        	dialog.show();
        } catch (IOException e) {
        	JOptionPane.showMessageDialog(VisatApp.getApp().getMainFrame(),
        			e.getMessage(), OfewClassificationDialog.TITLE, JOptionPane.ERROR_MESSAGE);
        	VisatApp.getApp().getLogger().log(Level.SEVERE, e.getMessage(), e);
        	return;
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
}
