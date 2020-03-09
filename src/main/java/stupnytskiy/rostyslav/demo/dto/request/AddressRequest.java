package stupnytskiy.rostyslav.demo.dto.request;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Setter
public class AddressRequest {

    @NotNull
    @NotBlank
    private String settlement;

    @NotNull
    @NotBlank
    private String streetName;

    @NotNull
    @NotBlank
    private String streetNumber;

    @NotNull
    @PositiveOrZero
    private Long streetTypeId;

    @NotNull
    @PositiveOrZero
    private Long regionId;

}
