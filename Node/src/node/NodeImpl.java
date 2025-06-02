package node;

import commonlib.models.*;
import commonlib.interfaces.*;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class NodeImpl extends UnicastRemoteObject implements INode {

    private final String department;
    private final Map<String, byte[]> files = new HashMap<>();

    public NodeImpl(String department) throws RemoteException {
        super();
        this.department = department;
    }

    @Override
    public FileData readFile(String filename, Token token) throws RemoteException {
        byte[] data = files.get(filename);
        if (data == null) {
            Path path = Paths.get("./backup/" + department + "/files/" + filename); // adjust path if needed
           System.out.println(Files.exists(path));
            try {
                if (Files.exists(path)) {
                    data = Files.readAllBytes(path);
                    return new FileData(data);
                }
            } catch (IOException e) {
                System.out.println("Error reading file from disk: " + e.getMessage());
            }
            return null;
        }
        return new FileData(data);
    }

    @Override
    public void writeFile(FileRequest request) throws RemoteException {
        try {
            Path filePath = Paths.get("./nodes/" + request.getDepartment() + "/files/" + request.getFilename());


            Files.createDirectories(filePath.getParent());
            String message = request.getDepartment() + ";" + request.getFilename() + ";" + request.getOperation() + ";" + Base64.getEncoder().encodeToString(request.getContent());
            try (Socket socket = new Socket("localhost", 9999);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }

            switch (request.getOperation()) {
                case ADD:
                case MODIFY:

                    Files.write(filePath, request.getContent());
                    files.put(request.getFilename(), request.getContent());
                    System.out.println("file add / edit successfully " + request.getFilename());
                    break;

                case DELETE:
                    Files.deleteIfExists(filePath);
                    files.remove(request.getFilename());
                    System.out.println("file deleted " + request.getFilename());
                    break;
            }
        } catch (Exception e) {
            System.out.println("error in execute " + e.getMessage());
        }
    }

    @Override
    public List<String> listFiles() throws RemoteException {
        return new ArrayList<>(files.keySet());
    }
}