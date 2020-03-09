package stupnytskiy.rostyslav.demo.dto.response;

import lombok.Getter;
import lombok.Setter;
import stupnytskiy.rostyslav.demo.entity.StreetType;

@Getter
@Setter
public class StreetTypeResponse {

    private Long id;
    private String name;

    public StreetTypeResponse(StreetType streetType){
        id = streetType.getId();
        name = streetType.getName();
    }
}
