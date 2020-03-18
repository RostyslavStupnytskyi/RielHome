package stupnytskiy.rostyslav.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import stupnytskiy.rostyslav.demo.dto.request.AddressRequest;
import stupnytskiy.rostyslav.demo.dto.request.FirmRegistrationRequest;
import stupnytskiy.rostyslav.demo.dto.request.LoginRequest;
import stupnytskiy.rostyslav.demo.dto.response.AuthenticationResponse;
import stupnytskiy.rostyslav.demo.entity.Address;
import stupnytskiy.rostyslav.demo.entity.Firm;
import stupnytskiy.rostyslav.demo.entity.User;
import stupnytskiy.rostyslav.demo.entity.UserRole;
import stupnytskiy.rostyslav.demo.repository.FirmRepository;
import stupnytskiy.rostyslav.demo.security.JwtTokenTool;
import stupnytskiy.rostyslav.demo.security.JwtUser;
import stupnytskiy.rostyslav.demo.tools.FileTool;

import java.io.IOException;


@Service
public class FirmService{

    @Autowired
    private FirmRepository firmRepository;

//    @Autowired
//    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenTool jwtTokenTool;

    @Autowired
    private AddressService addressService;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private FileTool fileTool;

    @Autowired
    private UserService userService;


//    public AuthenticationResponse register(FirmRegistrationRequest request) throws IOException {
//        if (firmRepository.existsByLogin(request.getLogin())) {
//            throw new BadCredentialsException("Firm with login " + request.getLogin() + " already exists");
//        }
//        Firm firm = registrationRequestToFirm(request);
//        firm.setPassword(encoder.encode(request.getPassword()));
//        firmRepository.save(firm);
//        return login(registrationToLogin(request));
//    }
//
//    public AuthenticationResponse login(LoginRequest request) {
//        String login = request.getLogin();
//        Firm firm = findByLogin(login);
//        userService.authenticate(request);
////        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, request.getPassword()));
//        String token = jwtTokenTool.createToken(login, firm.getUserRole());
//        String name = firm.getName();
//        Long id = firm.getId();
//        return new AuthenticationResponse(name,token,id);
//    }

//    @Override
//    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
//        User user = userService.findByLogin(login);
//        return new JwtUser(user.getLogin(), user.getUserRole(), user.getPassword());
//    }

//    public Firm findByLogin(String username)  {
//        return firmRepository.findByLogin(username).orElseThrow(() -> new UsernameNotFoundException("User with login " + username + " not exists"));
//    }

    public Firm findById(Long id)  {
        return firmRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("Firm with id " + id + " not exists"));
    }

//    private Firm registrationRequestToFirm(FirmRegistrationRequest request) throws IOException {
//        Firm firm = new Firm();
//        String userDir = "user_" + request.getLogin();
//        firm.setLogin(request.getLogin());
//        firm.setUserRole(UserRole.ROLE_FIRM);
//        firm.setEmail(request.getEmail());
//        firm.setName(request.getName());
//        firm.setPhoneNumber(request.getPhoneNumber());
//        if (request.getImage() != null ) firm.setImage(fileTool.saveUserAvatar(request.getImage(), userDir));
//        request.getAddresses().forEach(a -> firm.getAddresses().add(addressService.addressRequestToAddress(a,null)));
//        return firm;
//    }
//
//    private LoginRequest registrationToLogin(FirmRegistrationRequest registrationRequest){
//        LoginRequest loginRequest = new LoginRequest();
//        loginRequest.setLogin(registrationRequest.getLogin());
//        loginRequest.setPassword(registrationRequest.getPassword());
//        return loginRequest;
//    }

}
