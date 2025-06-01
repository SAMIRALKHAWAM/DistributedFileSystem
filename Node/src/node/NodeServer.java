package node;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class NodeServer {

    public static void main(String[] args) {
        // Set default values if no arguments are provided
        ArrayList<String> department = new ArrayList<String>();
        department.add("Development");
        department.add("Qa");
        department.add("Design");

        try {
           int port = 1100;
            for (int i=0 ; i< department.toArray().length ;i++) {
                NodeImpl node = new NodeImpl(department.get(i));
                Registry registry = LocateRegistry.createRegistry(port);
                registry.rebind("NodeService_" + department.get(i), node);

                System.out.println("node connect" + department.get(i) + " on port " + port);
                port ++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}