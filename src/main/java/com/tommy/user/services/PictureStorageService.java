package com.tommy.user.services;

import com.tommy.user.models.PictureResponse;
import com.tommy.user.models.User;
import com.tommy.user.repository.UserRepo;
import com.tommy.user.utility.ProfileUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@Service
public class PictureStorageService {
    Logger logger = Logger.getLogger(this.getClass().getName());
    private final Path rootLocation = Paths.get("profile-pics-dir");
    Set<String> files = new HashSet<>();


    private UserRepo userRepo;
    private final ProfileUtility utility;

    @Autowired
    public PictureStorageService(UserRepo userRepo, ProfileUtility utility) {
        this.userRepo = userRepo;
        this.utility = utility;
    }

    /**
     * Stores the profile picture on the server and stores the file name on the server
     * @param profilePicture MultipartFile containing the profile picture
     * @return
     */
    public ResponseEntity<?> storeProfilePicture(MultipartFile profilePicture) {
        User user = userRepo.findUserByEmail(utility.getUsername()).orElseThrow(() -> new RuntimeException("User does not exist"));
        user.setProfilePictureLocation(setUserIdAsFileName(profilePicture,user.getId()));

        try {
            Files.copy(profilePicture.getInputStream(), this.rootLocation.resolve(user.getProfilePictureLocation()));
            userRepo.save(user);
            return ResponseEntity.status(HttpStatus.OK).body(user.getProfilePictureLocation());
        } catch (IOException ex) {
            throw new RuntimeException("Error while accessing location");
        }
    }

    /**
     * @return
     */
    public ResponseEntity<?> retrieveProfilePicture() {
        User user = userRepo.findUserByEmail(utility.getUsername()).orElseThrow(() -> new RuntimeException("User does not exist"));
        String mvcUri = MvcUriComponentsBuilder.fromController(PictureStorageService.class).build().toString();
        return ResponseEntity.status(HttpStatus.OK).body(new PictureResponse(mvcUri + "profile/files/" + user.getProfilePictureLocation()));
    }




    /**
     * Initializes the storage. The function deleteAll() is mandatory
     */
    public void init() {
        try {
            Files.createDirectory(rootLocation);
            files.add("not_yet_set.png");
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage!");
        }
    }

    /**
     * Delete all files recursively in the rootlocation
     */
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }


    /**
     * @param unalteredFile
     * @return
     */
    private String setUserIdAsFileName(MultipartFile unalteredFile, Long id) {
        int dot = unalteredFile.getOriginalFilename().indexOf('.');
        String fileExtension = unalteredFile.getOriginalFilename().substring(dot);

        return id + fileExtension;
    }


    public Resource loadFile(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Error while loading");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error while loading");
        }
    }
}