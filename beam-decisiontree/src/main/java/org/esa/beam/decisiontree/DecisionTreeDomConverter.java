/*
 * $Id: $
 * 
 * Copyright (C) 2007 by Brockmann Consult (info@brockmann-consult.de)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation. This program is distributed in the hope it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.esa.beam.decisiontree;

import com.bc.ceres.binding.ConversionException;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.dom.DomConverter;
import com.bc.ceres.binding.dom.DomElement;
import org.esa.beam.util.StringUtils;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by marcoz.
 *
 * @author marcoz
 * @version $Revision: $ $Date: $
 */
public class DecisionTreeDomConverter implements DomConverter {

    public Object convertDomToValue(DomElement parentElement, Object value)
            throws ConversionException, ValidationException {

        DecisionTreeConfiguration configuration = (DecisionTreeConfiguration) value;
        if (configuration == null) {
            configuration = new DecisionTreeConfiguration();
        }

        String treeName = parentElement.getAttribute("name");
        configuration.setName(treeName);

        DomElement[] classes = parentElement.getChildren("classes");
        if (classes.length == 1) {
            DomElement[] classDoms = parentElement.getChild("classes").getChildren();
            Classification[] theClasses = parseClasses(classDoms);
            configuration.setClasses(theClasses);
        } else {
            throw new ConversionException("DecisionTreeConfiguration doesn't contain classes");
        }

        DomElement[] variables = parentElement.getChildren("variables");
        if (variables.length == 1) {
            DomElement[] variableDoms = variables[0].getChildren();
            DecisionVariable[] theVariables = parseVariables(variableDoms);
            configuration.setVariables(theVariables);
        }

        DomElement decisionDom = parentElement.getChild("decision");
        Decision decision = parseDecisions(configuration, decisionDom);
        configuration.setRootDecisions(decision);
        return configuration;
    }

    private static Classification[] parseClasses(DomElement[] classesDom) {
        Classification[] classifications = new Classification[classesDom.length];
        int i = 0;
        for (DomElement classDom : classesDom) {
            String classificationName = classDom.getAttribute("name");
            String value = classDom.getAttribute("value");
            String colorString = classDom.getAttribute("color");
            Color color = parseColor(colorString);
            Classification classification = new Classification(
                    classificationName, value, color);
            classifications[i] = classification;
            i++;
        }
        return classifications;
    }

    private static Decision parseDecisions(
            DecisionTreeConfiguration configuration, DomElement decisionDom) {
        String decisionName = decisionDom.getAttribute("name");
        String term = decisionDom.getAttribute("term");
        DomElement yesDom = decisionDom.getChild("yes");
        DomElement noDom = decisionDom.getChild("no");
        Decision decisions = new Decision(decisionName, term);
        if (yesDom.getChildCount() == 0) {
            String yesClassName = yesDom.getValue();
            Classification yesClass = configuration.getClass(yesClassName);
            decisions.setYesClass(yesClass);
        } else {
            Decision yesDecision = parseDecisions(configuration, yesDom
                    .getChild("decision"));
            decisions.setYesDecision(yesDecision);
        }
        if (noDom.getChildCount() == 0) {
            String noClassName = noDom.getValue();
            Classification noClass = configuration.getClass(noClassName);
            decisions.setNoClass(noClass);
        } else {
            Decision noDecision = parseDecisions(configuration, noDom
                    .getChild("decision"));
            decisions.setNoDecision(noDecision);
        }

        return decisions;

    }

    private static DecisionVariable[] parseVariables(DomElement[] variableDoms) {
        DecisionVariable[] theVariables = new DecisionVariable[variableDoms.length];
        int i = 0;
        for (DomElement variableDom : variableDoms) {
            String varName = variableDom.getAttribute("name");
            double value = Double
                    .parseDouble(variableDom.getAttribute("value"));
            String description = variableDom.getAttribute("description");
            DecisionVariable decisionVariable = new DecisionVariable(varName,
                    value, description);
            theVariables[i] = decisionVariable;
            i++;
        }
        return theVariables;
    }

