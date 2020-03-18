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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private UserRole userRole;

    private String phoneNumber;

    private String email;

    private String image;

    @OneToOne(cascade = CascadeType.ALL)
    private Firm firm;

    @OneToOne(cascade = CascadeType.ALL)
    private Realtor realtor;

    @OneToMany(mappedBy = "user")
    private List<Wish> wishes;

    @OneToMany(mappedBy = "user")
    private List<Order> orders;

}

