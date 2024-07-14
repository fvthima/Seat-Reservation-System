import java.util.List;

public class User {
    private final String username;
    private final String password;
    private final List<String> reservedSeats;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.reservedSeats = getReservedSeats();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getReservedSeats() {
        return reservedSeats;
    }

}