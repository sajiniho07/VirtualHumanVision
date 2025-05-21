package org.vhv.virtualhumanvision.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddEmotionRequest(
        @NotNull Long personId,
        @NotNull String emotionType) {
}
