package com.insurancesystem.Model.MapStruct;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancesystem.Model.Dto.PrescriptionItemDTO;
import com.insurancesystem.Model.Entity.PrescriptionItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface PrescriptionItemMapper {

    @Mapping(source = "priceList.id", target = "medicineId")
    @Mapping(source = "priceList.serviceName", target = "medicineName")
    @Mapping(source = "priceList.price", target = "unionPrice")
    @Mapping(source = "calculatedQuantity", target = "calculatedQuantity")
    @Mapping(source = "dispensedQuantity", target = "dispensedQuantity")
    @Mapping(source = "coveredQuantity", target = "coveredQuantity")
    @Mapping(source = "drugForm", target = "form")
    @Mapping(source = "unionPricePerUnit", target = "unionPricePerUnit")
    @Mapping(source = "pharmacistPricePerUnit", target = "pharmacistPricePerUnit")
    PrescriptionItemDTO toDto(PrescriptionItem entity);

    @AfterMapping
    default void decodeJson(@MappingTarget PrescriptionItemDTO dto, PrescriptionItem entity) {
        try {
            if (entity.getPriceList() != null && entity.getPriceList().getServiceDetails() != null) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> map = mapper.readValue(entity.getPriceList().getServiceDetails(), Map.class);
                dto.setScientificName((String) map.get("scientificName"));
                dto.setMedicineQuantity(map.get("quantity") != null ? (Integer) map.get("quantity") : 1);
                
                // Use drugForm from entity if available, otherwise extract from serviceDetails
                String form = entity.getDrugForm();
                if (form == null || form.isEmpty()) {
                    form = (String) map.get("form");
                }
                if (form != null && !form.isEmpty()) {
                    dto.setForm(form);
                }
            }
        } catch (Exception e) {
            // Silently handle JSON parsing errors - form might already be set from entity
        }
    }

    @Mapping(target = "prescription", ignore = true)
    @Mapping(target = "priceList", ignore = true)
    @Mapping(target = "finalPrice", ignore = true)
    @Mapping(target = "expiryDate", ignore = true)
    PrescriptionItem toEntity(PrescriptionItemDTO dto);
}

