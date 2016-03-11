package sk.eea.td.console.form;

import org.hibernate.validator.constraints.ScriptAssert;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@ScriptAssert(lang = "javascript", script = "_this.newPassword.equals(_this.repeatPassword)", message = "New and repeated password does not match.")
public class ChangePasswordForm {

    @NotNull
    private String oldPassword;

    @NotNull
    @Pattern(regexp = "(?=^.{8,}$)(?=.*\\d)(?=.*[!@#$%^&*]+)(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$", message = "The password length must be greater than or equal to 8, must contain at least one uppercase(e.g. 'A'), lowercase(e.g. 'a'), numeric value(0-9) and special character (!@#$%^&*).")
    private String newPassword;

    @NotNull
    private String repeatPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }
}
