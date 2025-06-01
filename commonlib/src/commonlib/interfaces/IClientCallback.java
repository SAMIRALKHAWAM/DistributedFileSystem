package commonlib.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClientCallback extends Remote {
    void notifyFileModified(String filename) throws RemoteException;
}
