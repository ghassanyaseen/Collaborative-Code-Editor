package com.example.collaborativecodeeditor.Controllers.Pages;

import com.example.collaborativecodeeditor.Entity.Provider;
import com.example.collaborativecodeeditor.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/signup")
public class SignupController {

    @Autowired
    private UserService userService;


    @GetMapping
    public String signup() {
        return "signup";
    }

    @PostMapping
    public String signup(
            @RequestParam String username,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        
        if (userService.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Username already exists!");
            return "redirect:/signup";
        }

        userService.saveNewUser(username,password,Provider.NORMAL,null);

        redirectAttributes.addFlashAttribute("success", "Account created successfully! Please log in.");
        return "redirect:/login";
    }
}
