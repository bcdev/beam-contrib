package org.esa.beam.decisiontree;

import java.awt.Color;

public class ClassificationClass {
	
	private String name;
	private int value;
	private Color color;

	public ClassificationClass(String name, String valueString, String colorString) {
		this.name = name;
		value = Integer.parseInt(valueString);
		color = Color.decode(colorString);
	}

}
