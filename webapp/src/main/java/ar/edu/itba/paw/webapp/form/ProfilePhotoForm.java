package ar.edu.itba.paw.webapp.form;

import org.springframework.web.multipart.MultipartFile;

public class ProfilePhotoForm {

    private MultipartFile photo;

    public MultipartFile getPhoto() {
        return photo;
    }

    public void setPhoto(final MultipartFile photo) {
        this.photo = photo;
    }
}
