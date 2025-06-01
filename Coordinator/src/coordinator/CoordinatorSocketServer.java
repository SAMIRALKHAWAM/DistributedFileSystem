package coordinator;

import commonlib.interfaces.INode;
import commonlib.models.FileRequest;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class CoordinatorSocketServer {
    private final int port = 9999;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    private final Map<String, List<String>> departmentFiles;
    private final Map<String, ReentrantLock> fileLocks;
    private final Map<String, INode> nodes;
    private static final Map<String, List<FileRequest>> pendingRequests = new ConcurrentHashMap<>();

    public CoordinatorSocketServer(Map<String, List<String>> departmentFiles,
                                   Map<String, ReentrantLock> fileLocks,
                                   Map<String, INode> nodes) {
        this.departmentFiles = departmentFiles;
        this.fileLocks = fileLocks;
        this.nodes = nodes;
    }

    public void start() {
        executor.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Socket server started on port " + port);
                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Accepted socket connection: " + socket);
                    executor.submit(() -> processPendingRequests());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void queueSyncRequest(String department, FileRequest request) {
        pendingRequests.computeIfAbsent(department, k -> new ArrayList<>()).add(request);
    }

    public void processPendingRequests() {
        for (String department : pendingRequests.keySet()) {
            List<FileRequest> requests = new ArrayList<>(pendingRequests.getOrDefault(department, new ArrayList<>()));
            INode node = nodes.get(department);
            if (node != null) {
                for (FileRequest req : requests) {
                    try {
                        node.writeFile(req);
                        System.out.println("Synced file to node: " + req.getFilename());
                        pendingRequests.get(department).remove(req);
                    } catch (RemoteException e) {
                        System.out.println("Node not reachable: " + department);
                    }
                }
            }
        }
    }

    public void syncEveryFiveMinutes() {
        executor.scheduleAtFixedRate(this::processPendingRequests, 0, 1, TimeUnit.MINUTES);
    }
}
