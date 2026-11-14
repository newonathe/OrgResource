package app.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import app.dto.ApiDtos.MessageView;
import app.dto.ApiDtos.ResourceView;
import app.entities.Resource;
import app.entities.ResourceType;
import app.repositories.AllocationRepository;
import app.repositories.ResourceRepository;
import app.repositories.ResourceRequirementRepository;

public class ResourceComponentTest {
    private ResourceComponent component;
    private ResourceRepository resources;
    private ResourceRequirementRepository requirements;
    private AllocationRepository allocations;
    private Resource existing;

    @Before
    public void setUp() {
        component = new ResourceComponent();
        resources = org.mockito.Mockito.mock(ResourceRepository.class);
        requirements = org.mockito.Mockito.mock(ResourceRequirementRepository.class);
        allocations = org.mockito.Mockito.mock(AllocationRepository.class);
        ReflectionTestUtils.setField(component, "resources", resources);
        ReflectionTestUtils.setField(component, "requirements", requirements);
        ReflectionTestUtils.setField(component, "allocations", allocations);
        ReflectionTestUtils.setField(component, "philippineClock",
                Clock.fixed(Instant.parse("2026-11-15T00:00:00Z"), ZoneId.of("Asia/Manila")));
        existing = new Resource("Old paper", "old paper", ResourceType.REUSABLE, 10);
        existing.id = 23L;
    }

    @Test
    public void updateNormalizesNameKeepsTypeAndReturnsServerAvailability() {
        when(resources.lockById(23L)).thenReturn(Optional.of(existing));
        when(resources.existsByNameKeyAndIdNot("a4 paper sheets", 23L)).thenReturn(false);
        when(allocations.sumEffectiveHolds(23L, app.entities.AllocationStatus.APPROVED,
                java.time.LocalDateTime.now(Clock.fixed(Instant.parse("2026-11-15T00:00:00Z"),
                        ZoneId.of("Asia/Manila"))))).thenReturn(2L);
        when(allocations.sumIssuedCustody(23L, app.entities.AllocationStatus.ISSUED)).thenReturn(3L);
        when(resources.saveAndFlush(existing)).thenReturn(existing);

        ResourceView result = component.update(23L, "  A4   paper sheets ", 12, 10);

        assertEquals("A4 paper sheets", result.name);
        assertEquals("a4 paper sheets", existing.nameKey);
        assertEquals(Integer.valueOf(12), result.totalQuantity);
        assertEquals(Integer.valueOf(7), result.availableQuantity);
        assertEquals(ResourceType.REUSABLE, result.type);
        verify(resources).saveAndFlush(existing);
    }

    @Test
    public void staleExpectedQuantityDoesNotChangeResource() {
        when(resources.lockById(23L)).thenReturn(Optional.of(existing));

        expectApiError(409, "STALE_RESOURCE_QUANTITY", () ->
                component.update(23L, "New paper", 12, 9));

        assertEquals("Old paper", existing.name);
        assertEquals(Integer.valueOf(10), existing.totalQuantity);
        verify(resources, never()).saveAndFlush(existing);
    }

    @Test
    public void normalizedDuplicateNameDoesNotChangeResource() {
        when(resources.lockById(23L)).thenReturn(Optional.of(existing));
        when(resources.existsByNameKeyAndIdNot("other paper", 23L)).thenReturn(true);

        expectApiError(409, "DUPLICATE_NAME", () ->
                component.update(23L, " Other   paper ", 10, 10));

        assertEquals("Old paper", existing.name);
        assertEquals("old paper", existing.nameKey);
        verify(resources, never()).saveAndFlush(existing);
    }

    @Test
    public void invalidQuantityIsRejectedBeforeDatabaseAccess() {
        expectApiError(400, "VALIDATION_FAILED", () ->
                component.update(23L, "Paper", 1000001, 10));
        verify(resources, never()).lockById(23L);
    }

    @Test
    public void reusableTotalCannotDropBelowHoldsPlusIssuedCustody() {
        when(resources.lockById(23L)).thenReturn(Optional.of(existing));
        when(resources.existsByNameKeyAndIdNot("old paper", 23L)).thenReturn(false);
        when(allocations.sumEffectiveHolds(23L, app.entities.AllocationStatus.APPROVED,
                java.time.LocalDateTime.now(Clock.fixed(Instant.parse("2026-11-15T00:00:00Z"),
                        ZoneId.of("Asia/Manila"))))).thenReturn(3L);
        when(allocations.sumIssuedCustody(23L, app.entities.AllocationStatus.ISSUED)).thenReturn(4L);

        expectApiError(409, "QUANTITY_BELOW_COMMITMENTS", () ->
                component.update(23L, "Old paper", 6, 10));

        assertEquals(Integer.valueOf(10), existing.totalQuantity);
        verify(resources, never()).saveAndFlush(existing);
    }

    @Test
    public void consumableQuantityUsesEffectiveApprovedHoldsButNotReusableCustody() {
        existing.type = ResourceType.CONSUMABLE;
        when(resources.lockById(23L)).thenReturn(Optional.of(existing));
        when(resources.existsByNameKeyAndIdNot("paper", 23L)).thenReturn(false);
        when(allocations.sumEffectiveHolds(23L, app.entities.AllocationStatus.APPROVED,
                java.time.LocalDateTime.now(Clock.fixed(Instant.parse("2026-11-15T00:00:00Z"),
                        ZoneId.of("Asia/Manila"))))).thenReturn(2L);
        when(allocations.sumIssuedCustody(23L, app.entities.AllocationStatus.ISSUED)).thenReturn(5L);
        when(resources.saveAndFlush(existing)).thenReturn(existing);

        ResourceView result = component.update(23L, "Paper", 4, 10);

        assertEquals(ResourceType.CONSUMABLE, result.type);
        assertEquals(Integer.valueOf(2), result.availableQuantity);
    }

    @Test
    public void deleteRefusesAnyRequirementBeforeCheckingStock() {
        existing.totalQuantity = 0;
        when(resources.lockById(23L)).thenReturn(Optional.of(existing));
        when(requirements.existsByResource_Id(23L)).thenReturn(true);

        expectApiError(409, "DEPENDENCY_EXISTS", () -> component.delete(23L));

        verify(resources, never()).delete(existing);
    }

    @Test
    public void deleteRefusesPositiveStock() {
        when(resources.lockById(23L)).thenReturn(Optional.of(existing));
        when(requirements.existsByResource_Id(23L)).thenReturn(false);

        expectApiError(409, "RESOURCE_NOT_EMPTY", () -> component.delete(23L));

        verify(resources, never()).delete(existing);
    }

    @Test
    public void deletePhysicallyRemovesZeroStockUnreferencedResource() {
        existing.totalQuantity = 0;
        when(resources.lockById(23L)).thenReturn(Optional.of(existing));
        when(requirements.existsByResource_Id(23L)).thenReturn(false);

        MessageView result = component.delete(23L);

        assertEquals("Record deleted.", result.message);
        verify(resources).delete(existing);
        verify(resources).flush();
    }

    @Test
    public void missingResourceIsNotFoundAndNonpositiveIdIsInvalid() {
        when(resources.lockById(404L)).thenReturn(Optional.<Resource>empty());
        expectApiError(404, "NOT_FOUND", () -> component.delete(404L));
        expectApiError(400, "VALIDATION_FAILED", () -> component.delete(0L));
    }

    private void expectApiError(int status, String code, Runnable action) {
        try {
            action.run();
            fail("Expected API error " + code);
        } catch (ApiException error) {
            assertEquals(status, error.status);
            assertEquals(code, error.code);
        }
    }
}
