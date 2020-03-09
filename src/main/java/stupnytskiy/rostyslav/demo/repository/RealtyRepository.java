package stupnytskiy.rostyslav.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stupnytskiy.rostyslav.demo.entity.Realty;

@Repository
public interface RealtyRepository extends JpaRepository<Realty,Long> {
}
