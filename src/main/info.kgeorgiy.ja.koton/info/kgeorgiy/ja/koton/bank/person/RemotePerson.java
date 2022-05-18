package info.kgeorgiy.ja.koton.bank.person;

import info.kgeorgiy.ja.koton.bank.account.Account;
import info.kgeorgiy.ja.koton.bank.account.RemoteAccount;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemotePerson extends AbstractPerson {
    private final int port;
    private final ConcurrentMap<String, RemoteAccount> accounts = new ConcurrentHashMap<>();

    public RemotePerson(final long id, final String firstName, final String lastName, final int port) {
        super(id, firstName, lastName);
        this.port = port;
    }

    @Override
    public ConcurrentMap<String, RemoteAccount> getAccounts() {
        return accounts;
    }

    @Override
    public RemoteAccount createAccount(String id) throws RemoteException {
        final String fullId = getAccountId(id);
        System.out.println("Creating remote account " + fullId);
        final RemoteAccount account = new RemoteAccount(fullId);
        if (accounts.putIfAbsent(id, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccount(id);
        }
    }

    @Override
    public RemoteAccount getAccount(String id) {
        System.out.println("Retrieving remote account " + getAccountId(id));
        return accounts.get(id);
    }
}
