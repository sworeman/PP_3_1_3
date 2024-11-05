package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.services.RoleService;
import ru.kata.spring.boot_security.demo.services.UserService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String adminPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User user = (User) userService.loadUserByUsername(username);
        model.addAttribute("username", username);
        model.addAttribute("roles", user.getRoles());
        model.addAttribute("users", userService.findAll());
        model.addAttribute("allRoles", roleService.findAll());
        model.addAttribute("newUser", new User());
        return "admin";
    }

    public String getRoleString(User user) {
        return user.getRoles().stream()
                .map(role -> role.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.joining(", "));
    }

    @PostMapping("/add")
    public String addUser(@ModelAttribute("newUser") User user, @RequestParam("roles") List<Long> roleIds) {
        try {
            Set<Role> roles = roleService.findByIds(roleIds);
            user.setRoles(roles);
            userService.save(user);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/admin";
    }

    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleService.findAll());
        return "edit_user";
    }

    @PostMapping("/update_user")
    public String updateUser(@ModelAttribute User user, @RequestParam Long id, Model model) {
        user.setId(id);
        userService.updateUser(user);
        return "redirect:/admin";
    }
}
