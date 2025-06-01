package clientapp;

import commonlib.interfaces.ICoordinator;
import commonlib.models.Token;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class LoginScreen {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ICoordinator coordinator = (ICoordinator) registry.lookup("CoordinatorService");

            if (coordinator.login("ali", "1234")) {
                Token token = coordinator.generateToken("samir");
                SessionManager.setToken(token);
                System.out.println("login successfully , Token : " + token.getSessionId());
            } else {
                System.out.println("login error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
