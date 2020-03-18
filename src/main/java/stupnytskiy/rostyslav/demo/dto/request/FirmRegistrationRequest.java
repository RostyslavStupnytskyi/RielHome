package stupnytskiy.rostyslav.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class FirmRegistrationRequest {

    @JsonProperty("user")
    private UserRegistrationRequest userRegistrationRequest;

    @Size(min = 1)
    private List<AddressRequest> addresses;
}
