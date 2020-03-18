package stupnytskiy.rostyslav.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stupnytskiy.rostyslav.demo.dto.request.PaginationRequest;
import stupnytskiy.rostyslav.demo.dto.request.WishRequest;
import stupnytskiy.rostyslav.demo.dto.response.PageResponse;
import stupnytskiy.rostyslav.demo.dto.response.WishResponse;
import stupnytskiy.rostyslav.demo.entity.*;
import stupnytskiy.rostyslav.demo.repository.WishRepository;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WishService {

    @Autowired
    private WishRepository wishRepository;

    @Autowired
    private HomeTypeService homeTypeService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private FirmService firmService;

    @Autowired
    private RealtorService realtorService;
    @Autowired
    private StreetTypeService streetTypeService;

    @Autowired
    private UserService userService;

    public void save(WishRequest request){
        wishRepository.save(wishRequestToWish(request));
    }

    public PageResponse<WishResponse> findByRegions(Set<Region> regions, PaginationRequest request) {
        final Page<Wish> page = wishRepository.findAll(request.mapToPageable());
        return new PageResponse<>(page.getContent()
                .stream()
                .filter(w -> regions.contains(w.getRegion()))
                .map(WishResponse::new)
                .collect(Collectors.toList()),
                page.getTotalElements(),
                page.getTotalPages());

    }

//    public PageResponse<WishResponse> findByFirmRegions(PaginationRequest request){
//        final Firm firm = firmService.findByLogin((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
//        final Set<Region> regions = firm.getAddresses().stream().map(Address::getRegion).collect(Collectors.toSet());
//        return findByRegions(regions, request);
//    }

//    public PageResponse<WishResponse> findByRealtorRegion(PaginationRequest request){
//        final Realtor realtor = realtorService.findByLogin((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
//        return findByRegions(Collections.singleton(realtor.getRegion()), request);
//    }

    public Set<WishResponse> findAll(){
        return wishRepository.findAll()
                .stream()
                .map(WishResponse::new)
                .collect(Collectors.toSet());
    }

    public Wish findById(Long id){
        return wishRepository.findById(id).orElseThrow(IllegalArgumentException::new);
    }

    private Wish wishRequestToWish(WishRequest request){
        Wish wish = new Wish();
        User user = userService.findByLogin((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        wish.setBasement(request.getBasement());
        wish.setRent(request.getRent());
        wish.setStartDate(request.getStartDate());
        wish.setEndDate(request.getEndDate());
        wish.setHomeType(homeTypeService.findById(request.getHomeTypeId()));
        wish.setRegion(regionService.findById(request.getRegionId()));
        wish.setMaxArea(request.getMaxArea());
        wish.setMinArea(request.getMinArea());
        wish.setMaxPrice(request.getMaxPrice());
        wish.setMinPrice(request.getMinPrice());
        wish.setDescription(request.getDescription());
        wish.setStage(request.getStage());
        wish.setStagesCount(request.getStagesCount());
        wish.setUser(user);
        return wish;
    }
}
