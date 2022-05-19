package info.kgeorgiy.ja.koton.bank;

import info.kgeorgiy.ja.koton.bank.account.Account;
import info.kgeorgiy.ja.koton.bank.bank.Bank;
import info.kgeorgiy.ja.koton.bank.person.Person;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public final class Client {
    /**
     * Utility class.
     */
    private Client() {
    }

    public static void main(final String... args) throws RemoteException {
        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }

        final String firstName;
        final String lastName;
        final String passportId;
        final String accountId;
        final int amountDiff;

        try {
            int i = 0;
            firstName = i < args.length ? args[i++] : "Georgiy";
            lastName = i < args.length ? args[i++] : "Korneev";
            passportId = i < args.length ? args[i++] : "4200";
            accountId = i < args.length ? args[i++] : "geo";
            amountDiff = i < args.length ? Integer.parseInt(args[i++]) : 100;
        } catch (NumberFormatException e) {
            System.err.println("Invalid arguments: " + e);
            System.err.println("Usage: <firstName> <lastName> <passportId> <accountId> <amountDiff> ");
            return;
        }

        Person person = bank.getPerson(passportId, false);
        if (person == null) {
            System.out.println("Creating person");
            person = bank.createPerson(passportId, firstName, lastName);
        } else {
            System.out.println("Person already exists");

            final String actualFirstName = person.getFirstName();
            if (!actualFirstName.equals(firstName)) {
                System.err.printf("First name doesn't match. Expected: '%s', found: '%s'%n", actualFirstName, firstName);
                return;
            }

            final String actualLastName = person.getLastName();
            if (!actualLastName.equals(lastName)) {
                System.err.printf("Last name doesn't match. Expected: '%s', found: '%s'%n", actualLastName, lastName);
                return;
            }
        }

        Account account = person.getAccount(accountId);
        if (account == null) {
            System.out.println("Creating account");
            account = person.createAccount(accountId);
        } else {
            System.out.println("Account already exists");
        }

        System.out.println("Account id: " + account.getId());
        System.out.println("Money: " + account.getAmount());
        System.out.println("Update money");
        account.setAmount(account.getAmount() + amountDiff);
        System.out.println("Money: " + account.getAmount());
    }
}
