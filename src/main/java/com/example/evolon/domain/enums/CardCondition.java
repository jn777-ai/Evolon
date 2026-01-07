package com.example.evolon.domain.enums;

public enum CardCondition {
	NEW("新品"), NEAR_MINT("未使用に近い"), EXCELLENT("目立った傷なし"), GOOD("やや傷あり"), POOR("傷あり");

	private final String label;

	CardCondition(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
