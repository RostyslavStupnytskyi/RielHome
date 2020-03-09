package stupnytskiy.rostyslav.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Region region;

    @Column(nullable = false)
    private String settlement;

    @ManyToOne
    private StreetType streetType;

    @Column(nullable = false)
    private String streetName;

    private String streetNumber;

    @OneToOne
    private Realty realty;

}
