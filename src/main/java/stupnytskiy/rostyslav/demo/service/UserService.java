package stupnytskiy.rostyslav.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import stupnytskiy.rostyslav.demo.dto.request.UserLoginRequest;
import stupnytskiy.rostyslav.demo.dto.request.UserRegistrationRequest;
import stupnytskiy.rostyslav.demo.dto.response.AuthenticationResponse;
import stupnytskiy.rostyslav.demo.entity.User;
import stupnytskiy.rostyslav.demo.entity.UserRole;
import stupnytskiy.rostyslav.demo.repository.UserRepository;
import stupnytskiy.rostyslav.demo.security.JwtTokenTool;
import stupnytskiy.rostyslav.demo.security.JwtUser;

import java.io.IOException;


@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenTool jwtTokenTool;


    @Autowired
    private BCryptPasswordEncoder encoder;



    public AuthenticationResponse register(UserRegistrationRequest request) throws IOException {
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new BadCredentialsException("User with username " + request.getLogin() + " already exists");
        }
        User user = new User();
        user.setLogin(request.getLogin());
        user.setUserRole(UserRole.ROLE_USER);
        user.setPassword(encoder.encode(request.getPassword()));
        user.setUsername(request.getUsername());
        userRepository.save(user);
        return login(registrationToLogin(request));
    }

    public AuthenticationResponse login(UserLoginRequest request) {
        String login = request.getLogin();
        User user = findByLogin(login);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, request.getPassword()));
        String token = jwtTokenTool.createToken(login, user.getUserRole());
        String name = user.getUsername();
        Long id = user.getId();
        return new AuthenticationResponse(name,token,id);
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = findByLogin(login);
        return new JwtUser(user.getLogin(), user.getUserRole(), user.getPassword());
    }

    public User findByLogin(String username)  {
        return userRepository.findByLogin(username).orElseThrow(() -> new UsernameNotFoundException("User with login " + username + " not exists"));
    }

    public User findById(Long id)  {
        return userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User with id " + id + " not exists"));
    }

    private UserLoginRequest registrationToLogin(UserRegistrationRequest registrationRequest){
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setLogin(registrationRequest.getLogin());
        loginRequest.setPassword(registrationRequest.getPassword());
        return loginRequest;
    }

}
