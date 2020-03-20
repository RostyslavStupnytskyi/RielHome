package stupnytskiy.rostyslav.demo.dto.response;

import lombok.Getter;
import lombok.Setter;
import stupnytskiy.rostyslav.demo.entity.User;

@Getter
@Setter
public class FirmProfileResponse {
    private UserResponse user;
    private Long realtors;
    private Long realties;
    private Long firmId;

    public FirmProfileResponse(User user){
        this.user = new UserResponse(user);
        firmId = user.getFirm().getId();
        realtors = 0L;
        realties = 0L;
        user.getFirm().getRealtors().forEach(r -> realtors++);
        user.getFirm().getRealtyList().forEach(r->realties++);
    }
}
