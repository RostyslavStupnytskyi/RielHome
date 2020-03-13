package stupnytskiy.rostyslav.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import stupnytskiy.rostyslav.demo.dto.request.PaginationRequest;
import stupnytskiy.rostyslav.demo.dto.request.RegionRequest;
import stupnytskiy.rostyslav.demo.dto.response.PageResponse;
import stupnytskiy.rostyslav.demo.dto.response.RegionResponse;
import stupnytskiy.rostyslav.demo.entity.Region;
import stupnytskiy.rostyslav.demo.repository.RegionRepository;

import java.util.stream.Collectors;

@Service
public class RegionService {

    @Autowired
    private RegionRepository regionRepository;

    public void save(RegionRequest request){
        regionRepository.save(regionRequestToRegion(request,null));
    }

    public void update(RegionRequest request, Long id){
        regionRepository.save(regionRequestToRegion(request,findById(id)));
    }

    public PageResponse<RegionResponse> findPage(PaginationRequest request){
        final Page<Region> page = regionRepository.findAll(request.mapToPageable());
        return new PageResponse<>(page.getContent().stream().map(RegionResponse::new).collect(Collectors.toList()),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
    
    public void delete(Long id){
        regionRepository.delete(findById(id));
    }

    public Region findById(Long id){
        return regionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Region type with id " + id + " does not exist"));
    }

    private Region regionRequestToRegion(RegionRequest request, Region region) {
        if (region == null) {
            region = new Region();
        }
        region.setName(request.getName());
        return region;
    }
}
