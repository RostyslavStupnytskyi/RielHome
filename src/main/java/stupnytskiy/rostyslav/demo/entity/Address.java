package stupnytskiy.rostyslav.demo.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
