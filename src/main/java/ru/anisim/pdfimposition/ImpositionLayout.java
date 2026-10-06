package ru.anisim.pdfimposition;

import java.util.List;
import java.util.Objects;

public record ImpositionLayout(String name, String templateResource, float artworkWidthMm, float artworkHeightMm,
                               List<ArtworkPlacement> placements) {
    public ImpositionLayout {
        Objects.requireNonNull(name, "Название обязательно");
        Objects.requireNonNull(templateResource, "Шаблон обязателен");
        Objects.requireNonNull(placements, "Позиции обязательны");

        if (!Float.isFinite(artworkWidthMm) || !Float.isFinite(artworkHeightMm) || artworkWidthMm <= 0 || artworkHeightMm <= 0) {
            throw new IllegalArgumentException("Размеры макета должны быть положительными конечными числами");
        }

        if (placements.isEmpty()) {
            throw new IllegalArgumentException("Нужна хотя бы одна позиция макета");
        }

        placements = List.copyOf(placements);
    }
}

