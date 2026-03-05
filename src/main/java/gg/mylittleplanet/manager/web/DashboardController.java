package gg.mylittleplanet.manager.web;

import gg.mylittleplanet.manager.config.ManagerConfig;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ManagerConfig config;

    @GetMapping("/")
    public @NotNull String dashboard(@NotNull Model model) {
        model.addAttribute("serverCount", config.getServers().size());
        model.addAttribute("servers", config.getServers());
        model.addAttribute("result", null);
        return "dashboard";
    }
}