package swp.group2.swpbe.dto;

public class ResetPasswordDTO {

    private String email;

    public ResetPasswordDTO() {
    }

    public ResetPasswordDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
