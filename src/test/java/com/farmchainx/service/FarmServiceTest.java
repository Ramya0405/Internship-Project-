package com.farmchainx.service;

import com.farmchainx.TestEntityHelper;
import com.farmchainx.dto.FarmDto;
import com.farmchainx.entity.Farm;
import com.farmchainx.entity.User;
import com.farmchainx.enums.UserRole;
import com.farmchainx.exception.ResourceNotFoundException;
import com.farmchainx.exception.UnauthorizedAccessException;
import com.farmchainx.repository.FarmRepository;
import com.farmchainx.service.impl.FarmServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmServiceTest {

    @Mock
    private FarmRepository farmRepository;

    @InjectMocks
    private FarmServiceImpl farmService;

    private User owner;
    private User otherFarmer;
    private User admin;
    private Farm farm;

    @BeforeEach
    void setUp() {
        owner = new User();
        TestEntityHelper.withId(owner, 10L);
        owner.setFullName("John Farmer");
        owner.setRole(UserRole.FARMER);

        otherFarmer = new User();
        TestEntityHelper.withId(otherFarmer, 20L);
        otherFarmer.setFullName("Bob Farmer");
        otherFarmer.setRole(UserRole.FARMER);

        admin = new User();
        TestEntityHelper.withId(admin, 1L);
        admin.setFullName("Admin");
        admin.setRole(UserRole.ADMIN);

        farm = new Farm();
        TestEntityHelper.withId(farm, 100L);
        farm.setFarmName("Green Meadows");
        farm.setLocation("Punjab, India");
        farm.setFarmSize(50.0);
        farm.setCertification("Organic India");
        farm.setOwner(owner);
    }

    @Test
    @DisplayName("Create farm successfully")
    void create_success() {
        FarmDto dto = new FarmDto();
        dto.setFarmName("Green Meadows");
        dto.setLocation("Punjab, India");
        dto.setFarmSize(50.0);
        dto.setCertification("Organic India");

        when(farmRepository.save(any(Farm.class))).thenAnswer(i -> {
            Farm f = i.getArgument(0);
            TestEntityHelper.withId(f, 100L);
            return f;
        });

        FarmDto created = farmService.create(dto, owner);

        assertThat(created).isNotNull();
        assertThat(created.getFarmName()).isEqualTo("Green Meadows");
        assertThat(created.getOwnerId()).isEqualTo(10L);
        verify(farmRepository).save(any(Farm.class));
    }

    @Test
    @DisplayName("Update farm by owner successfully")
    void update_byOwner_success() {
        when(farmRepository.findById(100L)).thenReturn(Optional.of(farm));
        when(farmRepository.save(any(Farm.class))).thenAnswer(i -> i.getArgument(0));

        FarmDto updateDto = new FarmDto();
        updateDto.setFarmName("Updated Meadows");
        updateDto.setLocation("New Location");

        FarmDto updated = farmService.update(100L, updateDto, owner);

        assertThat(updated.getFarmName()).isEqualTo("Updated Meadows");
        assertThat(updated.getLocation()).isEqualTo("New Location");
    }

    @Test
    @DisplayName("Update farm by non-owner throws UnauthorizedAccessException")
    void update_byNonOwner_throwsException() {
        when(farmRepository.findById(100L)).thenReturn(Optional.of(farm));

        FarmDto updateDto = new FarmDto();
        updateDto.setFarmName("Hacked Farm");

        assertThatThrownBy(() -> farmService.update(100L, updateDto, otherFarmer))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("You do not have permission to modify farm");
    }

    @Test
    @DisplayName("Admin can update any farm")
    void update_byAdmin_success() {
        when(farmRepository.findById(100L)).thenReturn(Optional.of(farm));
        when(farmRepository.save(any(Farm.class))).thenAnswer(i -> i.getArgument(0));

        FarmDto updateDto = new FarmDto();
        updateDto.setFarmName("Admin Updated Name");

        FarmDto updated = farmService.update(100L, updateDto, admin);

        assertThat(updated.getFarmName()).isEqualTo("Admin Updated Name");
    }

    @Test
    @DisplayName("Delete farm by owner")
    void delete_byOwner_success() {
        when(farmRepository.findById(100L)).thenReturn(Optional.of(farm));
        doNothing().when(farmRepository).delete(farm);

        farmService.delete(100L, owner);

        verify(farmRepository).delete(farm);
    }

    @Test
    @DisplayName("Delete farm by non-owner throws UnauthorizedAccessException")
    void delete_byNonOwner_throwsException() {
        when(farmRepository.findById(100L)).thenReturn(Optional.of(farm));

        assertThatThrownBy(() -> farmService.delete(100L, otherFarmer))
                .isInstanceOf(UnauthorizedAccessException.class);

        verify(farmRepository, never()).delete(any(Farm.class));
    }
}
