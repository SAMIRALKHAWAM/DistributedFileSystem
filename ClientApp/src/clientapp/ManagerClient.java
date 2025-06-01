package clientapp;

import commonlib.interfaces.ICoordinator;
import commonlib.models.User;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ManagerClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ICoordinator coordinator = (ICoordinator) registry.lookup("CoordinatorService");

            User user = new User("ali", "1234", "employee", "Design");
            coordinator.registerUser(user);

            System.out.println("login successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
