package stupnytskiy.rostyslav.demo.dto.request;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Getter
@Setter
public class AnswerRequest {

    @NotNull
    @Positive
    private Long wishId;

    @NotNull
    @Positive
    private Long realtyId;

    @NotBlank
    private String message;

    private Long realtorId;
}
