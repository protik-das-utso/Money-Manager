package dev.protik.moneymanager.service;

import dev.protik.moneymanager.dto.AuthDTO;
import dev.protik.moneymanager.dto.ProfileDTO;
import dev.protik.moneymanager.entity.ProfileEntity;
import dev.protik.moneymanager.repository.ProfileRepository;
import dev.protik.moneymanager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public ProfileDTO registerProfile(ProfileDTO profileDTO) {

        ProfileEntity newProfile = toEntity(profileDTO);

        // encode password once before saving
        newProfile.setPassword(passwordEncoder.encode(newProfile.getPassword()));
        newProfile = profileRepository.save(newProfile);

        // sending email for account activation using google mail service
        newProfile.setActivationToken(UUID.randomUUID().toString());
        String activationLink =
                "localhost:8086/api/v1.0/activate?token=" + newProfile.getActivationToken();
        String body =
                "Hello 👋,\n\n" +
                        "Thank you for creating an account with us!\n\n" +
                        "To complete your registration, please activate your account by clicking the link below:\n\n" +
                        "🔗 Activate Your Account\n" +
                        activationLink + "\n\n" +
                        "⏰ Important:\n" +
                        "This activation link is valid for a limited time. If you do not activate your account within this period, you may need to request a new link.\n\n" +
                        "🔒 Security Notice:\n" +
                        "If you did not create this account, please ignore this email. No action is required.\n\n" +
                        "If you face any issues or have questions, feel free to reply to this email — we’re happy to help 😊\n\n" +
                        "Best regards,\n" +
                        "Protik The DEV\n" +
                        "Money Manager";
        emailService.sendMail(
                newProfile.getEmail(),
                "Account Activation Request",
                body
        );

        return toDTO(newProfile);

    }

    // DTO to Entity
    public ProfileEntity toEntity(ProfileDTO profileDTO) {
        return ProfileEntity.builder()
                .id(profileDTO.getId())
                .fullName(profileDTO.getFullName())
                .email(profileDTO.getEmail())
                // store raw password here; encoding will be handled by registerProfile to avoid double-encoding
                .password(profileDTO.getPassword())
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .createdAt(profileDTO.getCreatedAt())
                .updatedAt(profileDTO.getUpdatedAt())
                .build();
    }

    public ProfileDTO toDTO(ProfileEntity profileEntity) {
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .fullName(profileEntity.getFullName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build();
    }

    public boolean activateProfile(String activationToken) {
        return profileRepository.findByActivationToken(activationToken)
                .map(profile -> {
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return true;
                }).orElse(false);

    }

    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return profileRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));
    }

    public ProfileDTO getPublicProfile(String email) {
        ProfileEntity currentUser = null;
        if (email == null) {
            currentUser = getCurrentProfile();
        } else {
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));
        }
        return toDTO(currentUser);
    }


    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authDTO.getEmail(),
                            authDTO.getPassword()
                    )
            );
            // Generate JWT token
            String token = jwtUtil.generateToken(authDTO.getEmail());
            return Map.of(
                    "token", token,
                    "user", getPublicProfile(authDTO.getEmail())
            );
        } catch (Exception e) {
            log.error("Authentication failed for user {}: {}", authDTO.getEmail(), e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
