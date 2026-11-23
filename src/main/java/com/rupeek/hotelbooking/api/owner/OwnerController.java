package com.rupeek.hotelbooking.api.owner;

import com.rupeek.hotelbooking.api.property.PropertyResponse;
import com.rupeek.hotelbooking.application.PropertyOnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/owners")
@Tag(name = "Onboarding")
public class OwnerController {

    private final PropertyOnboardingService onboardingService;

    public OwnerController(PropertyOnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register an owner account (an independent hotel or a chain)")
    public OwnerResponse register(@Valid @RequestBody RegisterOwnerRequest request) {
        return OwnerResponse.from(onboardingService.registerOwner(request.name(), request.email()));
    }

    @GetMapping("/{ownerId}")
    @Operation(summary = "Get an owner")
    public OwnerResponse get(@PathVariable String ownerId) {
        return OwnerResponse.from(onboardingService.getOwner(ownerId));
    }

    @PostMapping("/{ownerId}/properties")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Onboard a property under an owner")
    public PropertyResponse addProperty(@PathVariable String ownerId, @Valid @RequestBody AddPropertyRequest request) {
        return PropertyResponse.from(onboardingService.addProperty(ownerId, request.toCommand()));
    }

    @GetMapping("/{ownerId}/properties")
    @Operation(summary = "List an owner's properties")
    public List<PropertyResponse> listProperties(@PathVariable String ownerId) {
        return onboardingService.getPropertiesOf(ownerId).stream().map(PropertyResponse::from).toList();
    }
}
