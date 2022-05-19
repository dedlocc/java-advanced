package info.kgeorgiy.ja.koton.bank.account;

public class LocalAccount extends AbstractAccount {
    public LocalAccount(final RemoteAccount account) {
        super(account.getId(), account.getAmount());
    }

    public LocalAccount(final String id) {
        super(id);
    }
}
