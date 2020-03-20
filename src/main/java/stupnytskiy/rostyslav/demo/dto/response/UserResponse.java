package stupnytskiy.rostyslav.demo.dto.response;

import lombok.Getter;
import lombok.Setter;
import stupnytskiy.rostyslav.demo.entity.User;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String image;

    public UserResponse(User user){
        id = user.getId();
        name = user.getUsername();
        email = user.getEmail();
        phone = user.getPhoneNumber();
        image = user.getImage();
    }
}
