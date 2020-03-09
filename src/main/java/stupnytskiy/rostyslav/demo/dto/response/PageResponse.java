package stupnytskiy.rostyslav.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> data;
    private Long totalElements;
    private Integer totalPages;

}
