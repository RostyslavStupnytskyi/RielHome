package stupnytskiy.rostyslav.demo.dto.request;

import lombok.Getter;
import lombok.Setter;
import stupnytskiy.rostyslav.demo.entity.HomeType;
import stupnytskiy.rostyslav.demo.entity.Region;

import javax.persistence.ManyToOne;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@Getter
@Setter
public class WishRequest {

    private Boolean rent;

    private Integer stage;

    private Integer stagesCount;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer maxPrice;

    private Integer minPrice;

    private Boolean basement;

    @Positive
    private Long homeTypeId;

    @Positive
    private Long regionId;

    private String description;

    @Positive
    private Double minArea;

    @Positive
    private Double maxArea;
}
