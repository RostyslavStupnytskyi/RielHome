package stupnytskiy.rostyslav.demo.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class HomeType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @OneToMany(mappedBy = "homeType")
  private List<Realty> realtyList;

  @OneToMany(mappedBy = "homeType")
  private List<Wish> wishes;
}
