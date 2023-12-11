package org.folio.login.mapper;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;

import org.folio.login.domain.dto.PasswordCreateAction;
import org.folio.login.domain.entity.PasswordCreateActionEntity;
import org.mapstruct.Mapper;

/**
 * Mapper for mapping {@link PasswordCreateAction} objects to {@link PasswordCreateActionEntity} objects and
 * vice versa.
 */
@Mapper(componentModel = "spring", injectionStrategy = CONSTRUCTOR)
public interface PasswordCreateActionMapper {

  PasswordCreateActionEntity toEntity(PasswordCreateAction passwordCreateAction);

  PasswordCreateAction toDto(PasswordCreateActionEntity passwordCreateActionEntity);
}
