package stupnytskiy.rostyslav.demo.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class LoginRequest {

    @NotBlank
    private String login;

    @Size(min = 3, max = 30)
    private String password;

//    @NotEmpty//@Size(min = 1)
//    private List<Long> favoritesIds;
}
