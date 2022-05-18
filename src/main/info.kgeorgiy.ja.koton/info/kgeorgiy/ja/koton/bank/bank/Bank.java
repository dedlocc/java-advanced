package info.kgeorgiy.ja.koton.bank.bank;

import info.kgeorgiy.ja.koton.bank.account.Account;
import info.kgeorgiy.ja.koton.bank.person.Person;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    /**
     * Returns account by identifier.
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exist.
     */
    Account getAccount(String id) throws RemoteException;

    /**
     * Creates a new person with specified id and name if it does not already exist.
     * @param id identification document number
     * @param firstName first name
     * @param lastName last name
     * @return created or existing person.
     */
    Person createPerson(long id, String firstName, String lastName) throws RemoteException;

    /**
     * Returns person by identification document number.
     * @param id identification document number
     * @param local whether local person is requested
     * @return person with specified id or {@code null} if such person does not exist.
     */
    Person getPerson(long id, boolean local) throws RemoteException;
}
