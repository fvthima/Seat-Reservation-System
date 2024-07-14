import java.io.*;
import java.util.*;

public class JApp extends SeatReservationSystem {
    private final SeatReservationSystem seatReservationSystem;
    private final Scanner scanner = new Scanner(System.in);
    private final List<User> users = new ArrayList<>();

    public JApp() {
        seatReservationSystem = new SeatReservationSystem();
    }

    public void run() {
        boolean exit = false;

        while (!exit) {
            printMainMenu();
            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // consume the newline character
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // consume the invalid input
                continue; // skip to the next iteration of the loop
            }

            switch (choice) {
                case 1 -> seatReservationSystem.login();
                case 2 -> register();
                case 3 -> loginAdmin();
                case 4 -> {
                    System.out.println("See you soon!");
                    exit = true;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n╔══════════════════════════╗");
        System.out.println("║ \033[1m\033[36m    WELCOME TO JApp!\033[0m     ║");
        System.out.println("╠══════════════════════════╣");
        System.out.println("║\033[33m 1. Login\033[0m                 ║");
        System.out.println("║\033[33m 2. Create an account\033[0m     ║");
        System.out.println("║\033[33m 3. Login as Admin\033[0m        ║");
        System.out.println("║\033[33m 4. Exit\033[0m                  ║");
        System.out.println("╚══════════════════════════╝");
        System.out.print("\n\033[33mEnter your choice:\033[0m ");
    }


    private void register() {
        System.out.print("Enter username: ");
        String username = scanner.next();

        // Check if the username already exists in the file
        if (checkUsernameExists(username)) {
            System.out.println("Username already exists. Please try a different username.");
            return; // Return to the main menu
        }

        System.out.print("Enter password: ");
        String password = scanner.next();
        System.out.print("Account created successfully!");
        users.add(new User(username, password));
        addUsersToFile();
    }

    private boolean checkUsernameExists(String username) {
        try (Scanner scan = new Scanner(new File("Users.txt"))) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                String[] parts = line.split(" ");
                if (parts.length >= 1 && parts[0].equals(username)) {
                    return true; // Username already exists in the file
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void addUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Users.txt", true))) {
            for (User user : users) {
                writer.write(user.getUsername() + " ");
                writer.write(user.getPassword());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        JApp main = new JApp();
        main.run();
    }
}