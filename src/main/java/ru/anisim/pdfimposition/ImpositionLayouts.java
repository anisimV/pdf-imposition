package ru.anisim.pdfimposition;

import java.util.List;

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


    private ImpositionLayouts() {
    }
}
