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
package org.esa.beam.ofew;

import org.esa.beam.framework.datamodel.ProductNode;

import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.Validator;
import com.bc.ceres.binding.ValueModel;

/**
 * Created by marcoz.
 *
 * @author marcoz
 * @version $Revision: $ $Date: $
 */
public class ProductNameValidator implements Validator {

	public void validateValue(ValueModel valueModel, Object value) throws ValidationException {
        if (value == null ||
        		value.toString().trim().isEmpty() ||
        		!ProductNode.isValidNodeName(value.toString().trim())) {
            throw new ValidationException("Kein gültiger Produktname für '" + valueModel.getDefinition().getDisplayName() + "' angegeben.");
        }
    }

}
