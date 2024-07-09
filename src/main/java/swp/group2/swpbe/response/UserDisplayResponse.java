package swp.group2.swpbe.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDisplayResponse {
    private int id;
    private String fullName;
    private String email;
    private String avatarUrl;

}
