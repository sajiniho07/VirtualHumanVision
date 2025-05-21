package org.vhv.virtualhumanvision.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddDetectionRequest(@NotNull Double confidence,
                                  Long personId,
                                  Long objectId,
                                  Long textId) {
}
