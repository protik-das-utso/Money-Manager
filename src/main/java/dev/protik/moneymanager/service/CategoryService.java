package dev.protik.moneymanager.service;

import dev.protik.moneymanager.dto.CategoryDTO;
import dev.protik.moneymanager.entity.CategoryEntity;
import dev.protik.moneymanager.entity.ProfileEntity;
import dev.protik.moneymanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;

    // save category
    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        if(categoryRepository.existsByCategoryNameAndProfileId(categoryDTO.getCategoryName(), profile.getId())) {
            throw new RuntimeException("Category with name " + categoryDTO.getCategoryName() + " already exists for this profile.");
        }
        CategoryEntity newCategory = toEntity(categoryDTO, profile);
        newCategory = categoryRepository.save(newCategory);
        return toDTO(newCategory);
    }

    // get category for current user
    public List<CategoryDTO> getCategoryForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> categories = categoryRepository.findByProfileId(profile.getId());
        return categories.stream().map(this::toDTO).toList();
    }

    // get categories by type for current user
    public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> categories = categoryRepository.findByTypeAndProfileId(type, profile.getId());
        return categories.stream().map(this::toDTO).toList();
    }

    // update category
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity existingCategory = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new RuntimeException("Category not found or accessible"));

        existingCategory.setCategoryName(categoryDTO.getCategoryName());
        existingCategory.setIcon(categoryDTO.getIcon());
        existingCategory.setType(categoryDTO.getType());

        existingCategory = categoryRepository.save(existingCategory);
        return toDTO(existingCategory);
    }

    // helper method
    private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profile) {
        return CategoryEntity.builder()
                .categoryName(categoryDTO.getCategoryName())
                .icon(categoryDTO.getIcon())
                .type(categoryDTO.getType())
                .profile(profile)
                .build();
    }
    public CategoryDTO toDTO(CategoryEntity entity) {
       return CategoryDTO.builder()
               .id(entity.getId())
               .profileId(entity.getProfile().getId() != null ? entity.getProfile().getId() : null)
               .categoryName(entity.getCategoryName())
               .icon(entity.getIcon())
               .type(entity.getType())
               .createdAt(entity.getCreatedAt())
               .updatedAt(entity.getUpdatedAt())
               .build();
    }

}
