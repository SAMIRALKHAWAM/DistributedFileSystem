package commonlib.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import commonlib.models.*;

public interface ICoordinator extends Remote {
    boolean login(String username, String password) throws RemoteException;

    Token generateToken(String username) throws RemoteException;

    void registerUser(User user) throws RemoteException;

    FileData requestFile(String filename, Token token) throws RemoteException;

    void routeFileOperation(FileRequest request) throws RemoteException;

    List<String> getAvailableFiles(String department, Token token) throws RemoteException;

}
