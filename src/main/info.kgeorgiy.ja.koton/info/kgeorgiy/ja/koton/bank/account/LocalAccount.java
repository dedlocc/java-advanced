package info.kgeorgiy.ja.koton.bank.account;

public class LocalAccount implements Account {
    private final String id;
    private int amount;

    public LocalAccount(final RemoteAccount account) {
        this(account.getId(), account.getAmount());
    }

    public LocalAccount(final String id, final int amount) {
        this.id = id;
        this.amount = amount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getAmount() {
        System.out.println("Getting amount of money for account " + id);
        return amount;
    }

    @Override
    public void setAmount(final int amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount = amount;
    }
}
