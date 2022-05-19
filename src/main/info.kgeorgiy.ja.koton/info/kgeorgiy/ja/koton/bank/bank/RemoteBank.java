package info.kgeorgiy.ja.koton.bank.bank;

import info.kgeorgiy.ja.koton.bank.account.Account;
import info.kgeorgiy.ja.koton.bank.account.LocalAccount;
import info.kgeorgiy.ja.koton.bank.person.LocalPerson;
import info.kgeorgiy.ja.koton.bank.person.Person;
import info.kgeorgiy.ja.koton.bank.person.RemotePerson;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, RemotePerson> persons = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account getAccount(final String id) throws RemoteException {
        final StringTokenizer tokenizer = new StringTokenizer(id, ":");
        if (tokenizer.countTokens() == 2) {
            final Person person = getPerson(tokenizer.nextToken(), false);
            if (person != null) {
                return person.getAccount(tokenizer.nextToken());
            }
        }
        return null;
    }

    @Override
    public Person createPerson(final String passportId, final String firstName, final String lastName) throws RemoteException {
        System.out.printf("Creating person %s (%s %s)%n", passportId, firstName, lastName);
        final RemotePerson person = new RemotePerson(passportId, firstName, lastName, port);
        if (persons.putIfAbsent(passportId, person) == null) {
            UnicastRemoteObject.exportObject(person, port);
            return person;
        } else {
            return getPerson(passportId, false);
        }
    }

    @Override
    public Person getPerson(final String passportId, final boolean local) throws RemoteException {
        System.out.printf("Retrieving person %s (%s)%n", passportId, local ? "local" : "remote");
        final RemotePerson person = persons.get(passportId);
        return local && person != null ? new LocalPerson(
            person.getPassportId(),
            person.getFirstName(),
            person.getLastName(),
            person.getAccounts().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new LocalAccount(e.getValue())))
        ) : person;
    }
}
