package stupnytskiy.rostyslav.demo.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
public class Realty {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Boolean rent;

    private Integer stage;

    private Integer stagesCount;

    @Column(nullable = false)
    private Boolean basement;

    private Double area;

    private String description;

    private String mainImage;

    @ElementCollection
    private List<String> images;

    private LocalDate startDate;

    private LocalDate endDate;

    @ManyToOne
    private Firm firm;

    @ManyToOne
    private Realtor realtor;

    @ManyToOne
    private HomeType homeType;

    @ManyToOne
    private Region region;

    @OneToOne(cascade = CascadeType.ALL)
    private Address address;

    @OneToMany(mappedBy = "realty")
    private Set<Answer> answers;

    @OneToMany(mappedBy = "realty")
    private Set<Order> orders;

}
