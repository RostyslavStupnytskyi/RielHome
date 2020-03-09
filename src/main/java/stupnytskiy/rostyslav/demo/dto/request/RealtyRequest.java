package stupnytskiy.rostyslav.demo.dto.request;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class RealtyRequest {

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @PositiveOrZero
    private Integer price;

    private Boolean rent;

    private Integer stage;

    private Integer stagesCount;

    private Boolean basement;

    private Double area;

    private String description;

    private String mainImage;

    @Size(max = 10)
    private List<String> images;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long regionId;

    private Long homeTypeId;

    @NotNull
    private AddressRequest address;

}
