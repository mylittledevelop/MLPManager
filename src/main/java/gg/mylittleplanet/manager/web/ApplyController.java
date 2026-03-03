package gg.mylittleplanet.manager.web;

import gg.mylittleplanet.manager.apply.ApplyResult;
import gg.mylittleplanet.manager.apply.ApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ApplyController {

    private final ApplyService applyService;

    @PostMapping("/apply")
    public String apply(Model model) {
        final ApplyResult result = applyService.apply();
        model.addAttribute("result", result);
        model.addAttribute("serverCount", 0);
        return "dashboard";
    }
}