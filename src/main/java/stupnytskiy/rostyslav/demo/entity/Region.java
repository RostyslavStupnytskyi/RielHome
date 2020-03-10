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
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "region")
    private List<Address> addresses;

    @OneToMany(mappedBy = "region")
    private List<Wish> wishes;

    @OneToMany(mappedBy = "region")
    private List<Realtor> realtors;

    @ManyToMany
    private List<Firm> firms;
}
