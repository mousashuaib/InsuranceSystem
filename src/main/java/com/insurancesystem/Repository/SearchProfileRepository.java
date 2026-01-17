package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.ProfileStatus;
import com.insurancesystem.Model.Entity.SearchProfile;
import com.insurancesystem.Model.Entity.Enums.SearchProfileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SearchProfileRepository extends JpaRepository<SearchProfile, UUID> {

    List<SearchProfile> findByNameContainingIgnoreCase(String name);

    List<SearchProfile> findByNameContainingIgnoreCaseAndType(String name, SearchProfileType type);

    List<SearchProfile> findByType(SearchProfileType type);

    List<SearchProfile> findByNameContainingIgnoreCaseAndStatus(String name, ProfileStatus status);

    List<SearchProfile> findByNameContainingIgnoreCaseAndTypeAndStatus(String name, SearchProfileType type, ProfileStatus status);

    List<SearchProfile> findByTypeAndStatus(SearchProfileType type, ProfileStatus status);

    List<SearchProfile> findByStatus(ProfileStatus status);


    // ✅ يرجع بروفايل صاحب الـ ID (إن وجد)
    Optional<SearchProfile> findByOwnerId(UUID ownerId);

    List<SearchProfile> findAllByOwnerId(UUID ownerId);
    @Query("""
    select sp
    from SearchProfile sp
    join fetch sp.owner o
    left join fetch o.universityCardImages
    where sp.owner.id = :ownerId
""")
    List<SearchProfile> findMyProfilesWithOwnerImages(@Param("ownerId") UUID ownerId);

}
