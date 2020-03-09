package stupnytskiy.rostyslav.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stupnytskiy.rostyslav.demo.entity.Realtor;

@Repository
public interface RealtorRepository extends JpaRepository<Realtor,Long> {
}
