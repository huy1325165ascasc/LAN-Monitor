package com.vku.lanmonitor.server.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

    @GetMapping
    public String adminPanel() {
        return "redirect:/admin/monitor";
    }

    @GetMapping("/monitor")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/quizzes")
    public String quizManager() {
        return "quiz";
    }
}
