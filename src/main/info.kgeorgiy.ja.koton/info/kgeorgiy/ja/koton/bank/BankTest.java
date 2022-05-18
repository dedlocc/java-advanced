package info.kgeorgiy.ja.koton.bank;

import info.kgeorgiy.ja.koton.bank.account.Account;
import info.kgeorgiy.ja.koton.bank.bank.Bank;
import info.kgeorgiy.ja.koton.bank.bank.RemoteBank;
import info.kgeorgiy.ja.koton.bank.person.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class BankTest {
    private static Registry registry;
    private static final int REGISTRY_PORT = 8901;
    private static final int BANK_PORT = 8902;
    private static final String BANK_URL = "//localhost/bank";

    private static final Random rng = new Random(774229058805375986L);

    @BeforeClass
    public static void beforeClass() throws RemoteException {
        registry = LocateRegistry.createRegistry(REGISTRY_PORT);
    }

    //

    private Bank bank;

    @Before
    public void setUp() throws RemoteException {
        bank = new RemoteBank(BANK_PORT);
        UnicastRemoteObject.exportObject(bank, BANK_PORT);
        registry.rebind(BANK_URL, bank);
        System.out.println("Server started");
    }

    @After
    public void tearDown() throws RemoteException, NotBoundException {
        UnicastRemoteObject.unexportObject(bank, true);
        registry.unbind(BANK_URL);
        System.out.println("Server stopped");
    }

    //

    @Test
    public void createPerson() throws RemoteException {
        createRandomPerson();
    }

    @Test
    public void createAndGetRemotePerson() throws RemoteException {
        final Person person = createRandomPerson();
        final Person remotePerson = bank.getPerson(person.getId(), false);
        assertSame(person, remotePerson);
        assertEquals(person, remotePerson);
    }

    @Test
    public void createAndGetLocalPerson() throws RemoteException {
        final Person person = createRandomPerson();
        final Person localPerson = bank.getPerson(person.getId(), true);
        assertNotSame(person, localPerson);
        assertEquals(person, localPerson);
    }

    @Test
    public void getNonExistingRemotePerson() throws RemoteException {
        assertNull(bank.getPerson(randomPersonId(), false));
    }

    @Test
    public void getNonExistingLocalPerson() throws RemoteException {
        assertNull(bank.getPerson(randomPersonId(), true));
    }

    @Test
    public void createAccount() throws RemoteException {
        final Person person = createRandomPerson();
        final String accountId = randomAccountId();
        final Account account = person.createAccount(accountId);
        assertNotNull(account);
        assertEquals(person.getId() + ":" + accountId, account.getId());
        assertEquals(0, account.getAmount());
    }

    @Test
    public void getAccounts() throws RemoteException {
        final Person person = createRandomPerson();
        final String accountId1 = randomAccountId();
        final String accountId2 = randomAccountId();
        final Account account1 = person.createAccount(accountId1);
        final Account account2 = person.createAccount(accountId2);
        assertEquals(
            Map.of(accountId1, account1, accountId2, account2),
            person.getAccounts()
        );
    }

    @Test
    public void getPersonAccount() throws RemoteException {
        final Person person = createRandomPerson();
        final String accountId = randomAccountId();
        final Account account = person.createAccount(accountId);
        final Account gottenAccount = person.getAccount(accountId);
        assertSame(account, gottenAccount);
    }

    @Test
    public void getBankAccount() throws RemoteException {
        final Person person = createRandomPerson();
        final Account account = person.createAccount(randomAccountId());
        final Account gottenAccount = bank.getAccount(account.getId());
        assertSame(account, gottenAccount);
    }

    @Test
    public void modifyOriginalPerson() throws RemoteException {
        final Person person = createRandomPerson();
        final Person remotePerson = bank.getPerson(person.getId(), false);
        final Person localPerson = bank.getPerson(person.getId(), true);

        assertSame(person, remotePerson);
        assertEquals(localPerson, remotePerson);
        assertEquals(localPerson, person);

        final Account account = person.createAccount(randomAccountId());

        assertSame(person, remotePerson);
        assertNotEquals(localPerson, remotePerson);
        assertNotEquals(localPerson, person);

        assertNotNull(bank.getAccount(account.getId()));
    }

    @Test
    public void modifyRemotePerson() throws RemoteException {
        final Person person = createRandomPerson();
        final Person remotePerson = bank.getPerson(person.getId(), false);
        final Person localPerson = bank.getPerson(person.getId(), true);

        assertSame(person, remotePerson);
        assertEquals(localPerson, remotePerson);
        assertEquals(localPerson, person);

        final Account account = remotePerson.createAccount(randomAccountId());

        assertSame(person, remotePerson);
        assertNotEquals(localPerson, remotePerson);
        assertNotEquals(localPerson, person);

        assertNotNull(bank.getAccount(account.getId()));
    }

    @Test
    public void modifyLocalPerson() throws RemoteException {
        final Person person = createRandomPerson();
        final Person remotePerson = bank.getPerson(person.getId(), false);
        final Person localPerson = bank.getPerson(person.getId(), true);

        assertSame(person, remotePerson);
        assertEquals(localPerson, remotePerson);
        assertEquals(localPerson, person);

        final Account account = localPerson.createAccount(randomAccountId());

        assertSame(person, remotePerson);
        assertNotEquals(localPerson, remotePerson);
        assertNotEquals(localPerson, person);

        assertNull(bank.getAccount(account.getId()));
    }

    @Test
    public void modifyOriginalAccount() throws RemoteException {
        final Person person = createRandomPerson();
        final String accountId = randomAccountId();
        final Account account = person.createAccount(accountId);

        final Person remotePerson = bank.getPerson(person.getId(), false);
        final Person localPerson = bank.getPerson(person.getId(), true);

        assertEquals(0, person.getAccount(accountId).getAmount());
        assertEquals(0, remotePerson.getAccount(accountId).getAmount());
        assertEquals(0, localPerson.getAccount(accountId).getAmount());

        account.setAmount(42);

        assertEquals(42, person.getAccount(accountId).getAmount());
        assertEquals(42, remotePerson.getAccount(accountId).getAmount());
        assertEquals(0, localPerson.getAccount(accountId).getAmount());
    }

    @Test
    public void modifyRemoteAccount() throws RemoteException {
        final Person person = createRandomPerson();
        final String accountId = randomAccountId();
        person.createAccount(accountId);

        final Person remotePerson = bank.getPerson(person.getId(), false);
        final Person localPerson = bank.getPerson(person.getId(), true);

        assertEquals(0, person.getAccount(accountId).getAmount());
        assertEquals(0, remotePerson.getAccount(accountId).getAmount());
        assertEquals(0, localPerson.getAccount(accountId).getAmount());

        remotePerson.getAccount(accountId).setAmount(42);

        assertEquals(42, person.getAccount(accountId).getAmount());
        assertEquals(42, remotePerson.getAccount(accountId).getAmount());
        assertEquals(0, localPerson.getAccount(accountId).getAmount());
    }

    @Test
    public void modifyLocalAccount() throws RemoteException {
        final Person person = createRandomPerson();
        final String accountId = randomAccountId();
        person.createAccount(accountId);

        final Person remotePerson = bank.getPerson(person.getId(), false);
        final Person localPerson = bank.getPerson(person.getId(), true);

        assertEquals(0, person.getAccount(accountId).getAmount());
        assertEquals(0, remotePerson.getAccount(accountId).getAmount());
        assertEquals(0, localPerson.getAccount(accountId).getAmount());

        localPerson.getAccount(accountId).setAmount(42);

        assertEquals(0, person.getAccount(accountId).getAmount());
        assertEquals(0, remotePerson.getAccount(accountId).getAmount());
        assertEquals(42, localPerson.getAccount(accountId).getAmount());
    }

    //

    private Person createRandomPerson() throws RemoteException {
        final long id = randomPersonId();
        final String firstName = randomFirstName();
        final String lastName = randomLastName();

        final Person person = bank.createPerson(id, firstName, lastName);
        assertNotNull(person);

        assertEquals(id, person.getId());
        assertEquals(firstName, person.getFirstName());
        assertEquals(lastName, person.getLastName());
        assertTrue(person.getAccounts().isEmpty());
        return person;
    }

    private static long randomPersonId() {
        return rng.nextLong(0, Long.MAX_VALUE);
    }

    private static String randomFirstName() {
        return "first" + rng.nextInt(0, Integer.MAX_VALUE);
    }

    private static String randomLastName() {
        return "last" + rng.nextInt(0, Integer.MAX_VALUE);
    }

    private static String randomAccountId() {
        return "acc" + rng.nextInt(0, Integer.MAX_VALUE);
    }
}
