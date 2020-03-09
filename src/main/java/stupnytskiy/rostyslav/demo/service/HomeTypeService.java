package stupnytskiy.rostyslav.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import stupnytskiy.rostyslav.demo.dto.request.HomeTypeRequest;
import stupnytskiy.rostyslav.demo.dto.request.PaginationRequest;
import stupnytskiy.rostyslav.demo.dto.response.HomeTypeResponse;
import stupnytskiy.rostyslav.demo.dto.response.PageResponse;
import stupnytskiy.rostyslav.demo.entity.HomeType;
import stupnytskiy.rostyslav.demo.repository.HomeTypeRepository;

import java.util.stream.Collectors;

@Service
public class HomeTypeService {

    @Autowired
    private HomeTypeRepository homeTypeRepository;

    public void save(HomeTypeRequest request){
        homeTypeRepository.save(homeTypeRequestToHomeType(request,null));
    }

    public void update(HomeTypeRequest request, Long id){
        homeTypeRepository.save(homeTypeRequestToHomeType(request,findById(id)));
    }

    public PageResponse<HomeTypeResponse> findPage(PaginationRequest request){
        final Page<HomeType> page = homeTypeRepository.findAll(request.mapToPageable());
        return new PageResponse<>(page.getContent().stream().map(HomeTypeResponse::new).collect(Collectors.toList()),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public void delete(Long id){
        homeTypeRepository.delete(findById(id));
    }

    public HomeType findById(Long id){
        return homeTypeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("HomeType with id " + id + " does not exist"));
    }

    private HomeType homeTypeRequestToHomeType(HomeTypeRequest request, HomeType homeType) {
        if (homeType == null) {
            homeType = new HomeType();
        }
        homeType.setName(request.getName());
        return homeType;
    }
}
