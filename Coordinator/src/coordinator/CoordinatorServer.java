package coordinator;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;



public class CoordinatorServer {



    public static void main(String[] args) {
        try {
            CoordinatorImpl impl = new CoordinatorImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("CoordinatorService", impl);
            System.out.println(" Coordinator RMI Server  1099");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
