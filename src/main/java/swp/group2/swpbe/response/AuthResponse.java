package swp.group2.swpbe.response;

import lombok.Getter;
import lombok.Setter;
import swp.group2.swpbe.entities.User;

@Getter
@Setter
public class AuthResponse {

    private User user;
    private String accessToken;
    private String refreshToken;

    public AuthResponse(User user, String accessToken, String refreshToken) {
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;

    }

}
