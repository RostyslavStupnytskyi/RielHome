package stupnytskiy.rostyslav.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

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

    @OneToMany(mappedBy = "region")
    private List<Realty> realtyList;

    @ManyToMany
    private List<Firm> firms;
}
