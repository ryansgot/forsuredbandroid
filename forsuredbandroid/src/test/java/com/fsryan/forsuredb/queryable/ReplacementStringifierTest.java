package com.fsryan.forsuredb.queryable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;

public abstract class ReplacementStringifierTest {

    @RunWith(Parameterized.class)
    public static class Stringify {

        private static final int randomInt = ThreadLocalRandom.current().nextInt();
        private static final long randomLong = ThreadLocalRandom.current().nextLong();
        private static final float randomFloat = ThreadLocalRandom.current().nextFloat() * ThreadLocalRandom.current().nextInt();
        private static final double randomDouble = ThreadLocalRandom.current().nextDouble() * ThreadLocalRandom.current().nextLong();
        private static final byte[] allPossible;
        static {
            allPossible = new byte[0xFF + 1];
            for (int i = 0; i <= 0xFF; i++) {
                allPossible[i] = (byte) i;
            }
        }

        private final Object input;
        private final String expected;

        public Stringify(Object input, String expected) {
            this.input = input;
            this.expected = expected;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {randomInt, String.valueOf(randomInt)},
                    {randomLong, String.valueOf(randomLong)},
                    {randomFloat, String.valueOf(randomFloat)},
                    {randomDouble, String.valueOf(randomDouble)},
                    {allPossible, "X'000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F202122232425262728292A2B2C2D2E2F303132333435363738393A3B3C3D3E3F404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F606162636465666768696A6B6C6D6E6F707172737475767778797A7B7C7D7E7F808182838485868788898A8B8C8D8E8F909192939495969798999A9B9C9D9E9FA0A1A2A3A4A5A6A7A8A9AAABACADAEAFB0B1B2B3B4B5B6B7B8B9BABBBCBDBEBFC0C1C2C3C4C5C6C7C8C9CACBCCCDCECFD0D1D2D3D4D5D6D7D8D9DADBDCDDDEDFE0E1E2E3E4E5E6E7E8E9EAEBECEDEEEFF0F1F2F3F4F5F6F7F8F9FAFBFCFDFEFF'"},
            });
        }

        @Test
        public void shouldCorrectlyStringifyReplacement() {
            assertEquals(expected, ReplacementStringifier.stringify(input));
        }
    }
}
