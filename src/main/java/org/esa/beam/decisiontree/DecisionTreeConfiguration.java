package org.esa.beam.decisiontree;

import java.io.Reader;

import com.thoughtworks.xstream.io.xml.xppdom.Xpp3Dom;
import com.thoughtworks.xstream.io.xml.xppdom.Xpp3DomBuilder;

public class DecisionTreeConfiguration {

	private ClassificationClass[] classes;
	private Decision rootDecisions;
	
	public DecisionTreeConfiguration(Reader inputReader) throws Exception {
        Xpp3Dom dom = Xpp3DomBuilder.build(inputReader);
        
        Xpp3Dom[] classesDom = dom.getChild("classes").getChildren();
        buildClasses(classesDom);
        
        Xpp3Dom decisionDom = dom.getChild("decision");
        buildDecisionTree(decisionDom);
        
	}

	private void buildDecisionTree(Xpp3Dom decisionDom) {
		String name = decisionDom.getAttribute("name");
		String term = decisionDom.getAttribute("term");
		Xpp3Dom yesDom = decisionDom.getChild("yes");
		Xpp3Dom noDom = decisionDom.getChild("no");
		rootDecisions = new Decision(name, term);
		
	}

	private void buildClasses(Xpp3Dom[] classesDoms) {
		classes = new ClassificationClass[classesDoms.length];
		int i = 0;
		for (Xpp3Dom classDom : classesDoms) {
			String name = classDom.getAttribute("name");
			String value = classDom.getAttribute("value");
			String color = classDom.getAttribute("color");
			ClassificationClass cClass = new ClassificationClass(name, value, color);
			classes[i] = cClass;
			i++;
		}
	}
}
