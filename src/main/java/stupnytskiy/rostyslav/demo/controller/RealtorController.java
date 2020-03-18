package stupnytskiy.rostyslav.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import stupnytskiy.rostyslav.demo.dto.request.LoginRequest;
import stupnytskiy.rostyslav.demo.dto.request.RealtorRegistrationRequest;
import stupnytskiy.rostyslav.demo.dto.response.AuthenticationResponse;
import stupnytskiy.rostyslav.demo.service.UserService;

import javax.validation.Valid;
import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/realtor")
public class RealtorController {

    @Autowired
    private UserService userService;


    @PostMapping("/login")
    public AuthenticationResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/register")
    public AuthenticationResponse register(@Valid @RequestBody RealtorRegistrationRequest request) throws IOException {
        return userService.registerRealtor(request);
    }

    @GetMapping("/checkToken")
    public void checkToken() {
    }

}
