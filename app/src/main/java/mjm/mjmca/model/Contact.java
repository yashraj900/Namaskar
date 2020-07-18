package mjm.mjmca.model;

public class Contact {

    private String id, name, phone, image, uid, username, about, state;
    public boolean isBlocked = false;
    public Contact(){

    }

    public Contact(String id, String name, String phone, String image, String uid, String username, String about, String state, boolean isBlocked) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.image = image;
        this.uid = uid;
        this.username = username;
        this.about = about;
        this.state = state;
        this.isBlocked = isBlocked;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
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

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
