package stupnytskiy.rostyslav.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import stupnytskiy.rostyslav.demo.dto.request.AddressRequest;
import stupnytskiy.rostyslav.demo.dto.request.PaginationRequest;
import stupnytskiy.rostyslav.demo.dto.response.AddressResponse;
import stupnytskiy.rostyslav.demo.dto.response.PageResponse;
import stupnytskiy.rostyslav.demo.service.AddressService;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public PageResponse<AddressResponse> getFirmAddresses(Long id, PaginationRequest request){
        return addressService.finsAllByFirmId(id,request);
    }

    @GetMapping("/all")
    public List<AddressResponse> getAddresses(Long id){
        return addressService.findAll(id);
    }

    @GetMapping("/one")
    public AddressResponse getOne(Long id){
        return new AddressResponse(addressService.findById(id));
    }

    @PostMapping
    public void save(@RequestBody AddressRequest request){
        addressService.save(request);
    }

    @PutMapping
    public void update(Long id,@RequestBody AddressRequest request){
        addressService.update(id,request);
    }

    @DeleteMapping
    public void delete(Long id){
        addressService.delete(id);
    }
}
