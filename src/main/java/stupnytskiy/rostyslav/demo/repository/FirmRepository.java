package stupnytskiy.rostyslav.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stupnytskiy.rostyslav.demo.entity.Firm;

@Repository
public interface FirmRepository extends JpaRepository<Firm,Long> {
}
