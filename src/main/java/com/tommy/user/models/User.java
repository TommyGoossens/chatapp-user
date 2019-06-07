package com.tommy.user.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @NotBlank(message = "Email may not be null")
    @Column(unique = true)
    private String email;

    private String firstName;

    private String lastName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date dateOfBirth;

    private String profilePictureLocation = "https://firebasestorage.googleapis.com/v0/b/chatapp-68f21.appspot.com/o/profile_pictures%2Fnot_yet_set.png?alt=media&token=0ee8fca4-196d-4fc7-bbea-f0e366a21a39";

    @OneToMany
    @JoinTable(name="friends")
    @JoinColumn(name = "user_A_id", referencedColumnName = "id")
    @JoinColumn(name="user_B_id", referencedColumnName="id")
    private Set<User> friends = new HashSet<>();

    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public User(String firstName, String lastName, String email, Date dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
    }

    public void addFriend(User friend){
        this.friends.add(friend);
    }


    public void removeFriend(String friendName){
        User friend = this.friends.stream().filter(f -> f.getEmail().equals(friendName)).findFirst().orElseThrow(() -> new UsernameNotFoundException(friendName));
        this.friends.remove(friend);
    }

    public boolean isBefriended(String email) {
        return this.friends.stream().anyMatch(f -> f.getEmail().equals(email));
    }
}
