package info.kgeorgiy.ja.koton.bank.person;

import info.kgeorgiy.ja.koton.bank.account.Account;

import java.util.Map;
import java.util.Objects;

public abstract class AbstractPerson implements Person {
    private final String id;
    private final String firstName;
    private final String lastName;

    public AbstractPerson(final String id, final String firstName, final String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String getPassportId() {
        return id;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public abstract Map<String, ? extends Account> getAccounts();

    public String getAccountId(final String id) {
        return getPassportId() + ":" + id;
    }


    @Override
    public boolean equals(final Object o) {
        return this == o
            || o instanceof final AbstractPerson that
            && Objects.equals(getPassportId(), that.getPassportId()) &&
            Objects.equals(getFirstName(), that.getFirstName()) &&
            Objects.equals(getLastName(), that.getLastName()) &&
            Objects.equals(getAccounts(), that.getAccounts());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPassportId(), getFirstName(), getLastName(), getAccounts());
    }
}
