package sk.eea.td.console.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sk.eea.td.console.form.ChangePasswordForm;

import javax.validation.Valid;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class UserProfileController {

    private static final Logger LOG = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsManager userDetailsManager;

    @Autowired AuthenticationManager authenticationManager;

    @RequestMapping("/user_profile")
    public String userProfile(ChangePasswordForm changePasswordForm) {
        return "user_profile";
    }

    @RequestMapping(value = "/user_profile/change_password", method = RequestMethod.POST)
    public String submitChangePassword(@Valid ChangePasswordForm changePasswordForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            LOG.debug("Validation errors: " + bindingResult.toString());

            //model.addAttribute("success", true);
            return "user_profile";
        }

        try {
            userDetailsManager.changePassword(changePasswordForm.getOldPassword(), passwordEncoder.encode(changePasswordForm.getNewPassword()));
        } catch (AuthenticationException e) {
            LOG.debug("Authentication with old password failed.", e);
            bindingResult.rejectValue("oldPassword", "user.profile.title.error.old.password");
            return "user_profile";
        }


        return "redirect:/user_profile?success=true";
    }
}
