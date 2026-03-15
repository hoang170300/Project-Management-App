package taskmanagement.repository;

import taskmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByIdAndDeletedFalse(Long id);
    List<User> findByDeletedFalse();

    default Optional<User> findByIdActive(Long id) {
        return findByIdAndDeletedFalse(id);
    }

    default List<User> findAllActive() {
        return findByDeletedFalse();
    }

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsernameAndDeletedFalse(String username);
    boolean existsByEmailAndDeletedFalse(String email);
}