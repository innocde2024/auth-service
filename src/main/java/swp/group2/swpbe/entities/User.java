package swp.group2.swpbe.entities;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import swp.group2.swpbe.constant.Gender;
import swp.group2.swpbe.constant.State;
import swp.group2.swpbe.constant.UserRole;

@Entity
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "full_name")
    private String fullName;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private State state;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @Column(name = "avatar_url")
    private String avatarUrl;
    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "updated_at")
    private Date updatedAt;
    private String about;
    private Date dob;
    @Enumerated(EnumType.STRING)
    private Gender gender;

    public User(String fullName, String email, String password, State state) {
        this.fullName = fullName;
        this.email = email;
        this.state = state;
        this.role = UserRole.USER;
        this.avatarUrl = null;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.password = password;
        this.about = null;
        this.dob = null;
        this.gender = null;
    }

    public User() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }

}