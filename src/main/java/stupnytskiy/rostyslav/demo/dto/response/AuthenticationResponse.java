package stupnytskiy.rostyslav.demo.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationResponse {
    private String username;
    private Long id;
    private String token;

    public AuthenticationResponse(String username, String token, Long id) {
        this.username = username;
        this.token = token;
        this.id = id;
    }
}
