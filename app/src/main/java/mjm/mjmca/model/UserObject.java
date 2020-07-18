package mjm.mjmca.model;

import android.graphics.Bitmap;
import android.net.Uri;

public class UserObject {
    public String id, name, phone;
    public Bitmap image;
    public Uri photoUri;

    public UserObject(){

    }

    public UserObject(String id, String name, String phone, Bitmap image, Uri photoUri) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.image = image;
        this.photoUri = photoUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }
}
