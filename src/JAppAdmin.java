import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class JAppAdmin extends SeatReservationSystem {

    public void displayReservations() {
        try {
            File userFile = new File("Seats.txt");
            try (Scanner out = new Scanner(userFile)) {
				while(out.hasNextLine()) {
				    System.out.println(out.nextLine());
				}
			}
        }
        catch(FileNotFoundException e) {
            System.err.println("Error: Seats.txt file not found.");
        }
    }

    void displayUserList() {
        try {
            File userFile = new File("Users.txt");
            try (Scanner out = new Scanner(userFile)) {
				while(out.hasNextLine()) {
				    String details = out.nextLine();
				    String [] split = details.split(" ");
				    System.out.println(split[0]);
				}
			}

        }
        catch(FileNotFoundException e) {
            System.err.println("Error: Users.txt file not found.");
        }
    }
}
