package com.example.evolon.domain.enums;

public enum ListingType {
	SINGLE("シングル"), BOX("BOX"), DECK("デッキ");

	private final String label;

	ListingType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
