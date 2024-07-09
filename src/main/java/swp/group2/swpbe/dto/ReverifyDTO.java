package swp.group2.swpbe.dto;

public class ReverifyDTO {
    private String email;

    // Constructors
    public ReverifyDTO() {
        // Default constructor
    }

    public ReverifyDTO(String email) {

        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
