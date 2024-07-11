package com.credibanco.bancinc.utils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class UniqueNumberGenerator {

    private final Set<Long> generatedNumbers = new HashSet<>();
    private final Random random = new Random();

    public long generateUniqueNumber() {
        long number;
        do {
            number = generateRandomTenDigitNumber();
        } while (generatedNumbers.contains(number));
        generatedNumbers.add(number);
        return number;
    }

    private long generateRandomTenDigitNumber() {
        long min = 1000000000L;
        long max = 9999999999L;
        return min + (long) (random.nextDouble() * (max - min + 1));
    }
}
