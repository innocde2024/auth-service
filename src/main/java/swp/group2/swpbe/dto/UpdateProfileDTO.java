package swp.group2.swpbe.dto;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import swp.group2.swpbe.constant.Gender;

@Getter
@Setter
public class UpdateProfileDTO {
    private String fullName;
    private String about;
    private Date dob;
    private Gender gender;

    public UpdateProfileDTO() {
    }

    public UpdateProfileDTO(String fullName, String about, Date dob, Gender gender) {
        this.fullName = fullName;
        this.about = about;
        this.dob = dob;
        this.gender = gender;
    }

    public String getFullName() {
        return this.fullName;
    }

}
