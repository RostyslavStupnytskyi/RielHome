package stupnytskiy.rostyslav.demo.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Setter
public class PaginationRequest {

    @Positive
    @NotNull
    private Integer size;
    @NotNull
    @PositiveOrZero
    private Integer page;

    private String field;

    private Sort.Direction direction;

    public Pageable mapToPageable() {
        if (field != null && direction != null) {
            return PageRequest.of(page, size, direction, field);
        } else if (field != null) {
            return PageRequest.of(page, size, Sort.Direction.ASC, field);
        } else {
            return PageRequest.of(page, size);
        }
    }
    }

