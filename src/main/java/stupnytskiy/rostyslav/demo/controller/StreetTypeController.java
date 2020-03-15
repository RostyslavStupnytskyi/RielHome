package stupnytskiy.rostyslav.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import stupnytskiy.rostyslav.demo.dto.request.HomeTypeRequest;
import stupnytskiy.rostyslav.demo.dto.request.PaginationRequest;
import stupnytskiy.rostyslav.demo.dto.request.StreetTypeRequest;
import stupnytskiy.rostyslav.demo.dto.response.PageResponse;
import stupnytskiy.rostyslav.demo.dto.response.StreetTypeResponse;
import stupnytskiy.rostyslav.demo.service.StreetTypeService;

import javax.validation.Valid;

@RestController
@RequestMapping("/streettype")
public class StreetTypeController {

    @Autowired
    private StreetTypeService streetTypeService;

    @GetMapping
    public PageResponse<StreetTypeResponse> findPage(PaginationRequest request){
        return streetTypeService.findPage(request);
    }

    @PutMapping
    public void create(@Valid @RequestBody StreetTypeRequest request){
        streetTypeService.save(request);
    }

    @PostMapping
    public void update(@Valid @RequestBody StreetTypeRequest request, Long id){
        streetTypeService.update(request , id);
    }

    @DeleteMapping
    public void delete(Long id){
        streetTypeService.delete(id);
    }
}
