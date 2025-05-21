package org.vhv.virtualhumanvision.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddTextRequest(@NotNull String content) {
}
