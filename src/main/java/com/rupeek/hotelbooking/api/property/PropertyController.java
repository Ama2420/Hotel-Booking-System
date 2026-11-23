package com.rupeek.hotelbooking.api.property;

import com.rupeek.hotelbooking.application.PropertyOnboardingService;
import com.rupeek.hotelbooking.application.PropertySearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/properties")
@Tag(name = "Discovery")
public class PropertyController {

    private final PropertySearchService searchService;
    private final PropertyOnboardingService onboardingService;

    public PropertyController(PropertySearchService searchService, PropertyOnboardingService onboardingService) {
        this.searchService = searchService;
        this.onboardingService = onboardingService;
    }

    @GetMapping("/search")
    @Operation(summary = "Find properties with rooms available for the stay, cheapest first")
    public List<PropertySearchResponse> search(@Valid @ParameterObject SearchPropertiesRequest request) {
        return searchService.search(request.toQuery()).stream().map(PropertySearchResponse::from).toList();
    }

    @GetMapping("/{propertyId}")
    @Operation(summary = "Get a property")
    public PropertyResponse get(@PathVariable String propertyId) {
        return PropertyResponse.from(onboardingService.getProperty(propertyId));
    }
}
