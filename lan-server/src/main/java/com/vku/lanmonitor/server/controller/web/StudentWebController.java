package com.vku.lanmonitor.server.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentWebController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String studentWaitingRoom() {
        return "student_home";
    }

    @GetMapping("/take_quiz")
    public String takeQuiz() {
        return "take_quiz";
    }

    @GetMapping("/quiz_result")
    public String quizResult() {
        return "quiz_result";
    }
}