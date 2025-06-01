package commonlib.interfaces;

import commonlib.models.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface INode extends Remote {
    FileData readFile(String filename, Token token) throws RemoteException;

    void writeFile(FileRequest request) throws RemoteException;

    List<String> listFiles() throws RemoteException;
}
