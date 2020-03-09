package stupnytskiy.rostyslav.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
public class Wish {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Boolean rent;

  private Integer stage;

  private Integer stagesCount;

  private LocalDate startDate;

  private LocalDate endDate;

  private Integer maxPrice;

  private Integer minPrice;

  private Boolean basement;

  @ManyToOne
  private HomeType homeType;

  @ManyToOne
  private Region region;

  private String description;

  private Double minArea;

  private Double maxArea;

  @ManyToOne
  private User user;

  @OneToMany(mappedBy = "wish")
  private Set<Answer> answers;
}
