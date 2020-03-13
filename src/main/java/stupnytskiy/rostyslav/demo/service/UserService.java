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
import stupnytskiy.rostyslav.demo.dto.request.LoginRequest;
import stupnytskiy.rostyslav.demo.dto.request.UserRegistrationRequest;
import stupnytskiy.rostyslav.demo.dto.response.AuthenticationResponse;
import stupnytskiy.rostyslav.demo.entity.User;
import stupnytskiy.rostyslav.demo.entity.UserRole;
import stupnytskiy.rostyslav.demo.repository.UserRepository;
import stupnytskiy.rostyslav.demo.security.JwtTokenTool;
import stupnytskiy.rostyslav.demo.security.JwtUser;
import stupnytskiy.rostyslav.demo.tools.FileTool;

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

    @Autowired
    private FileTool fileTool;



    public AuthenticationResponse register(UserRegistrationRequest request) throws IOException {
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new BadCredentialsException("User with username " + request.getLogin() + " already exists");
        }
        User user = registrationRequestToUser(request);
        user.setPassword(encoder.encode(request.getPassword()));
        userRepository.save(user);
        return login(registrationToLogin(request));
    }

    public AuthenticationResponse login(LoginRequest request) {
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

    private User registrationRequestToUser(UserRegistrationRequest request) throws IOException {
        User user = new User();
        String userDir = "user_" + request.getLogin();
        user.setLogin(request.getLogin());
        user.setUsername(request.getUsername());
        user.setUserRole(UserRole.ROLE_USER);
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        if(request.getImage() != null)
        user.setImage(fileTool.saveUserAvatar(request.getImage(), userDir));
        return user;
    }

    private LoginRequest registrationToLogin(UserRegistrationRequest registrationRequest){
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLogin(registrationRequest.getLogin());
        loginRequest.setPassword(registrationRequest.getPassword());
        return loginRequest;
    }

}
