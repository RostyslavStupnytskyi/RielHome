package stupnytskiy.rostyslav.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stupnytskiy.rostyslav.demo.entity.StreetType;

@Repository
public interface StreetTypeRepository extends JpaRepository<StreetType,Long> {
}
