package dev.protik.moneymanager.controller;

import dev.protik.moneymanager.dto.AuthDTO;
import dev.protik.moneymanager.dto.ProfileDTO;
import dev.protik.moneymanager.entity.ProfileEntity;
import dev.protik.moneymanager.repository.ProfileRepository;
import dev.protik.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final ProfileRepository profileRepository;

    @PostMapping("/register")
    public ResponseEntity<ProfileDTO> registerProfile(@RequestBody ProfileDTO profileDTO) {
        ProfileDTO registeredProfile = profileService.registerProfile(profileDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredProfile);
    }
    @GetMapping("/activate")
    public ResponseEntity<String> activateProfile(@RequestParam String token) {
        boolean isActivated = profileService.activateProfile(token);
        if (!isActivated) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid activation token.");
        } else{
            return ResponseEntity.ok("Profile activated successfully.");
        }
    }
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthDTO authDTO) {
        try{
            Optional<ProfileEntity> user = profileRepository.findByEmail(authDTO.getEmail());
            if(user.isPresent()){
                if(!profileService.isAccountActive(authDTO.getEmail())){
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Account is not activated." + authDTO.getEmail()));
                }
                System.out.println("check1");

                Map<String, Object> response = profileService.authenticateAndGenerateToken(authDTO);
                System.out.println("check1");
                return ResponseEntity.ok(response);
            } else{
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "No account found in this email"));
            }

        } catch (Exception e){
            // Return the actual exception message temporarily to help debugging
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

}
