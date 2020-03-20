package stupnytskiy.rostyslav.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import stupnytskiy.rostyslav.demo.entity.Realtor;


@Repository
public interface RealtorRepository extends JpaRepository<Realtor,Long>, JpaSpecificationExecutor<Realtor> {
    Page<Realtor> findAllByFirmUserLogin(String login, Pageable pageable);
}
