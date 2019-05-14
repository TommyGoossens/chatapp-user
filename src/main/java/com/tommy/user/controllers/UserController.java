package com.tommy.user.controllers;

import com.tommy.user.models.User;
import com.tommy.user.repository.UserRepo;
import com.tommy.user.services.PictureStorageService;
import com.tommy.user.utility.ProfileUtility;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/profile")
@Api(value = "/profile", produces = "application/json")
public class UserController {


    private final PictureStorageService pictureStorageService;
    private final UserRepo userRepo;
    private final ProfileUtility utility;

    @Autowired
    public UserController(PictureStorageService pictureStorageService, UserRepo userRepo, ProfileUtility utility) {
        this.pictureStorageService = pictureStorageService;
        this.userRepo = userRepo;
        this.utility = utility;
    }

    @PostMapping("/picture")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile profilePicture) {
        return pictureStorageService.storeProfilePicture(profilePicture);
    }

    @GetMapping("/picture")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<?> retrieveProfilePicture() {
        return pictureStorageService.retrieveProfilePicture();
    }

    /**
     * @return
     */
    @ApiOperation(value = "Create a new user profile", response = User.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User profile retrieved", response = User.class),
            @ApiResponse(code = 403, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "User profile is not found"),
            @ApiResponse(code = 500, message = "Internal server error")

    })
    @PostMapping
//    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public void createUserProfile(@RequestBody User user) {
        userRepo.save(user);

        System.out.println("[User saved] : " + userRepo.findUserByEmail(user.getEmail()));
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
        throw new NotImplementedException();
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
        User user = userRepo.findUserByEmail(utility.getUsername()).orElseThrow(RuntimeException::new);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }


    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Resource file = pictureStorageService.loadFile(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

}

