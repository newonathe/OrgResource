package edu.orgresource.extended.api;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public final class InventoryDtos {
    private InventoryDtos() { }

    public static class ReceiveRequest {
        @NotNull public UUID operationId;
        @NotNull @Min(1) @Max(1000000) public Integer quantity;
    }

    public static class AdjustRequest {
        @NotNull public UUID operationId;
        @NotNull @Min(0) @Max(1000000) public Integer newTotalQuantity;
    }

    public static class AllocationOperationRequest {
        @NotNull public UUID operationId;
    }

    public record OperationView(UUID operationId, String action, UUID resourceId, UUID allocationId,
                                String allocationStatus, Integer quantity, Integer totalQuantity,
                                Integer availableQuantity, Long version) { }

    public record ResourceState(UUID id, String name, String type, int totalQuantity,
                                int availableQuantity, long version) { }

    public record ResourcePage(List<ResourceState> items, UUID nextCursor) { }

    public record AllocationState(UUID id, UUID resourceId, String status,
                                  int quantity, long version) { }

    public record AllocationPage(List<AllocationState> items, UUID nextCursor) { }

    public record MovementView(UUID id, UUID operationId, UUID allocationId, String kind,
                               int quantity, long deltaTotal, long deltaIssued, String occurredAt) { }

    public record MovementPage(List<MovementView> items, UUID nextCursor) { }
}
