package stupnytskiy.rostyslav.demo.dto.response;

import lombok.Getter;
import lombok.Setter;
import stupnytskiy.rostyslav.demo.entity.HomeType;

@Getter
@Setter
public class HomeTypeResponse {

    private Long id;
    private String name;

    public HomeTypeResponse(HomeType homeType) {
        id = homeType.getId();
        name = homeType.getName();
    }
}
