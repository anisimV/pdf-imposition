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

    private ImpositionLayouts() {
    }
}
