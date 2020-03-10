package stupnytskiy.rostyslav.demo.dto.response;

import lombok.Getter;
import lombok.Setter;
import stupnytskiy.rostyslav.demo.entity.Region;

@Getter
@Setter
public class RegionResponse {
    private Long id;
    private String name;

    public RegionResponse (Region region){
        id = region.getId();
        name = region.getName();
    }
}
