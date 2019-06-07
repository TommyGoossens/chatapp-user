package com.tommy.user.controllers;

import com.tommy.user.exception.NotExistingEntity;
import com.tommy.user.models.FriendAvailabilityDTO;
import com.tommy.user.models.FriendDTO;
import com.tommy.user.models.UpdatePictureDTO;
import com.tommy.user.models.User;
import com.tommy.user.repository.UserRepo;
import com.tommy.user.utility.ProfileUtility;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/profile")
@Api(value = "/profile", produces = "application/json")
public class UserController {


    private final UserRepo userRepo;
    private final ProfileUtility utility;
    private final RestTemplate restTemplate;

    @Autowired
    public UserController(UserRepo userRepo, ProfileUtility utility, RestTemplate restTemplate) {
        this.userRepo = userRepo;
        this.utility = utility;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/picture")
    public ResponseEntity<?> handleFileUpload(@RequestBody String fileurl) {
        System.out.printf("[Picture received] : %s %n ", fileurl);
        User user = userRepo.findUserByEmail(utility.getUsername()).orElseThrow(() -> new NotExistingEntity("This user does nog exist"));
        user.setProfilePictureLocation(fileurl);
        userRepo.save(user);

        restTemplate.postForEntity("http://lobby-service/userserviceimg",new UpdatePictureDTO(utility.getUsername(),fileurl),void.class);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @RequestMapping("/register")
//    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public void registerUser(@RequestBody User user) {
        userRepo.save(user);

        System.out.println("[User saved] : " + user.getEmail());
    }

    /**
     * @param newUser
     * @return
     */
    @ApiOperation(value = "Update a single user profile", response = User.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User profile retrieved", response = User.class),
            @ApiResponse(code = 403, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "User profile is not found"),
            @ApiResponse(code = 500, message = "Internal server error")

    })
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<?> updateUserProfile(@RequestBody User newUser) {
        User user = userRepo.findUserByEmail(utility.getUsername()).orElseThrow(() -> new NotExistingEntity(utility.getUsername()));
            if(!user.getEmail().equals(newUser.getEmail())) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This is not your profile");

        user.setFirstName(newUser.getFirstName());
        user.setLastName(newUser.getLastName());
        user.setDateOfBirth(newUser.getDateOfBirth());

        userRepo.save(user);

        return ResponseEntity.status(200).body(user);
    }

    /**
     * @return
     */
    @ApiOperation(value = "Get a single user profile", response = User.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User profile retrieved", response = User.class),
            @ApiResponse(code = 403, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "User profile is not found"),
            @ApiResponse(code = 500, message = "Internal server error")

    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<?> getUserProfile() {
        User user = userRepo.findUserByEmail(utility.getUsername()).orElseThrow(() -> new NotExistingEntity(utility.getUsername()));
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @GetMapping("/checkfriend/{email}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> checkFriend(@PathVariable("email") String email) {
        User user = userRepo.findUserByEmail(utility.getUsername()).orElseThrow(() -> new NotExistingEntity(utility.getUsername()));
        if(user.isBefriended(email)) return ResponseEntity.status(HttpStatus.OK).body(new FriendAvailabilityDTO(false,String.format("You are already friends with %s",email)));
        User friend = userRepo.findUserByEmail(email).orElse(null);

        if(friend == null) return ResponseEntity.status(HttpStatus.OK).body(new FriendAvailabilityDTO(false, String.format("%s does not exist",email)));

        return ResponseEntity.status(HttpStatus.OK).body(new FriendAvailabilityDTO(true, String.format("%s can be added!",email)));


    }


    @PutMapping("/addfriend")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> addFriend(@RequestBody FriendDTO friendDTO) {
        System.out.println("[Adding friend] : " + friendDTO.getEmail());
        User user = userRepo.findUserByEmail(utility.getUsername()).orElseThrow(() -> new NotExistingEntity(utility.getUsername()));
        User friend = userRepo.findUserByEmail(friendDTO.getEmail()).orElseThrow(() -> new NotExistingEntity(friendDTO.getEmail()));

        user.addFriend(friend);
        userRepo.save(user);
        return ResponseEntity.status(200).body(user.getFriends());
    }

    @PutMapping("/removefriend")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> removeFriend(@RequestBody FriendDTO friendDTO) {
        User user = userRepo.findUserByEmail(utility.getUsername()).orElseThrow(() -> new NotExistingEntity(utility.getUsername()));

        user.removeFriend(friendDTO.getEmail());
        userRepo.save(user);
        return ResponseEntity.status(200).body(user.getFriends());
    }


}

