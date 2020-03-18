package stupnytskiy.rostyslav.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
public class RealtorRegistrationRequest {

    @JsonProperty("user")
    private UserRegistrationRequest userRegistrationRequest;

    private Long regionId;
}
