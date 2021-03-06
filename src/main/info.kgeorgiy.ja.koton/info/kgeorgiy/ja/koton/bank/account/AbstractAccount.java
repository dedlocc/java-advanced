package info.kgeorgiy.ja.koton.bank.account;

public abstract class AbstractAccount implements Account {
    private final String id;
    private int amount;

    public AbstractAccount(final String id, final int amount) {
        this.id = id;
        this.amount = amount;
    }

    public AbstractAccount(final String id) {
        this(id, 0);
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
