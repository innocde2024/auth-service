package swp.group2.swpbe.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupDTO {

    private String fullName;
    private String email;
    private String password;

    public SignupDTO() {

    }

    public SignupDTO(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

}
