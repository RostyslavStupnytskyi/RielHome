package stupnytskiy.rostyslav.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import stupnytskiy.rostyslav.demo.dto.request.PaginationRequest;
import stupnytskiy.rostyslav.demo.dto.request.RegionRequest;
import stupnytskiy.rostyslav.demo.dto.response.PageResponse;
import stupnytskiy.rostyslav.demo.dto.response.RegionResponse;
import stupnytskiy.rostyslav.demo.service.RegionService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/region")
public class RegionController {

    @Autowired
    private RegionService regionService;

    @GetMapping
    public PageResponse<RegionResponse> findPage(PaginationRequest request){
        return regionService.findPage(request);
    }

    @PutMapping
    public void create(@Valid @RequestBody RegionRequest request){
        regionService.save(request);
    }

    @PostMapping
    public void update(@Valid @RequestBody RegionRequest regionRequest, Long id){
        regionService.update(regionRequest, id);
    }

    @DeleteMapping
    public void delete(Long id){
        regionService.delete(id);
    }
}
