package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.NotificationDTO;
import com.insurancesystem.Model.Entity.Notification;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(target = "replied", source = "replied")
    @Mapping(source = "recipient.fullName", target = "recipientName")
    @Mapping(source = "recipient.id", target = "recipientId")
    @Mapping(source = "sender.id", target = "senderId")          // ✅ أضف المرسل
    @Mapping(source = "sender.fullName", target = "senderName")  // ✅ أضف اسم المرسل
    @Mapping(source = "type", target = "type")
    NotificationDTO toDto(Notification entity);

    @InheritInverseConfiguration
    @Mapping(target = "recipient", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "replied", source = "replied")
    Notification toEntity(NotificationDTO dto);
}
