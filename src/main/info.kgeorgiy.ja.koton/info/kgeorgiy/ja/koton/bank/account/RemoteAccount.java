package info.kgeorgiy.ja.koton.bank.account;

public class RemoteAccount extends AbstractAccount {
    public RemoteAccount(final String id) {
        super(id, 0);
    }

    @Override
    public synchronized int getAmount() {
        return super.getAmount();
    }

    @Override
    public synchronized void setAmount(final int amount) {
        super.setAmount(amount);
    }
}
