package stupnytskiy.rostyslav.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import stupnytskiy.rostyslav.demo.dto.request.LoginRequest;
import stupnytskiy.rostyslav.demo.dto.request.RealtorRegistrationRequest;
import stupnytskiy.rostyslav.demo.dto.request.UserRegistrationRequest;
import stupnytskiy.rostyslav.demo.dto.response.AuthenticationResponse;
import stupnytskiy.rostyslav.demo.service.RealtorService;

import javax.validation.Valid;
import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/realtor")
public class RealtorController {

    @Autowired
    private RealtorService realtorService;


    @PostMapping("/login")
    public AuthenticationResponse login(@Valid @RequestBody LoginRequest request) {
        return realtorService.login(request);
    }

    @PostMapping("/register")
    public AuthenticationResponse register(@Valid @RequestBody RealtorRegistrationRequest request) throws IOException {
        return realtorService.registerSimple(request);
    }

    @GetMapping("/checkToken")
    public void checkToken() {
    }

}
