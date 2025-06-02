package clientapp;


import commonlib.interfaces.IClientCallback;
import commonlib.interfaces.ICoordinator;
import commonlib.models.*;
import coordinator.UserManager;

import java.rmi.RemoteException;
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

            while (true) {
                System.out.print("Enter Choice:");
                String choice = scanner.nextLine();
                switch (choice) {
                    case "1":

                        System.out.print("Enter username: ");
                        String username = scanner.nextLine();

                        System.out.print("Enter password: ");
                        String password = scanner.nextLine();
                        if (SessionManager.getToken() != null) {
                            System.out.println("already login with token " + SessionManager.getToken());
                            break;
                        }
                        if (coordinator.login(username, password)) {
                            Token token = coordinator.generateToken(username);
                            System.out.println(token);
                            System.out.println("Login Successfully");
                            SessionManager.setToken(token);
                            break;
                        }
                        System.out.println("Login Failed");
                        break;
                    case "2":
                        if (SessionManager.getToken() != null) {
                            Token token = SessionManager.getToken();
                            User user = userManager.getUserByUsername(token.getUserId());
                            System.out.println(user.getRole());
                            if (user.getRole().equals("manager")) {
                                System.out.print("Enter username: ");
                                String newusername = scanner.nextLine();

                                System.out.print("Enter password: ");
                                String newpassword = scanner.nextLine();

                                System.out.print("Enter role: (manager , employee) :");
                                String newrole = scanner.nextLine();
                                while (!newrole.equals("manager") && !newrole.equals("employee")) {
                                    System.out.print("Enter role: (manager , employee) :");
                                    newrole = scanner.nextLine();
                                }

                                System.out.print("Enter department: (Development , Qa , Design) :");
                                String newdepartment = scanner.nextLine();
                                while (!newdepartment.equals("Development") && !newdepartment.equals("Qa") && !newdepartment.equals("Design")) {
                                    System.out.print("Enter department: (Development , Qa , Design) :");
                                    newdepartment = scanner.nextLine();
                                }

                                User newuser = new User(newusername, newpassword, newrole, newdepartment);
                                coordinator.registerUser(newuser);
                                System.out.println("User Created Successfully");
                                break;
                            } else {
                                System.out.println("Not enough permissions");
                                break;
                            }


                        }
                        System.out.println("Login needed");
                        break;

                    case "3":
                        if (SessionManager.getToken() != null) {
                            Token token = SessionManager.getToken();
                            User user = userManager.getUserByUsername(token.getUserId());
                            String department = user.getDepartment();

                            System.out.print("Enter file name :  ");
                            String filename = scanner.nextLine() + ".txt";

                            System.out.print("Enter file content :  ");
                            byte[] filecontent = scanner.nextLine().getBytes();
                            FileRequest fileRequest = new FileRequest(filename, department, FileRequest.OperationType.ADD, token, filecontent);
                            coordinator.routeFileOperation(fileRequest);
                            System.out.println("file added successfully : " + filename);
                            break;
                        }
                        System.out.println("Login needed");
                        break;
                    case "4":
                        if (SessionManager.getToken() != null) {
                            Token token = SessionManager.getToken();
                            User user = userManager.getUserByUsername(token.getUserId());
                            String department = user.getDepartment();

                            System.out.print("Enter file name :  ");
                            String newfilename = scanner.nextLine() + ".txt";

                            System.out.print("Enter file name :  ");
                            byte[] newfilecontent = scanner.nextLine().getBytes();

                            FileRequest fileRequest = new FileRequest(newfilename, department, FileRequest.OperationType.MODIFY, token, newfilecontent);
                            coordinator.routeFileOperation(fileRequest);
                            System.out.println("file Modified successfully : " + newfilename);
                            break;
                        }
                        System.out.println("Login needed");
                        break;
                    case "5":
                        if (SessionManager.getToken() != null) {
                            Token token = SessionManager.getToken();
                            User user = userManager.getUserByUsername(token.getUserId());
                            String department = user.getDepartment();

                            System.out.print("Enter file name :  ");
                            String filename = scanner.nextLine() + ".txt";

                            System.out.print("Enter approve :  ( 1 ,0 ) :");
                            byte[] newcontent = scanner.nextLine().getBytes();

                            while (newcontent.length != 1 || (newcontent[0] == '0' && newcontent[0] == '1')) {
                                System.out.println("Invalid input. Please enter '1' or '0': ");
                                newcontent = scanner.nextLine().getBytes();
                            }
                            if (newcontent[0] == '1') {

                                FileRequest fileRequest = new FileRequest(filename, department, FileRequest.OperationType.DELETE, token, newcontent);
                                coordinator.routeFileOperation(fileRequest);
                                System.out.println("file Deleted successfully : " + filename);
                                break;
                            } else {
                                System.out.println("file Deleted ignored : " + filename);
                                break;
                            }
                        }
                        System.out.println("Login needed");
                        break;
                    case "6":
                        if (SessionManager.getToken() != null) {
                            Token token = SessionManager.getToken();
                            User user = userManager.getUserByUsername(token.getUserId());
                            String department = user.getDepartment();

                            System.out.print("Enter file name: ");
                            String filename = scanner.nextLine().trim();
                            if (!filename.endsWith(".txt")) {
                                filename += ".txt";
                            }

                            try {
                                FileData file = coordinator.requestFile(filename, token);

                                if (file != null) {
                                    System.out.println(" File found and read successfully.");

                                    System.out.println("Content:");
                                    System.out.println(new String(file.getData()));
                                } else {
                                    System.out.println(" File doesn't exist in any node or backup.");
                                }

                            } catch (RemoteException e) {
                                System.out.println("Error contacting coordinator: " + e.getMessage());
                            }

                        } else {
                            System.out.println(" Login required.");
                        }
                        break;

                }


            }


//            if (coordinator.login(username, password)) {
//                Token token = coordinator.generateToken(username);
//                //User user = userManager.getUserByUsername(username);
//
//                System.out.print("Enter File Name: ");
//                String filename = scanner.nextLine();
//
//                System.out.print("Enter File Content: ");
//                String content = scanner.nextLine();
//
//                byte[] fileBytes = content.getBytes();
//
//                FileRequest request = new FileRequest(
//                        filename + ".txt",
//                        user.getDepartment(),
//                        FileRequest.OperationType.ADD,
//                        token,
//                        fileBytes
//                );
//
//                coordinator.routeFileOperation(request);
//
//                System.out.println(coordinator.getAvailableFiles("Development",token));
//
//                FileData file = coordinator.requestFile("samir.txt", token);
//                if (file != null) {
//                    System.out.println("file found");
//                } else {
//                    System.out.println("file doesn't exist in any node");
//                }
//
//
//
//                System.out.println("x");

//            } else {
//                System.out.println("s");
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
