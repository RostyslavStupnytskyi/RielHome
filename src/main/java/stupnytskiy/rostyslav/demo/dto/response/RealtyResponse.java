package stupnytskiy.rostyslav.demo.dto.response;

import lombok.Getter;
import lombok.Setter;
import stupnytskiy.rostyslav.demo.entity.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class RealtyResponse {
    private Long id;
    private String name;
    private Integer price;
    private Boolean rent;
    private Integer stage;
    private Integer stagesCount;
    private Boolean basement;
    private Double area;
    private String description;
    private String mainImage;
    private List<String> images;
    private LocalDate startDate;
    private LocalDate endDate;
    private String firmName;
    private String homeType;
    private String regionName;
    private AddressResponse address;

    public RealtyResponse(Realty realty){
        id = realty.getId();
        name = realty.getName();
        price = realty.getPrice();
        rent = realty.getRent();
        stage = realty.getStage();
        stagesCount = realty.getStagesCount();
        basement = realty.getBasement();
        area = realty.getArea();
        description  = realty.getDescription();
        mainImage = realty.getMainImage();
        images = realty.getImages();
        startDate = realty.getStartDate();
        endDate = realty.getEndDate();
        firmName = realty.getFirm().getName();
        homeType = realty.getHomeType().getName();
        regionName = realty.getRegion().getName();
        address = new AddressResponse(realty.getAddress());
    }
}
