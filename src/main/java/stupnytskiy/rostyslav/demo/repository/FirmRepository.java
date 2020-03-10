package stupnytskiy.rostyslav.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stupnytskiy.rostyslav.demo.entity.Firm;
import stupnytskiy.rostyslav.demo.entity.User;

import java.util.Optional;

@Repository
public interface FirmRepository extends JpaRepository<Firm,Long> {
    Optional<Firm> findByLogin(String login);
    boolean existsByLogin(String login);
}
