package com.credibanco.bancinc.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UniqueNumberGeneratorTest {

    @Test
    void testGenerateUniqueNumber() {
        UniqueNumberGenerator generator = new UniqueNumberGenerator();
        Set<Long> generatedNumbers = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            long number = generator.generateUniqueNumber();
            assertFalse(generatedNumbers.contains(number), "Número duplicado encontrado: " + number);
            generatedNumbers.add(number);

            assertTrue(isTenDigitNumber(number), "Número generado no tiene exactamente 10 dígitos: " + number);
        }

        assertEquals(1000, generatedNumbers.size(), "Número total de números generados no coincide");
    }

    private boolean isTenDigitNumber(long number) {
        return number >= 1000000000L && number <= 9999999999L;
    }
}
