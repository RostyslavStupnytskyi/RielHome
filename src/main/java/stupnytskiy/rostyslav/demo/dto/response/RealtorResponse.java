package stupnytskiy.rostyslav.demo.dto.response;

import lombok.Getter;
import lombok.Setter;
import stupnytskiy.rostyslav.demo.entity.Realtor;

@Getter
@Setter
public class RealtorResponse {
    private UserResponse user;
    private PageResponse<RealtyResponse> realtyList;
    private String firmName;
    private Long firmId;
    private String region;

    public RealtorResponse (Realtor realtor){
        user = new UserResponse(realtor.getUser());
        if (realtor.getFirm()!= null) {
            firmId = realtor.getFirm().getId();
            firmName = realtor.getFirm().getUser().getUsername();
        }
        region  = realtor.getRegion().getName();
    }
}
