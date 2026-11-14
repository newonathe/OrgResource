package app.components;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import app.dto.ApiDtos.ResourceAvailabilityView;
import app.dto.ApiDtos.ResourceView;
import app.entities.AllocationStatus;
import app.entities.Resource;
import app.entities.ResourceType;
import app.repositories.AllocationRepository;
import app.repositories.ResourceRepository;

@Component
public class ResourceComponent {
    @Autowired private ResourceRepository resources;
    @Autowired private AllocationRepository allocations;
    @Autowired private Clock philippineClock;

    @Transactional
    public ResourceView create(String rawName, ResourceType type, Integer totalQuantity) {
        String name = NameNormalizer.display(rawName, "name");
        validateQuantity(totalQuantity, true);
        if (type == null) {
            throw new ApiException(400, "VALIDATION_FAILED", "type is required.");
        }
        String key = NameNormalizer.key(name);
        if (resources.existsByNameKey(key)) {
            throw new ApiException(409, "DUPLICATE_NAME", "A Resource with this name already exists.");
        }
        Resource saved = resources.save(new Resource(name, key, type, totalQuantity));
        return view(saved, 0L, 0L);
    }

    @Transactional(readOnly = true)
    public List<ResourceView> list(ResourceType type) {
        List<Resource> rows = type == null ? resources.findAll() : resources.findByType(type);
        return rows.stream().map(resource -> view(resource,
                effectiveHolds(resource.id), issuedCustody(resource.id))).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResourceAvailabilityView getAvailability(Long id) {
        Resource resource = resources.findById(id)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Resource not found."));
        ResourceView view = view(resource, effectiveHolds(id), issuedCustody(id));
        return new ResourceAvailabilityView(resource.id, view.availableQuantity);
    }

    private ResourceView view(Resource resource, Long holdsValue, Long issuedValue) {
        long holds = holdsValue == null ? 0L : holdsValue;
        long issued = issuedValue == null ? 0L : issuedValue;
        long available = resource.type == ResourceType.REUSABLE
                ? resource.totalQuantity.longValue() - holds - issued
                : resource.totalQuantity.longValue() - holds;
        if (available < 0 || available > 1000000) {
            throw new ApiException(500, "INTERNAL_ERROR", "Stored Resource capacity violates inventory invariants.");
        }
        return new ResourceView(resource, (int) available);
    }

    private Long effectiveHolds(Long resourceId) {
        return allocations.sumEffectiveHolds(resourceId, AllocationStatus.APPROVED,
                LocalDateTime.now(philippineClock));
    }

    private Long issuedCustody(Long resourceId) {
        return allocations.sumIssuedCustody(resourceId, AllocationStatus.ISSUED);
    }

    private void validateQuantity(Integer value, boolean allowZero) {
        int minimum = allowZero ? 0 : 1;
        if (value == null || value < minimum || value > 1000000) {
            throw new ApiException(400, "VALIDATION_FAILED", "totalQuantity must be from " + minimum + " to 1000000.");
        }
    }
}
