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
import springfox.documentation.annotations.ApiIgnore;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/profile")
//@Api(value = "/profile", produces = "application/json")
@Api(tags = {"Profile"})
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


    //=======================================
    //       Profile related methods
    //=======================================

    /**
     * Sets the URL to the profile picture uploaded to firebase
     * @param fileurl FireBase location
     * @return HttpStatus OK (200)
     */
    @ApiOperation(value = "Update the profile picture location in the backend", tags = "Profile")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "[Profile picture updated]"),
            @ApiResponse(code = 403, message = "[Unauthorized]"),
            @ApiResponse(code = 404, message = "[This user does not exist]")
    })
    @PostMapping("/picture")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> handleFileUpload(@RequestBody String fileurl) {
        User user = userRepo.findUserByEmail(utility.getUsername()).orElseThrow(() -> new NotExistingEntity(utility.getUsername()));
        user.setProfilePictureLocation(fileurl);
        userRepo.save(user);
        restTemplate.postForEntity("http://lobby-service/userserviceimg",new UpdatePictureDTO(utility.getUsername(),fileurl),void.class);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Update profile information
     * @param newUser containing the new information
     * @return User object
     */
    @ApiOperation(value = "Update a single user profile", tags = "Profile")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "[User profile retrieved]", response = User.class),
            @ApiResponse(code = 403, message = "[Unauthorized]"),
            @ApiResponse(code = 404, message = "[This user does not exist]")
    })
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<?> updateUserProfile(@RequestBody User newUser) {
        User user = userRepo.findUserByEmail(utility.getUsername()).orElseThrow(() -> new NotExistingEntity(utility.getUsername()));
        user.setFirstName(newUser.getFirstName());
        user.setLastName(newUser.getLastName());
        user.setDateOfBirth(newUser.getDateOfBirth());

        userRepo.save(user);

        return ResponseEntity.status(200).body(user);
    }

    /**
     * Rerieve all profile information
     * @param email email of the user
     * @return User object
     */
    @ApiOperation(value = "Get a single user profile", tags = "Profile")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "[User profile retrieved]", response = User.class),
            @ApiResponse(code = 403, message = "[Unauthorized]"),
            @ApiResponse(code = 404, message = "[This user does not exist]")

    })
    @GetMapping("/{email}")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<?> getUserProfile(@PathVariable("email") String email) {
        User user = userRepo.findUserByEmail(email).orElseThrow(() -> new NotExistingEntity(email));
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    //=======================================
    //       Friend related methods
    //=======================================

    /**
     * Checks if the new friend exists / not already befriended
     * @param email which needs to be checked
     * @return FriendAvailabilityDTO
     */
    @ApiOperation(value = "Check friend availability", tags = "Profile")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "[Friend can be added]", response = FriendAvailabilityDTO.class),
            @ApiResponse(code = 403, message = "[Unauthorized]"),
            @ApiResponse(code = 404, message = "[This user does not exist]"),
            @ApiResponse(code = 404, message = "[This user does not exist]", response = FriendAvailabilityDTO.class)
    })
    @GetMapping("/checkfriend/{email}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> checkFriend(@PathVariable("email") String email) {
        User user = userRepo.findUserByEmail(utility.getUsername()).orElseThrow(() -> new NotExistingEntity(utility.getUsername()));
        if(user.isBefriended(email)) return ResponseEntity.status(HttpStatus.OK).body(new FriendAvailabilityDTO(false,String.format("You are already friends with %s",email)));

        User friend = userRepo.findUserByEmail(email).orElse(null);
        if(friend == null) return ResponseEntity.status(HttpStatus.OK).body(new FriendAvailabilityDTO(false, String.format("%s does not exist",email)));

        return ResponseEntity.status(HttpStatus.OK).body(new FriendAvailabilityDTO(true, String.format("%s can be added!",email)));
    }

    /**
     * Adds a friend relationship to the database
     * @param friendDTO object containing the email adress
     * @return list of the current friends
     */
    @ApiOperation(value = "Add a friend", tags = "Profile")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "[Friend added]" ,response = User.class, responseContainer = "Set"),
            @ApiResponse(code = 403, message = "[Unauthorized]"),
            @ApiResponse(code = 404, message = "[This user does not exist]"),
    })
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

    /**
     * Removes a friend relationship from the database
     * @param friendDTO object containing the email adress
     * @return list of the current friends
     */
    @ApiOperation(value = "Remove a friend", tags = "Profile")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "[Friend removed]" ,response = User.class, responseContainer = "Set"),
            @ApiResponse(code = 403, message = "[Unauthorized]"),
            @ApiResponse(code = 404, message = "[This user does not exist]"),
    })
    @PutMapping("/removefriend")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> removeFriend(@RequestBody FriendDTO friendDTO) {
        User user = userRepo.findUserByEmail(utility.getUsername()).orElseThrow(() -> new NotExistingEntity(utility.getUsername()));

        user.removeFriend(friendDTO.getEmail());
        userRepo.save(user);
        return ResponseEntity.status(200).body(user.getFriends());
    }

    //=======================================
    //       Methods handling input
    //        from other services
    //=======================================

    @ApiIgnore
    @RequestMapping("/register")
//    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public void registerUser(@RequestBody User user) {
        userRepo.save(user);

        System.out.println("[User saved] : " + user.getEmail());
    }
}

