package tk.nikomitk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class User {

    private int profilePicture;
    private String userName;
    private String nickName;
    @JsonIgnore
    private String password;

    public User(String userName, int profilePicture) {
        this.profilePicture = profilePicture;
        this.userName = userName;
        this.nickName = userName;
    }
}