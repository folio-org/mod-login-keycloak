package org.folio.login.domain.repository;

import java.util.Optional;
import java.util.UUID;
import org.folio.login.domain.entity.PasswordCreateActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordCreateActionRepository extends JpaRepository<PasswordCreateActionEntity, UUID> {

  Optional<PasswordCreateActionEntity> findPasswordCreateActionEntityByUserId(UUID userId);
}
