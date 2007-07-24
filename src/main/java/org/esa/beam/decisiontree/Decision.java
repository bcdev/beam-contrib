package org.esa.beam.decisiontree;

public class Decision {

	private final String name;
	private final String term;
	private Decision yesDecision;
	private Decision noDecision;
	private Classification yesClass;
	private Classification noClass;

	public Decision(String name, String term) {
		this.name = name;
		this.term = term;
	}

	public Classification getNoClass() {
		return noClass;
	}

	public void setNoClass(Classification noClass) {
		this.noClass = noClass;
	}

	public Decision getNoDecision() {
		return noDecision;
	}

	public void setNoDecision(Decision noDecision) {
		this.noDecision = noDecision;
	}

	public Classification getYesClass() {
		return yesClass;
	}

	public void setYesClass(Classification yesClass) {
		this.yesClass = yesClass;
	}

	public Decision getYesDecision() {
		return yesDecision;
	}

	public void setYesDecision(Decision yesDecision) {
		this.yesDecision = yesDecision;
	}

	public String getName() {
		return name;
	}

	public String getTerm() {
		return term;
	}
}
