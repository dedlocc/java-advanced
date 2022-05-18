package info.kgeorgiy.ja.koton.bank;

import org.junit.runner.JUnitCore;

public final class BankTests {
    public static void main(final String[] args) {
        System.exit(new JUnitCore().run(BankTest.class).wasSuccessful() ? 0 : 1);
    }
}
