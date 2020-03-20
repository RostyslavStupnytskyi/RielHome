package stupnytskiy.rostyslav.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import stupnytskiy.rostyslav.demo.dto.request.LoginRequest;
import stupnytskiy.rostyslav.demo.dto.request.PaginationRequest;
import stupnytskiy.rostyslav.demo.dto.request.RealtorRegistrationRequest;
import stupnytskiy.rostyslav.demo.dto.response.AuthenticationResponse;
import stupnytskiy.rostyslav.demo.dto.response.PageResponse;
import stupnytskiy.rostyslav.demo.dto.response.RealtorResponse;
import stupnytskiy.rostyslav.demo.service.RealtorService;
import stupnytskiy.rostyslav.demo.service.UserService;

import javax.validation.Valid;
import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/realtor")
public class RealtorController {

    @Autowired
    private UserService userService;

    @Autowired
    private RealtorService realtorService;

    @PostMapping("/register")
    public AuthenticationResponse register(@Valid @RequestBody RealtorRegistrationRequest request) throws IOException {
        return userService.registerRealtor(request);
    }

    @GetMapping("/firm")
    public PageResponse<RealtorResponse> findByFirm(PaginationRequest request){
        return realtorService.findByFirm(request);
    }

    @GetMapping("/checkToken")
    public void checkToken() {
    }

}
