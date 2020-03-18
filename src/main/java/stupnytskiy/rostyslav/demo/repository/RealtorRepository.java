package stupnytskiy.rostyslav.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stupnytskiy.rostyslav.demo.entity.Firm;
import stupnytskiy.rostyslav.demo.entity.Realtor;

import java.util.Optional;

@Repository
public interface RealtorRepository extends JpaRepository<Realtor,Long> {
//    Optional<Realtor> findByLogin(String login);
//    boolean existsByLogin(String login);
}
