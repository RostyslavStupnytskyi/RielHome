package stupnytskiy.rostyslav.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import stupnytskiy.rostyslav.demo.dto.request.HomeTypeRequest;
import stupnytskiy.rostyslav.demo.dto.request.PaginationRequest;
import stupnytskiy.rostyslav.demo.dto.request.RegionRequest;
import stupnytskiy.rostyslav.demo.dto.response.HomeTypeResponse;
import stupnytskiy.rostyslav.demo.dto.response.PageResponse;
import stupnytskiy.rostyslav.demo.dto.response.RegionResponse;
import stupnytskiy.rostyslav.demo.service.HomeTypeService;

import javax.validation.Valid;

@RestController
@RequestMapping("/hometype")
public class HomeTypeController {

    @Autowired
    private HomeTypeService homeTypeService;

    @GetMapping
    public PageResponse<HomeTypeResponse> findPage(PaginationRequest request){
        return homeTypeService.findPage(request);
    }

    @PutMapping
    public void create(@Valid @RequestBody HomeTypeRequest request){
        homeTypeService.save(request);
    }

    @PostMapping
    public void update(@Valid @RequestBody HomeTypeRequest request, Long id){
        homeTypeService.update(request , id);
    }

    @DeleteMapping
    public void delete(Long id){
        homeTypeService.delete(id);
    }
}
