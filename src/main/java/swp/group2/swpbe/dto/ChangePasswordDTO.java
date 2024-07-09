package swp.group2.swpbe.dto;

public class ChangePasswordDTO {

    private String token;
    private String newPassword;

    public ChangePasswordDTO() {
    }

    public ChangePasswordDTO(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return this.newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

}
