package stupnytskiy.rostyslav.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import stupnytskiy.rostyslav.demo.entity.Wish;

import javax.validation.constraints.Positive;
import java.time.LocalDate;

@Getter
@Setter
public class WishResponse {

    private Boolean rent;
    private Integer stage;
    private Integer stagesCount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate endDate;
    private Integer maxPrice;
    private Integer minPrice;
    private Boolean basement;
    private String homeType;
    private String region;
    private String description;
    private Double minArea;
    private Double maxArea;

    public WishResponse (Wish wish){
        rent = wish.getRent();
        stage = wish.getStage();
        stagesCount = wish.getStagesCount();
        startDate = wish.getStartDate();
        endDate = wish.getEndDate();
        maxPrice = wish.getMaxPrice();
        minPrice = wish.getMinPrice();
        basement = wish.getBasement();
        homeType = wish.getHomeType().getName();
        region = wish.getRegion().getName();
        description = wish.getDescription();
        minArea = wish.getMinArea();
        maxArea = wish.getMinArea();
    }
}
