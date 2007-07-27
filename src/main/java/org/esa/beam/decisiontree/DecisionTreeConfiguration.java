package org.esa.beam.decisiontree;

import com.thoughtworks.xstream.io.xml.xppdom.Xpp3Dom;
import com.thoughtworks.xstream.io.xml.xppdom.Xpp3DomBuilder;
import org.esa.beam.util.StringUtils;

import java.awt.Color;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecisionTreeConfiguration {
	
	private String name;
	private Classification[] classes;
	private Map<String, Classification> classMap;
	private DecisionVariable[] variables;
	private Decision rootDecisions;
	
	public DecisionTreeConfiguration(Reader inputReader) throws Exception {
        Xpp3Dom dom = Xpp3DomBuilder.build(inputReader);
        
        String decisionName = dom.getAttribute("name");
        setName(decisionName);
        
        Xpp3Dom[] classesDom = dom.getChild("classes").getChildren();
        Classification[] theClasses = parseClasses(classesDom);
        setClasses(theClasses);
        
        Xpp3Dom allVariablesDom = dom.getChild("variables");
        if (allVariablesDom != null) {
        	Xpp3Dom[] variableDoms = allVariablesDom.getChildren();
        	DecisionVariable[] theVariables = parseVariables(variableDoms);
        	setVariables(theVariables);
        }
        
        Xpp3Dom decisionDom = dom.getChild("decision");
        Decision decision = parseDecisions(decisionDom);
        setRootDecisions(decision);
	}



	private Decision parseDecisions(Xpp3Dom decisionDom) {
		String decisionName = decisionDom.getAttribute("name");
		String term = decisionDom.getAttribute("term");
		Xpp3Dom yesDom = decisionDom.getChild("yes");
		Xpp3Dom noDom = decisionDom.getChild("no");
		Decision decisions = new Decision(decisionName, term);
		if (yesDom.getChildCount() == 0) {
			String yesClassName = yesDom.getValue();
			Classification yesClass = getClass(yesClassName);
			decisions.setYesClass(yesClass);
		} else {
			Decision yesDecision = parseDecisions(yesDom.getChild("decision"));
			decisions.setYesDecision(yesDecision);
		}
		if (noDom.getChildCount() == 0) {
			String noClassName = noDom.getValue();
			Classification noClass = getClass(noClassName);
			decisions.setNoClass(noClass);
		} else {
			Decision noDecision = parseDecisions(noDom.getChild("decision"));
			decisions.setNoDecision(noDecision);
		}
		
		return decisions;
		
	}

	private DecisionVariable[] parseVariables(Xpp3Dom[] variableDoms) {
		DecisionVariable[] theVariables = new DecisionVariable[variableDoms.length];
		int i = 0;
		for (Xpp3Dom variableDom : variableDoms) {
			String varName = variableDom.getAttribute("name");
			String value = variableDom.getAttribute("value");
			String description = variableDom.getAttribute("description");
			DecisionVariable decisionVariable = new DecisionVariable(varName, value, description);
			theVariables[i] = decisionVariable;
			i++;
		}
		return theVariables;
	}
	
	private Classification[] parseClasses(Xpp3Dom[] classesDoms) {
		Classification[] classifications = new Classification[classesDoms.length];
		int i = 0;
		for (Xpp3Dom classDom : classesDoms) {
			String classificationName = classDom.getAttribute("name");
			String value = classDom.getAttribute("value");
			String colorString = classDom.getAttribute("color");
			Color color = parseColor(colorString);
			Classification classification = new Classification(classificationName, value, color);
			classifications[i] = classification;
			i++;
		}
		return classifications;
	}
	
	protected static Color parseColor(String colorString) {
		if (StringUtils.isNotNullAndNotEmpty(colorString)) {
			if (colorString.contains(",")) {
				Pattern p = Pattern.compile("\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*");
				Matcher m = p.matcher(colorString);
				if (m.matches() && m.groupCount() == 3) {
					int r = Integer.parseInt(m.group(1));
					int g = Integer.parseInt(m.group(2));
					int b = Integer.parseInt(m.group(3));
					return new Color(r, g, b);
				} else {
					throw new IllegalArgumentException("Wrong format rgb color expression: "+ colorString);
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
	        	throw new IllegalArgumentException("Nither a color name nor a rgb expression: "+colorString);
	        }
		} else {
			throw new IllegalArgumentException("No color given");
		}
	}

	public Classification[] getClasses() {
		return classes;
	}

	public Classification getClass(String aName) {
		return classMap.get(aName);
	}
	
	public void setClasses(Classification[] classes) {
		this.classes = classes;
		classMap = new HashMap<String, Classification>(classes.length);
		for (Classification aClass : classes) {
			classMap.put(aClass.getName(), aClass);
		}
	}
	
	public void setVariables(DecisionVariable[] variables) {
		this.variables = variables;
	}
	
	public DecisionVariable[] getVariables() {
		return variables;
	}
	
	

	public Decision getRootDecisions() {
		return rootDecisions;
	}

	public void setRootDecisions(Decision rootDecisions) {
		this.rootDecisions = rootDecisions;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public Decision[] getAllDecisions() {
		List<Decision> decisions = new ArrayList<Decision>();
		addDecisionToList(getRootDecisions(), decisions);
		return decisions.toArray(new Decision[decisions.size()]);
	}
	
	private void addDecisionToList(Decision decision, List<Decision> decisionList) {
		decisionList.add(decision);
		if (decision.getYesDecision() != null) {
			addDecisionToList(decision.getYesDecision(), decisionList);
		}
		if (decision.getNoDecision() != null) {
			addDecisionToList(decision.getNoDecision(), decisionList);
		}
	}
}
