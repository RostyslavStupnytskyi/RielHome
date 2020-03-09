package stupnytskiy.rostyslav.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stupnytskiy.rostyslav.demo.entity.Region;

@Repository
public interface RegionRepository extends JpaRepository<Region,Long> {
}
