package neu.finalPro.LeetcodeFarm.models;

public class User {
    private String username;
    private String userEmail;
    private String id;
    private String image;

    public User(String username, String userEmail, String id) {
        this.username = username;
        this.userEmail = userEmail;
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
