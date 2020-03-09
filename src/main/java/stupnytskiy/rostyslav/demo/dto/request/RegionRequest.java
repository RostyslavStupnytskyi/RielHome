package stupnytskiy.rostyslav.demo.dto.request;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class RegionRequest {

    @NotNull
    @NotBlank
    private String name;

}

