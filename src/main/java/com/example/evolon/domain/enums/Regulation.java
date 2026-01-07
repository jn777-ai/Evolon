package com.example.evolon.domain.enums;

public enum Regulation {
	H("H"), I("I"), J("J");

	private final String label;

	Regulation(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
