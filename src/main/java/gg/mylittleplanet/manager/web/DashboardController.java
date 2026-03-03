package gg.mylittleplanet.manager.web;

import gg.mylittleplanet.manager.config.ManagerConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ManagerConfig config;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("serverCount", config.getServers().size());
        model.addAttribute("result", null);
        return "dashboard";
    }
}