package coordinator;

import commonlib.interfaces.IClientCallback;
import commonlib.interfaces.ICoordinator;
import commonlib.interfaces.INode;
import commonlib.models.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;
import java.util.concurrent.locks.ReentrantLock;
import java.io.*;

public class CoordinatorImpl extends UnicastRemoteObject implements ICoordinator {
    private final UserManager userManager = new UserManager();
    private final Map<String, Token> activeTokens = new ConcurrentHashMap<>();
    private final static Map<String, List<String>> departmentFiles = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> fileLocks = new ConcurrentHashMap<>();
    private final Map<String, Set<IClientCallback>> fileListeners = new ConcurrentHashMap<>();
    private final Map<String, INode> nodes = new HashMap<>();
    private final CoordinatorSocketServer socketServer;
    private  DepartmentFileManager departmentFileDAO = null;

    public CoordinatorImpl() throws RemoteException {
        super();
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/distributed_fs", "root", "");
             departmentFileDAO = new DepartmentFileManager(conn);

            Registry devRegistry = LocateRegistry.getRegistry("localhost", 1100);
            nodes.put("Development", (INode) devRegistry.lookup("NodeService_Development"));
            Registry qaRegistry = LocateRegistry.getRegistry("localhost", 1101);
            nodes.put("Qa", (INode) qaRegistry.lookup("NodeService_Qa"));
            Registry desRegistry = LocateRegistry.getRegistry("localhost", 1102);
            nodes.put("Design", (INode) desRegistry.lookup("NodeService_Design"));
            System.out.println("nodes connected successfully");
        } catch (Exception e) {
            System.out.println("error connect node " + e.getMessage());
        }
        socketServer = new CoordinatorSocketServer(departmentFiles, fileLocks, nodes);
        socketServer.syncEveryFiveMinutes();
        socketServer.start();
    }

    @Override
    public boolean login(String username, String password) throws RemoteException {
        User user = userManager.getUserByUsername(username);
        return user != null && user.getPassword().equals(password);
    }

    @Override
    public Token generateToken(String username) throws RemoteException {
        Token token = new Token(username);
        activeTokens.put(username, token);
        return token;
    }

    @Override
    public void registerUser(User user) throws RemoteException {
        userManager.registerUser(user);
    }

    @Override
    public FileData requestFile(String filename, Token token) throws RemoteException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Callable<FileData>> searchTasks = new ArrayList<>();

        try {

            List<String> departments = departmentFileDAO.getAllDepartments();
            System.out.println(departments);

            for (String department : departments) {
                if (!departmentFileDAO.fileExists(department, filename)) {
                    continue;
                }

                INode node = nodes.get(department);

                searchTasks.add(() -> {
                    try {
                        if (node != null) {

                            return node.readFile(filename, token);
                        }
                    } catch (RemoteException e) {
                        System.out.println("Node unreachable for " + department + ", trying backup...");
                    }

                    Path backupPath = Paths.get("./backup/" + department + "/files/" + filename);
                    System.out.println(Files.exists(backupPath));
                    if (Files.exists(backupPath)) {

                        byte[] content = Files.readAllBytes(backupPath);
                        return new FileData(content);
                    }

                    return null;
                });
            }

            List<Future<FileData>> results = executor.invokeAll(searchTasks);
            for (Future<FileData> future : results) {
                FileData data = future.get();
                if (data != null) {
                    executor.shutdownNow();
                    return data;
                }
            }

        } catch (Exception e) {
            System.out.println("Error in requestFile: " + e.getMessage());
        } finally {
            executor.shutdown();
        }

        return null;
    }


    @Override
    public void routeFileOperation(FileRequest request) throws RemoteException {
        Token token = request.getToken();
        User user = userManager.getUserByUsername(token.getUserId());
        if (user == null || !user.getDepartment().equals(request.getDepartment())) {
            System.out.println("Permission not enough");
            return;
        }

        String department = user.getDepartment();
        String filename = request.getFilename();
        ReentrantLock lock = fileLocks.computeIfAbsent(filename, f -> new ReentrantLock());
        lock.lock();

        try {
            Path backupPath = Paths.get("./backup/" + department + "/files/" + filename);

            if (request.getOperation() == FileRequest.OperationType.ADD ||
                    request.getOperation() == FileRequest.OperationType.MODIFY) {


                Files.createDirectories(backupPath.getParent());


                Files.write(backupPath, request.getContent());
                System.out.println("Backup updated for " + filename);


                if (request.getOperation() == FileRequest.OperationType.ADD) {
                    try {
                        departmentFileDAO.addFile(department, filename);
                        System.out.println("File added to DB: " + filename);
                    } catch (SQLException e) {
                        System.out.println("Database error on ADD/MODIFY: " + e.getMessage());
                    }
                }

            } else if (request.getOperation() == FileRequest.OperationType.DELETE) {


                Files.deleteIfExists(backupPath);
                System.out.println("Deleted backup file: " + backupPath);


                try {
                   departmentFileDAO.removeFile(department, filename);

                } catch (SQLException e) {
                    System.out.println("Database error on DELETE: " + e.getMessage());
                }
            }

            // إرسال طلب المزامنة إلى النود الخاص بالقسم
            socketServer.queueSyncRequest(department, request);

        } catch (IOException e) {
            System.out.println("Error in routeFileOperation: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<String> getAvailableFiles(String department, Token token) throws RemoteException {
        return departmentFiles.getOrDefault(department, new ArrayList<>());
    }
}