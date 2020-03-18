package stupnytskiy.rostyslav.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import stupnytskiy.rostyslav.demo.dto.request.RealtyRequest;
import stupnytskiy.rostyslav.demo.dto.response.RealtyResponse;
//import stupnytskiy.rostyslav.demo.service.RealtorService;
import stupnytskiy.rostyslav.demo.service.RealtyService;

import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/realty")
public class RealtyController {

    @Autowired
    private RealtyService realtyService;

    @PostMapping
    public void save (@RequestBody RealtyRequest request) throws IOException {
        realtyService.save(request);
    }

    @PutMapping
    public void update(@RequestBody RealtyRequest request, Long id) throws IOException {
        realtyService.update(request,id);
    }
}
