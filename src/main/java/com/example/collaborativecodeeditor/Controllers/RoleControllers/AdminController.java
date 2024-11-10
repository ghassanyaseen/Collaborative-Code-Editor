package com.example.collaborativecodeeditor.Controllers.RoleControllers;

import com.example.collaborativecodeeditor.Entity.Role;
import com.example.collaborativecodeeditor.Entity.User;
import com.example.collaborativecodeeditor.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
@Secured("ROLE_ADMIN")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping()
    @Secured({"ROLE_ADMIN"})
    public String adminPage() {
        return "Admin/admin";
    }


    @GetMapping("/user-management")
    @Secured({"ROLE_ADMIN"})
    public String userManagement(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("roles", Role.values());
        return "Admin/user-management";
    }

    @PostMapping("/update-role")
    @Secured({"ROLE_ADMIN"})
    public String updateUserRole(@RequestParam Long userId, @RequestParam String newRole) {
        Role role = Role.valueOf(newRole);
        userService.updateUserRole(userId, role);
        return "redirect:/admin/user-management";
    }

    @PostMapping("/delete")
    @Secured({"ROLE_ADMIN"})
    public String deleteUser(@RequestParam Long userId) {
        userService.deleteUser(userId);
        return "redirect:/admin/user-management";
    }
}
