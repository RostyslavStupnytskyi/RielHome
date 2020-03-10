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
import stupnytskiy.rostyslav.demo.dto.request.FirmRegistrationRequest;
import stupnytskiy.rostyslav.demo.dto.request.LoginRequest;
import stupnytskiy.rostyslav.demo.dto.response.AuthenticationResponse;
import stupnytskiy.rostyslav.demo.entity.Firm;
import stupnytskiy.rostyslav.demo.entity.UserRole;
import stupnytskiy.rostyslav.demo.repository.FirmRepository;
import stupnytskiy.rostyslav.demo.security.JwtTokenTool;
import stupnytskiy.rostyslav.demo.security.JwtUser;

import java.io.IOException;


@Service
public class FirmService implements UserDetailsService {

    @Autowired
    private FirmRepository firmRepository;


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenTool jwtTokenTool;


    @Autowired
    private BCryptPasswordEncoder encoder;



    public AuthenticationResponse register(FirmRegistrationRequest request) throws IOException {
        if (firmRepository.existsByLogin(request.getLogin())) {
            throw new BadCredentialsException("Firm with login " + request.getLogin() + " already exists");
        }
        Firm firm = new Firm();
        firm.setLogin(request.getLogin());
        firm.setUserRole(UserRole.ROLE_FIRM);
        firm.setPassword(encoder.encode(request.getPassword()));
        firmRepository.save(firm);
        return login(registrationToLogin(request));
    }

    public AuthenticationResponse login(LoginRequest request) {
        String login = request.getLogin();
        Firm firm = findByLogin(login);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, request.getPassword()));
        String token = jwtTokenTool.createToken(login, firm.getUserRole());
        String name = firm.getName();
        Long id = firm.getId();
        return new AuthenticationResponse(name,token,id);
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Firm firm = findByLogin(login);
        return new JwtUser(firm.getLogin(), firm.getUserRole(), firm.getPassword());
    }

    public Firm findByLogin(String username)  {
        return firmRepository.findByLogin(username).orElseThrow(() -> new UsernameNotFoundException("User with login " + username + " not exists"));
    }

    public Firm findById(Long id)  {
        return firmRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("Firm with id " + id + " not exists"));
    }

    private LoginRequest registrationToLogin(FirmRegistrationRequest registrationRequest){
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLogin(registrationRequest.getLogin());
        loginRequest.setPassword(registrationRequest.getPassword());
        return loginRequest;
    }

}
