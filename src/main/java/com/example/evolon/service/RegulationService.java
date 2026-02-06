package com.example.evolon.service;

import org.springframework.stereotype.Service;

import com.example.evolon.domain.enums.PrintedRegulation;
import com.example.evolon.domain.enums.Regulation;

@Service
public class RegulationService {

	public Regulation resolve(PrintedRegulation printed) {
		if (printed == null)
			return null;

		return switch (printed) {
		case H, I, J -> Regulation.STANDARD;
		default -> Regulation.EXTRA;
		};
	}
}
