package kr.sofaware.slas.home;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    @GetMapping("/")
    public String defaultUrl() {
        return "home";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }
}
