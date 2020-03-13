package stupnytskiy.rostyslav.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import stupnytskiy.rostyslav.demo.dto.request.FirmRegistrationRequest;
import stupnytskiy.rostyslav.demo.dto.request.LoginRequest;
import stupnytskiy.rostyslav.demo.dto.request.RealtorRegistrationRequest;
import stupnytskiy.rostyslav.demo.dto.response.AuthenticationResponse;
import stupnytskiy.rostyslav.demo.entity.Firm;
import stupnytskiy.rostyslav.demo.entity.Realtor;
import stupnytskiy.rostyslav.demo.entity.UserRole;
import stupnytskiy.rostyslav.demo.repository.RealtorRepository;
import stupnytskiy.rostyslav.demo.security.JwtTokenTool;
import stupnytskiy.rostyslav.demo.security.JwtUser;
import stupnytskiy.rostyslav.demo.tools.FileTool;

import java.io.IOException;


@Service
public class RealtorService implements UserDetailsService {

    @Autowired
    private RealtorRepository realtorRepository;

    @Autowired
    private FirmService firmService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenTool jwtTokenTool;

    @Autowired
    private FileTool fileTool;

    @Autowired
    private BCryptPasswordEncoder encoder;



    public AuthenticationResponse registerByFirm(RealtorRegistrationRequest request) throws IOException {
        if (realtorRepository.existsByLogin(request.getLogin())) {
            throw new BadCredentialsException("Realtor with login " + request.getLogin() + " already exists");
        }
        Realtor realtor = realtorRegistrationRequestToRealtor(request);
        realtor.setFirm(firmService.findByLogin((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
        realtorRepository.save(realtor);
        return login(registrationToLogin(request));
    }

    public AuthenticationResponse registerSimple(RealtorRegistrationRequest request) throws IOException {
        if (realtorRepository.existsByLogin(request.getLogin())) {
            throw new BadCredentialsException("Realtor with login " + request.getLogin() + " already exists");
        }
        Realtor realtor = realtorRegistrationRequestToRealtor(request);
        realtorRepository.save(realtor);
        return login(registrationToLogin(request));
    }

    public AuthenticationResponse login(LoginRequest request) {
        String login = request.getLogin();
        Realtor realtor = findByLogin(login);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, request.getPassword()));
        String token = jwtTokenTool.createToken(login, realtor.getUserRole());
        String name = realtor.getName();
        Long id = realtor.getId();
        return new AuthenticationResponse(name,token,id);
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Realtor realtor = findByLogin(login);
        return new JwtUser(realtor.getLogin(), realtor.getUserRole(), realtor.getPassword());
    }

    public Realtor findByLogin(String username)  {
        return realtorRepository.findByLogin(username).orElseThrow(() -> new UsernameNotFoundException("Realtor with login " + username + " not exists"));
    }

    public Realtor findById(Long id)  {
        return realtorRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("Realtor with id " + id + " not exists"));
    }

    private LoginRequest registrationToLogin(RealtorRegistrationRequest registrationRequest){
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLogin(registrationRequest.getLogin());
        loginRequest.setPassword(registrationRequest.getPassword());
        return loginRequest;
    }

    private Realtor realtorRegistrationRequestToRealtor(RealtorRegistrationRequest request) throws IOException {
        Realtor realtor = new Realtor();
        String userDir = "user_" + request.getLogin();
        realtor.setName(request.getName());
        realtor.setLogin(request.getLogin());
        realtor.setUserRole(UserRole.ROLE_REALTOR);
        realtor.setRegion(regionService.findById(request.getRegionId()));
        realtor.setPhoneNumber(request.getPhoneNumber());
        realtor.setPassword(encoder.encode(request.getPassword()));
        if (request.getImage() != null) realtor.setImage(fileTool.saveUserAvatar(request.getImage(), userDir));
        return realtor;
    }
}
