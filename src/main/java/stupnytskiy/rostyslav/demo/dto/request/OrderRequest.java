package stupnytskiy.rostyslav.demo.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
public class OrderRequest {

    @NotNull
    @Positive
    private Long realtyId;

    @NotBlank
    private String message;
}
