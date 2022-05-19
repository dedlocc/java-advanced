package info.kgeorgiy.ja.koton.bank.bank;

import info.kgeorgiy.ja.koton.bank.account.Account;
import info.kgeorgiy.ja.koton.bank.person.Person;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    /**
     * Returns account by identifier.
     *
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exist.
     */
    Account getAccount(String id) throws RemoteException;

    /**
     * Creates a new person with specified passport and name if it does not already exist.
     *
     * @param passportId passport number
     * @param firstName  first name
     * @param lastName   last name
     * @return created or existing person.
     */
    Person createPerson(String passportId, String firstName, String lastName) throws RemoteException;

    /**
     * Returns person by passport number.
     *
     * @param passportId passport number
     * @param local      whether local person is requested
     * @return person with specified id or {@code null} if such person does not exist.
     */
    Person getPerson(String passportId, boolean local) throws RemoteException;
}
