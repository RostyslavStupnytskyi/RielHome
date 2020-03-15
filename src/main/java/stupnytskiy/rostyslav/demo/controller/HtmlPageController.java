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
}
