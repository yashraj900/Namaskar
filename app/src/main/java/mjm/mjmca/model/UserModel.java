package mjm.mjmca.model;

public class UserModel {
    private String uid, username, name, about, phone, image;

    public UserModel(){

    }

    public UserModel(String uid, String username, String name, String about, String phone, String image) {
        this.uid = uid;
        this.username = username;
        this.name = name;
        this.about = about;
        this.phone = phone;
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
