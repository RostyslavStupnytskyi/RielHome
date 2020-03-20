package stupnytskiy.rostyslav.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stupnytskiy.rostyslav.demo.entity.Address;
import stupnytskiy.rostyslav.demo.entity.Firm;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address,Long> {
    Page<Address> findAllByFirmId(Long id, Pageable pageable);
    List<Address> findAllByFirmId(Long id);
    Address findByIdAndFirm(Long id, Firm firm);
}
