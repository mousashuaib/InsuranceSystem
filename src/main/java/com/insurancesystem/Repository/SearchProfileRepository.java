package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Enums.ProfileStatus;
import com.insurancesystem.Model.Entity.SearchProfile;
import com.insurancesystem.Model.Entity.Enums.SearchProfileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SearchProfileRepository extends JpaRepository<SearchProfile, UUID> {

    List<SearchProfile> findByNameContainingIgnoreCase(String name);

    List<SearchProfile> findByNameContainingIgnoreCaseAndType(String name, SearchProfileType type);

    List<SearchProfile> findByType(SearchProfileType type);

    List<SearchProfile> findByNameContainingIgnoreCaseAndStatus(String name, ProfileStatus status);

    List<SearchProfile> findByNameContainingIgnoreCaseAndTypeAndStatus(String name, SearchProfileType type, ProfileStatus status);

    List<SearchProfile> findByTypeAndStatus(SearchProfileType type, ProfileStatus status);

    List<SearchProfile> findByStatus(ProfileStatus status);

}
