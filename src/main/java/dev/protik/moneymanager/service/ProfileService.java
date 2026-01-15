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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.activation.url}")
    private String activationURL;

    public ProfileDTO registerProfile(ProfileDTO profileDTO) {

        ProfileEntity newProfile = toEntity(profileDTO);

        // encode password once before saving
        newProfile.setPassword(passwordEncoder.encode(newProfile.getPassword()));
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile = profileRepository.save(newProfile);

        // sending email for account activation using google mail service
        String activationLink =
                activationURL+"/api/v1.0/activate?token=" + newProfile.getActivationToken();
        String body =
                "<div style='background:#f4f6f8; padding:5px; font-family:Arial, Helvetica, sans-serif;'>" +

                        "<div style='max-width:600px; margin:0 auto; background:#ffffff; padding:30px; " +
                        "border-radius:10px; box-shadow:0 6px 15px rgba(0,0,0,0.08);'>" +

                        "<h2 style='color:#2c3e50; margin-top:0;'>Hello, " + newProfile.getFullName() + " 👋</h2>"
                        +

                        "<p style='color:#555; font-size:15px; line-height:1.7;'>" +
                        "Thank you for creating an account with us! To complete your registration, " +
                        "please activate your account by clicking the button below." +
                        "</p>" +

                        "<div style='text-align:center; margin:35px 0;'>" +
                        "<a href='" + activationLink + "' " +
                        "style='background:#27ae60; color:#ffffff; text-decoration:none; " +
                        "padding:14px 30px; border-radius:8px; font-size:16px; font-weight:bold; " +
                        "display:inline-block;'>" +
                        "Activate Your Account" +
                        "</a>" +
                        "</div>" +

                        "<p style='color:#777; font-size:14px; line-height:1.6;'>" +
                        "If the button above does not work, copy and paste the following link into your browser:" +
                        "</p>" +

                        "<p style='word-break:break-all; background:#f9f9f9; padding:12px; " +
                        "border-radius:6px; font-size:13px; color:#2980b9;'>" +
                        activationLink +
                        "</p>" +

                        "<hr style='border:none; border-top:1px solid #eaeaea; margin:30px 0;'>" +

                        "<p style='color:#555; font-size:14px; line-height:1.6;'>" +
                        "<strong>⏰ Important:</strong><br>" +
                        "This activation link is valid for a limited time. If it expires, you may need to request a new one." +
                        "</p>" +

                        "<p style='color:#555; font-size:14px; line-height:1.6;'>" +
                        "<strong>🔒 Security Notice:</strong><br>" +
                        "If you did not create this account, please ignore this email. No action is required." +
                        "</p>" +

                        "<p style='color:#555; font-size:14px; line-height:1.6;'>" +
                        "If you face any issues or have questions, feel free to reply to this email — " +
                        "we’re happy to help 😊" +
                        "</p>" +

                        "<hr style='border:none; border-top:1px solid #eaeaea; margin:30px 0;'>" +

                        "<p style='color:#999; font-size:12px; text-align:center;'>" +
                        "Best regards,<br>" +
                        "<strong>Protik The DEV</strong><br>" +
                        "Money Manager" +
                        "</p>" +

                        "</div>" +
                        "</div>";

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
