package com.example.evolon.domain.enums;

public enum Rarity {
	C("C"), U("U"), R("R"), RR("RR"), SR("SR"), SAR("SAR"), UR("UR"), MUR("MUR"), MA("MA"), ACE("ACE"), BWR("BWR");

	private final String label;

	Rarity(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
