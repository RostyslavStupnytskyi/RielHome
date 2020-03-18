package stupnytskiy.rostyslav.demo.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Firm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   @OneToOne(mappedBy = "firm")
   private User user;

    @OneToMany(mappedBy = "firm")
    private Set<Realtor> realtors;

    @OneToMany(mappedBy = "firm")
    private List<Realty> realtyList;

    @OneToMany(mappedBy = "firm",cascade = CascadeType.ALL)
    private Set<Address> addresses;
}

