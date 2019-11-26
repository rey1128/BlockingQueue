package com.rey;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.RepeatedTest;

public class RandomListUtil {
	public static List<Integer> generateRandomRangedIntList(int min, int max) {
		Random random = new Random();
		if (random.nextBoolean()) {
			return new ArrayList<Integer>();
		} else {
			List<Integer> l = generateRandomRangedIntList(min, max);
			int rangedInt = random.nextInt((max - min) + 1) + min;

			l.add(rangedInt);
			return l;
		}
	}

	public static List<Integer> generateNonEmptyRandomList(int min, int max) {
		List<Integer> l = generateRandomRangedIntList(min, max);
		while (l.isEmpty()) {
			l = generateRandomRangedIntList(min, max);
		}

		return l;
	}

	@RepeatedTest(100)
	public void testGenerateRandomIntList() {
		List<Integer> l = generateRandomRangedIntList(0, 5000);
		assertNotNull(l);
		assertTrue(l.stream().allMatch(ele -> ele >= 0 || ele <= 5000));
	}

	@RepeatedTest(100)
	public void testGenerateNonEmptyRandomIntList() {
		List<Integer> l = generateNonEmptyRandomList(0, 5000);
		assertFalse(l.isEmpty());
		assertTrue(l.stream().allMatch(ele -> ele >= 0 || ele <= 5000));
	}
}
