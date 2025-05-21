package org.vhv.virtualhumanvision.dto.request;

import jakarta.validation.constraints.NotNull;
import org.vhv.virtualhumanvision.enums.ObjectType;

public record AddObjectRequest(
        @NotNull String name,
        @NotNull ObjectType type) {
}
