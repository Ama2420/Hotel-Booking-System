package com.rupeek.hotelbooking.application;

import com.rupeek.hotelbooking.domain.cancellation.CancellationPolicyRegistry;
import com.rupeek.hotelbooking.domain.cancellation.FlexibleCancellationPolicy;
import com.rupeek.hotelbooking.domain.common.ResourceNotFoundException;
import com.rupeek.hotelbooking.domain.property.Owner;
import com.rupeek.hotelbooking.domain.property.OwnerRepository;
import com.rupeek.hotelbooking.domain.property.Property;
import com.rupeek.hotelbooking.domain.property.PropertyRepository;
import com.rupeek.hotelbooking.domain.property.RoomType;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Registers owners and their properties. Independents and chains share this one path: a standalone hotel is an
 * owner that has added exactly one property.
 */
@Service
public class PropertyOnboardingService {

    private final OwnerRepository ownerRepository;
    private final PropertyRepository propertyRepository;
    private final InventoryService inventoryService;
    private final CancellationPolicyRegistry cancellationPolicies;

    public PropertyOnboardingService(OwnerRepository ownerRepository, PropertyRepository propertyRepository,
                                     InventoryService inventoryService,
                                     CancellationPolicyRegistry cancellationPolicies) {
        this.ownerRepository = ownerRepository;
        this.propertyRepository = propertyRepository;
        this.inventoryService = inventoryService;
        this.cancellationPolicies = cancellationPolicies;
    }

    public Owner registerOwner(String name, String email) {
        return ownerRepository.save(new Owner(Ids.newId("own"), name, email));
    }

    public Property addProperty(String ownerId, NewProperty command) {
        getOwner(ownerId);
        Property property = new Property(
                Ids.newId("prop"),
                ownerId,
                command.name(),
                command.location(),
                command.starRating(),
                command.amenities(),
                toRoomTypes(command.roomTypes()),
                resolveCancellationPolicy(command.cancellationPolicyCode()));
        // Inventory first, so a property never becomes searchable without inventory behind it.
        property.roomTypes().forEach(inventoryService::initialise);
        return propertyRepository.save(property);
    }

    public Owner getOwner(String ownerId) {
        return ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner", ownerId));
    }

    public Property getProperty(String propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));
    }

    public List<Property> getPropertiesOf(String ownerId) {
        getOwner(ownerId);
        return propertyRepository.findByOwnerId(ownerId);
    }

    private List<RoomType> toRoomTypes(List<NewRoomType> roomTypes) {
        return roomTypes.stream()
                .map(roomType -> new RoomType(Ids.newId("rt"), roomType.name(), roomType.maxOccupancy(),
                        roomType.totalUnits(), roomType.pricePerNight()))
                .toList();
    }

    private String resolveCancellationPolicy(String requestedCode) {
        String code = Optional.ofNullable(requestedCode).orElse(FlexibleCancellationPolicy.CODE);
        if (!cancellationPolicies.supports(code)) {
            throw new IllegalArgumentException("unsupported cancellation policy: " + code);
        }
        return code;
    }
}
