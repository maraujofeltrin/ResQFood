package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ProfileChangePasswordForm {

    @NotBlank(message = "{profile.changePassword.validation.current.notEmpty}")
    private String currentPassword;

    @NotBlank(message = "{passwordReset.validation.password.notEmpty}")
    @Size(min = 8, max = 100, message = "{passwordReset.validation.password.size}")
    private String newPassword;

    @NotBlank(message = "{passwordReset.validation.confirmPassword.notEmpty}")
    private String confirmPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(final String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(final String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(final String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    @AssertTrue(message = "{passwordReset.validation.passwords.mismatch}")
    public boolean isNewPasswordMatchingConfirm() {
        if (newPassword == null || confirmPassword == null) {
            return true;
        }
        return newPassword.equals(confirmPassword);
    }
}
