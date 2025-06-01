package clientapp;

import com.mysql.cj.xdevapi.ClientImpl;
import commonlib.interfaces.IClientCallback;
import commonlib.interfaces.ICoordinator;
import commonlib.models.*;
import coordinator.UserManager;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;


public class TestModels {


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserManager userManager = new UserManager();





        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ICoordinator coordinator = (ICoordinator) registry.lookup("CoordinatorService");

            System.out.print("Enter username: ");
            String username = scanner.nextLine();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();


            if (coordinator.login(username, password)) {
                Token token = coordinator.generateToken(username);
                User user = userManager.getUserByUsername(username);

                System.out.print("Enter File Name: ");
                String filename = scanner.nextLine();

                System.out.print("Enter File Content: ");
                String content = scanner.nextLine();

                byte[] fileBytes = content.getBytes();

                FileRequest request = new FileRequest(
                        filename + ".txt",
                        user.getDepartment(),
                        FileRequest.OperationType.ADD,
                        token,
                        fileBytes
                );

                coordinator.routeFileOperation(request);

                System.out.println(coordinator.getAvailableFiles("Development",token));

                FileData file = coordinator.requestFile("samir.txt", token);
                if (file != null) {
                    System.out.println("file found");
                    //Files.write(Paths.get("downloaded_report.docx"), file.getData());
                } else {
                    System.out.println("file doesn't exist in any node");
                }



                System.out.println("x");

            } else {
                System.out.println("s");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
