package swp.group2.swpbe.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginSocialDTO {
    private String name;
    private String picture;
    private String email;

    public LoginSocialDTO(String name, String picture, String email) {
        this.name = name;
        this.picture = picture;
        this.email = email;
    }

}
