package swp.group2.swpbe.repository;

import org.springframework.stereotype.Repository;
import swp.group2.swpbe.constant.UserRole;
import swp.group2.swpbe.entities.User;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findById(int id);

    User findByEmail(String email);

    List<User> findByRole(UserRole role);
}