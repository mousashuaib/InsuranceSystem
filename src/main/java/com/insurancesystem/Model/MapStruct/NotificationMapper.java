package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.NotificationDTO;
import com.insurancesystem.Model.Entity.Notification;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(source = "recipient.fullName", target = "recipientName")
    NotificationDTO toDto(Notification notification);

    @InheritInverseConfiguration
    @Mapping(target = "recipient", ignore = true)
    Notification toEntity(NotificationDTO dto);
}
