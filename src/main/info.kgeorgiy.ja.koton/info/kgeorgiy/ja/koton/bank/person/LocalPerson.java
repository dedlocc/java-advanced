package info.kgeorgiy.ja.koton.bank.person;

import info.kgeorgiy.ja.koton.bank.account.LocalAccount;

import java.io.Serializable;
import java.util.Map;

public class LocalPerson extends AbstractPerson implements Serializable {
    private final Map<String, LocalAccount> accounts;

    public LocalPerson(final long id, final String firstName, final String lastName, final Map<String, LocalAccount> accounts) {
        super(id, firstName, lastName);
        this.accounts = accounts;
    }

    @Override
    public Map<String, LocalAccount> getAccounts() {
        return accounts;
    }

    @Override
    public LocalAccount createAccount(String id) {
        final String fullId = getAccountId(id);
        System.out.println("Creating local account " + fullId);
        return accounts.computeIfAbsent(id, k -> new LocalAccount(id, 0));
    }

    @Override
    public LocalAccount getAccount(String id) {
        System.out.println("Retrieving local account " + getAccountId(id));
        return accounts.get(id);
    }
}
