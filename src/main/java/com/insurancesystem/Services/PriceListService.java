package com.insurancesystem.Services;

import com.insurancesystem.Model.Dto.*;
import com.insurancesystem.Model.Entity.*;
import com.insurancesystem.Model.MapStruct.PriceListMapper;
import com.insurancesystem.Repository.PriceListRepository;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Model.Entity.Enums.ProviderType;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class PriceListService {

    private final PriceListRepository priceListRepository;
    private final PriceListMapper priceListMapper;

    public PriceListResponseDTO create(CreatePriceListDTO dto) {

        PriceList entity = PriceList.builder()
                .providerType(dto.getProviderType())
                .serviceName(dto.getServiceName())
                .serviceCode(dto.getServiceCode())
                .price(dto.getPrice())
                .notes(dto.getNotes())
                .serviceDetails(dto.getServiceDetails())
                .active(true)
                .build();

        priceListRepository.save(entity);
        return priceListMapper.toDto(entity);
    }

    public PriceListResponseDTO updatePrice(UUID id, UpdatePriceListDTO dto) {
        PriceList price = priceListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Price not found"));

        if (dto.getServiceName() != null) price.setServiceName(dto.getServiceName());
        if (dto.getServiceCode() != null) price.setServiceCode(dto.getServiceCode());
        if (dto.getPrice() != null) price.setPrice(dto.getPrice());
        if (dto.getNotes() != null) price.setNotes(dto.getNotes());
        if (dto.getActive() != null) price.setActive(dto.getActive());
        if (dto.getServiceDetails() != null) price.setServiceDetails(dto.getServiceDetails());

        priceListRepository.save(price);
        return priceListMapper.toDto(price);
    }

    public void deletePrice(UUID id) {
        priceListRepository.deleteById(id);
    }

    public List<PriceListResponseDTO> getByType(ProviderType type) {
        return priceListRepository.findByProviderType(type)
                .stream()
                .map(priceListMapper::toDto)
                .collect(Collectors.toList());
    }
    public List<PriceListResponseDTO> getByProviderType(ProviderType type) {
        return priceListRepository.findByProviderType(type)
                .stream()
                .map(priceListMapper::toDto)
                .toList();
    }

}
