package sk.eea.td.console.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class ChangeEmailForm {

    private String currentEmail;

    @NotNull
    @Pattern(regexp = "^.+@.+\\..+$", message = "Value must be valid e-mail address. E.g. example@mail.com")
    private String newEmail;

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getCurrentEmail() {
        return currentEmail;
    }

    public void setCurrentEmail(String currentEmail) {
        this.currentEmail = currentEmail;
    }

    @Override public String toString() {
        return "ChangeEmailForm{" +
                "currentEmail='" + currentEmail + '\'' +
                ", newEmail='" + newEmail + '\'' +
                '}';
    }
}
