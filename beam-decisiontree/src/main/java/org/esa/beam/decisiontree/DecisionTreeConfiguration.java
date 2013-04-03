package org.esa.beam.decisiontree;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.io.xml.xppdom.Xpp3Dom;
import com.thoughtworks.xstream.io.xml.xppdom.Xpp3DomBuilder;
import com.bc.ceres.binding.dom.XppDomElement;
import com.thoughtworks.xstream.io.xml.xppdom.XppDom;
import com.thoughtworks.xstream.io.xml.xppdom.XppFactory;
import org.xmlpull.mxp1.MXParserCachingStrings;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.wrapper.classic.StaticXmlPullParserWrapper;
import org.xmlpull.v1.wrapper.classic.XmlPullParserDelegate;

public class DecisionTreeConfiguration {

	private String name;
	private Classification[] classes;
	private DecisionVariable[] variables;
	private Decision rootDecisions;
	private Map<String, Classification> classMap;

	public static DecisionTreeConfiguration fromXML(Reader inputReader) {
		XppDom dom;
		DecisionTreeConfiguration configuration = new DecisionTreeConfiguration();
		DecisionTreeDomConverter converter = new DecisionTreeDomConverter();
		try {
            final XmlPullParser parser = XppFactory.createDefaultParser();
            parser.setInput(inputReader);
            dom = XppDom.build(parser);
			final XppDomElement xppDomElement = new XppDomElement(dom);
			converter.convertDomToValue(xppDomElement, configuration);
		} catch (Exception e) {
			return null;
		}
        return configuration;
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

	public String getName() {
		return name;
	}

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
