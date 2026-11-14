package app.components;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import app.dto.ApiDtos.ResourceAvailabilityView;
import app.dto.ApiDtos.MessageView;
import app.dto.ApiDtos.ResourceView;
import app.entities.AllocationStatus;
import app.entities.Resource;
import app.entities.ResourceType;
import app.repositories.AllocationRepository;
import app.repositories.ResourceRepository;
import app.repositories.ResourceRequirementRepository;

@Component
public class ResourceComponent {
    @Autowired private ResourceRepository resources;
    @Autowired private ResourceRequirementRepository requirements;
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

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResourceView update(Long id, String rawName, Integer totalQuantity, Integer expectedTotalQuantity) {
        requirePositiveId(id);
        validateQuantity(totalQuantity, true);
        if (expectedTotalQuantity == null || expectedTotalQuantity < 0 || expectedTotalQuantity > 1000000) {
            throw new ApiException(400, "VALIDATION_FAILED", "expectedTotalQuantity must be from 0 to 1000000.");
        }
        Resource resource = resources.lockById(id)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Resource not found."));
        if (!resource.totalQuantity.equals(expectedTotalQuantity)) {
            throw new ApiException(409, "STALE_RESOURCE_QUANTITY",
                    "Resource totalQuantity changed; reload it before updating.");
        }

        String name = NameNormalizer.display(rawName, "name");
        String nameKey = NameNormalizer.key(name);
        if (resources.existsByNameKeyAndIdNot(nameKey, id)) {
            throw new ApiException(409, "DUPLICATE_NAME", "A Resource with this name already exists.");
        }

        LocalDateTime now = LocalDateTime.now(philippineClock);
        Long holdsValue = allocations.sumEffectiveHolds(id, AllocationStatus.APPROVED, now);
        Long issuedValue = allocations.sumIssuedCustody(id, AllocationStatus.ISSUED);
        long holds = holdsValue == null ? 0L : holdsValue;
        long issued = issuedValue == null ? 0L : issuedValue;
        long committed = resource.type == ResourceType.REUSABLE ? holds + issued : holds;
        if (totalQuantity.longValue() < committed) {
            throw new ApiException(409, "QUANTITY_BELOW_COMMITMENTS",
                    "totalQuantity cannot be lower than stock currently reserved or in custody.");
        }

        resource.name = name;
        resource.nameKey = nameKey;
        resource.totalQuantity = totalQuantity;
        Resource saved = resources.saveAndFlush(resource);
        return view(saved, holds, issued);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MessageView delete(Long id) {
        requirePositiveId(id);
        Resource resource = resources.lockById(id)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Resource not found."));
        if (requirements.existsByResource_Id(id)) {
            throw new ApiException(409, "DEPENDENCY_EXISTS", "Resource still has ResourceRequirements.");
        }
        if (resource.totalQuantity > 0) {
            throw new ApiException(409, "RESOURCE_NOT_EMPTY", "Resource totalQuantity must be zero before deletion.");
        }
        resources.delete(resource);
        resources.flush();
        return new MessageView("Record deleted.");
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

    private void requirePositiveId(Long id) {
        if (id == null || id <= 0) {
            throw new ApiException(400, "VALIDATION_FAILED", "Resource ID must be positive.");
        }
    }
}
