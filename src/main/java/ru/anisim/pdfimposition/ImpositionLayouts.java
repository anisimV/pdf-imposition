package ru.anisim.pdfimposition;

import java.util.List;
import java.util.ArrayList;

public final class ImpositionLayouts {

    public static final ImpositionLayout A5 = new ImpositionLayout(
            "А5",
            "/a5-template.pdf",
            148f,
            210f,
            List.of(
                    new ArtworkPlacement(
                            28.34f, 643.46f,
                            -14.17f, -5.67f, 439.37f, 615.12f
                    ),
                    new ArtworkPlacement(
                            459.21f, 643.46f,
                            -5.67f, -5.67f, 439.37f, 615.12f
                    ),
                    new ArtworkPlacement(
                            28.34f, 36.85f,
                            -14.17f, -14.17f, 439.37f, 615.12f
                    ),
                    new ArtworkPlacement(
                            459.21f, 36.85f,
                            -5.67f, -14.17f, 439.37f, 615.12f
                    )
            )
    );

    public static final ImpositionLayout A6 = new ImpositionLayout(
            "А6",
            "/a6-template.pdf",
            105f,
            148f,
            List.of(
                    new ArtworkPlacement(
                            25.51f, 459.21f,
                            -14.17f, -5.67f, 317.48f, 439.37f
                    ),
                    new ArtworkPlacement(
                            25.51f, 28.35f,
                            -14.17f, -14.17f, 317.48f, 439.37f
                    ),
                    new ArtworkPlacement(
                            334.49f, 459.21f,
                            -5.67f, -5.67f, 308.98f, 439.37f
                    ),
                    new ArtworkPlacement(
                            334.49f, 28.35f,
                            -5.67f, -14.17f, 308.98f, 439.37f
                    ),
                    new ArtworkPlacement(
                            643.46f, 459.21f,
                            -5.67f, -5.67f, 308.98f, 439.37f
                    ),
                    new ArtworkPlacement(
                            643.46f, 28.35f,
                            -5.67f, -14.17f, 308.98f, 439.37f
                    ),
                    new ArtworkPlacement(
                            952.44f, 459.21f,
                            -5.67f, -5.67f, 317.48f, 439.37f
                    ),
                    new ArtworkPlacement(
                            952.44f, 28.35f,
                            -5.67f, -14.17f, 317.48f, 439.37f
                    )
            )
    );

    public static final ImpositionLayout A7 = new ImpositionLayout(
            "А7",
            "/a7-template.pdf",
            70f,
            100f,
            createA7Placements()
    );

    private static List<ArtworkPlacement> createA7Placements() {
        var placements = new ArrayList<ArtworkPlacement>();

        // Позиции начала исходной страницы из готового спуска.
        var sourceX = new float[]{
                19.84f, 223.94f, 428.03f,
                632.13f, 836.22f, 1040.31f
        };

        var sourceY = new float[]{
                592.44f, 303.31f, 14.17f
        };

        // Вылеты 3 мм, выраженные в PDF-пунктах.
        var bleed = 3f * 72f / 25.4f;

        for (var column = 0; column < sourceX.length; column++) {
            for (var row = 0; row < sourceY.length; row++) {
                var clipX = column == 0 ? 0f : 5.67f;
                var clipY = row == 2 ? 0f : 5.67f;

                var clipWidth = column == 0 || column == 5
                        ? 209.76f
                        : 204.09f;

                var clipHeight = row == 1
                        ? 289.13f
                        : 294.80f;

                placements.add(new ArtworkPlacement(
                        sourceX[column] + bleed,
                        sourceY[row] + bleed,
                        clipX - bleed,
                        clipY - bleed,
                        clipWidth,
                        clipHeight
                ));
            }
        }

        return List.copyOf(placements);
    }


    private ImpositionLayouts() {
    }
}
