package info.kgeorgiy.ja.koton.bank.person;

import info.kgeorgiy.ja.koton.bank.account.Account;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Person extends Remote {
    /**
     * Returns identification document number.
     */
    long getId() throws RemoteException;

    /**
     * Returns first name.
     */
    String getFirstName() throws RemoteException;

    /**
     * Returns last name.
     */
    String getLastName() throws RemoteException;

    /**
     * Returns all accounts belonging to the person mapped by their identifiers.
     */
    Map<String, ? extends Account> getAccounts() throws RemoteException;

    /**
     * Creates a new account with specified identifier if it does not already exist.
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exist.
     */
    Account getAccount(String id) throws RemoteException;
}