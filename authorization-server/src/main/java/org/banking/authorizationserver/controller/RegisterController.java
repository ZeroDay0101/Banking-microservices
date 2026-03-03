package org.banking.authorizationserver.controller;

import jakarta.validation.Valid;
import org.banking.authorizationserver.dto.CreateAccountRequest;
import org.banking.authorizationserver.dto.RegisterForm;
import org.banking.authorizationserver.service.UserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/register")
public class RegisterController {
    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String registerPage(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegisterForm());
        }
        return "register";
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String registerAccountFromForm(
            @Valid @ModelAttribute("form") RegisterForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "register.password.mismatch", "Passwords do not match");
            return "register";
        }

        try {
            userService.createAccount(form.getUsername(), form.getPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully");
            return "redirect:/register";
        } catch (DuplicateKeyException ex) {
            bindingResult.rejectValue("username", "register.username.duplicate", ex.getMessage());
            return "register";
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> registerAccount(@RequestBody @Valid CreateAccountRequest account) {
        userService.createAccount(account.username(), account.password());
        return new ResponseEntity<>("Account created successfully", HttpStatus.OK);
    }

}
