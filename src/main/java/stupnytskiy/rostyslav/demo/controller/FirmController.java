package stupnytskiy.rostyslav.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import stupnytskiy.rostyslav.demo.dto.request.FirmRegistrationRequest;
import stupnytskiy.rostyslav.demo.dto.request.LoginRequest;
import stupnytskiy.rostyslav.demo.dto.request.RealtorRegistrationRequest;
import stupnytskiy.rostyslav.demo.dto.request.UserRegistrationRequest;
import stupnytskiy.rostyslav.demo.dto.response.AuthenticationResponse;
import stupnytskiy.rostyslav.demo.service.FirmService;
import stupnytskiy.rostyslav.demo.service.RealtorService;
import stupnytskiy.rostyslav.demo.service.UserService;

import javax.validation.Valid;
import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/firm")
public class FirmController {

    @Autowired
    private FirmService firmService;

    @Autowired
    private RealtorService realtorService;


    @PostMapping("/login")
    public AuthenticationResponse login(@Valid @RequestBody LoginRequest request) {
        return firmService.login(request);
    }

    @PostMapping("/register")
    public AuthenticationResponse register(@Valid @RequestBody FirmRegistrationRequest request) throws IOException {
        return firmService.register(request);
    }

    @PostMapping("/addRealtor")
    public AuthenticationResponse addRealtor(@Valid @RequestBody RealtorRegistrationRequest request) throws  IOException {
        return  realtorService.registerByFirm(request);
    }

    @GetMapping("/checkToken")
    public void checkToken() {
    }

}
