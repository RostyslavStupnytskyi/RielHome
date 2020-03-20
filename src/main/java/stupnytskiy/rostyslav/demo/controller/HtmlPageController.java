package stupnytskiy.rostyslav.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@Controller
public class HtmlPageController {

    @RequestMapping("/admin/region")
    public String region(){
        return "region.html";
    }

    @RequestMapping("/admin/hometype")
    public String homeType(){
        return "hometype.html";
    }

    @RequestMapping("/admin/streettype")
    public String streetType(){
        return "streettype.html";
    }

    @RequestMapping("/registration-page")
    public String registrationPage(){
        return "user/registration.html";
    }

    @RequestMapping("/sign-in")
    public String loginPage(){
        return "user/login.html";
    }

    @RequestMapping("/firm-profile")
    public String firmProfile(){
        return "user/firm-profile.html";
    }
}
