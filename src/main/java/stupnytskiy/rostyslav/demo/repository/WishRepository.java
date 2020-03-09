package stupnytskiy.rostyslav.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stupnytskiy.rostyslav.demo.entity.Wish;

@Repository
public interface WishRepository extends JpaRepository<Wish,Long> {
}
