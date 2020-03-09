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
public class Realtor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private UserRole userRole;

    private String image;

    @ManyToOne
    private Firm firm;

    @ManyToOne
    private Region region;

    @OneToMany(mappedBy = "realtor")
    private List<Realty> realtyList;

    @OneToMany(mappedBy = "realtor")
    private List<Answer> answers;
}