    protected static Color parseColor(String colorString) {
        if (StringUtils.isNotNullAndNotEmpty(colorString)) {
            if (colorString.contains(",")) {
                Pattern p = Pattern
                        .compile("\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*");
                Matcher m = p.matcher(colorString);
                if (m.matches() && m.groupCount() == 3) {
                    int r = Integer.parseInt(m.group(1));
                    int g = Integer.parseInt(m.group(2));
                    int b = Integer.parseInt(m.group(3));
                    return new Color(r, g, b);
                } else {
                    throw new IllegalArgumentException(
                            "Wrong format rgb color expression: " + colorString);
                }
            } else if (colorString.equalsIgnoreCase("white")) {
                return Color.WHITE;
            } else if (colorString.equals("lightGray")) {
                return Color.LIGHT_GRAY;
            } else if (colorString.equals("LIGHT_GRAY")) {
                return Color.LIGHT_GRAY;
            } else if (colorString.equalsIgnoreCase("gray")) {
                return Color.GRAY;
            } else if (colorString.equals("darkGray")) {
                return Color.DARK_GRAY;
            } else if (colorString.equals("DARK_GRAY")) {
                return Color.DARK_GRAY;
            } else if (colorString.equalsIgnoreCase("black")) {
                return Color.BLACK;
            } else if (colorString.equalsIgnoreCase("red")) {
                return Color.RED;
            } else if (colorString.equalsIgnoreCase("pink")) {
                return Color.PINK;
            } else if (colorString.equalsIgnoreCase("orange")) {
                return Color.ORANGE;
            } else if (colorString.equalsIgnoreCase("yellow")) {
                return Color.YELLOW;
            } else if (colorString.equalsIgnoreCase("green")) {
                return Color.GREEN;
            } else if (colorString.equalsIgnoreCase("magenta")) {
                return Color.MAGENTA;
            } else if (colorString.equalsIgnoreCase("cyan")) {
                return Color.CYAN;
            } else if (colorString.equalsIgnoreCase("blue")) {
                return Color.BLUE;
            } else {
                throw new IllegalArgumentException(
                        "Nither a color name nor a rgb expression: "
                                + colorString);
            }
        } else {
            throw new IllegalArgumentException("No color given");
        }
    }

    public void convertValueToDom(Object value, DomElement parentElement) {
        DecisionTreeConfiguration decisionTreeConfiguration = (DecisionTreeConfiguration) value;
        Classification[] classes = decisionTreeConfiguration.getClasses();
        if (classes.length != 0) {
            DomElement classesDom = parentElement.createChild("classes");
            for (Classification classification : classes) {
                DomElement classDom = classesDom.createChild("class");
                classDom.setAttribute("name", classification.getName());
                classDom.setAttribute("value", Integer.toString(classification.getValue()));
                Color color = classification.getColor();
                String colorString = Integer.toString(color.getRed()) + "," +
                        Integer.toString(color.getGreen()) + "," +
                        Integer.toString(color.getBlue());
                classDom.setAttribute("color", colorString);
            }
        }

        DecisionVariable[] variables = decisionTreeConfiguration.getVariables();
        if (variables.length != 0) {
            DomElement variablesDom = parentElement.createChild("variables");
            for (DecisionVariable variable : variables) {
                DomElement varDom = variablesDom.createChild("variable");
                varDom.setAttribute("name", variable.getName());
                varDom.setAttribute("value", Double.toString(variable.getValue()));
                varDom.setAttribute("description", variable.getDescription());
            }
        }

        Decision rootDecisions = decisionTreeConfiguration.getRootDecisions();
        addDecision(parentElement, rootDecisions);
    }

    private void addDecision(DomElement dom, Decision decision) {
        DomElement decisionDom = dom.createChild("decision");

        decisionDom.setAttribute("name", decision.getName());
        decisionDom.setAttribute("term", decision.getTerm());

        DomElement yesDom = decisionDom.createChild("yes");
        if (decision.getYesClass() != null) {
            yesDom.setValue(decision.getYesClass().getName());
        } else {
            Decision yesDecision = decision.getYesDecision();
            addDecision(yesDom, yesDecision);
        }
        DomElement noDom = decisionDom.createChild("no");
        if (decision.getNoClass() != null) {
            noDom.setValue(decision.getNoClass().getName());
        } else {
            Decision noDecision = decision.getNoDecision();
            addDecision(noDom, noDecision);
        }
    }

    public Class<DecisionTreeConfiguration> getValueType() {
        return DecisionTreeConfiguration.class;
    }
}
