package gg.mylittleplanet.manager.web;

import gg.mylittleplanet.manager.apply.ApplyResult;
import gg.mylittleplanet.manager.apply.ApplyService;
import gg.mylittleplanet.manager.config.ManagerConfig;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ApplyController {

    private final ApplyService applyService;
    private final ManagerConfig config;

    @PostMapping("/apply")
    public @NotNull String apply(@NotNull Model model) {
        final ApplyResult result = applyService.apply();
        model.addAttribute("result", result);
        model.addAttribute("serverCount", config.getServers().size());
        model.addAttribute("servers", config.getServers());
        return "dashboard";
    }
}