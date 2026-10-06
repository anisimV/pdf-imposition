package ru.anisim.pdfimposition;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.util.Matrix;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class PdfImpositionService {

    // Координаты из образца, в PDF-пунктах.
    // Начало координат — снизу слева.
    private final ImpositionLayout layout;

    public PdfImpositionService(ImpositionLayout layout) {
        this.layout = Objects.requireNonNull(
                layout,
                "Настройки спуска обязательны"
        );
    }

    public void impose(Path inputPath, Path outputPath) throws IOException {
        try (var templateStream = PdfImpositionService.class
                .getResourceAsStream(layout.templateResource())) {

            if (templateStream == null) {
                throw new IOException(
                        "Не найден шаблон: " + layout.templateResource()
                );
            }

            try (var source = Loader.loadPDF(inputPath.toFile());
                 var template = Loader.loadPDF(templateStream.readAllBytes());
                 var output = new PDDocument()) {

                var layerUtility = new LayerUtility(output);

                for (var pageIndex = 0;
                     pageIndex < source.getNumberOfPages();
                     pageIndex++) {

                    var sourcePage = source.getPage(pageIndex);
                    var trimBox = sourcePage.getTrimBox();

                    if (sourcePage.getRotation() != 0) {
                        throw new IOException(
                                "Страница " + (pageIndex + 1)
                                        + ": сначала уберите поворот страницы"
                        );
                    }

                    if (Math.abs(trimBox.getWidth()
                            - millimetersToPoints(layout.artworkWidthMm())) > 0.6f
                            || Math.abs(trimBox.getHeight()
                            - millimetersToPoints(layout.artworkHeightMm())) > 0.6f) {

                        throw new IOException(
                                "Страница " + (pageIndex + 1)
                                        + ": граница реза TrimBox должна быть "
                                        + layout.artworkWidthMm() + " × "
                                        + layout.artworkHeightMm() + " мм"
                        );
                    }

                    var sheet = output.importPage(template.getPage(0));
                    sheet.getCOSObject().removeItem(COSName.getPDFName("PieceInfo"));

                    var artwork = layerUtility.importPageAsForm(
                            source, pageIndex
                    );

                    // Используем исходные координаты и сохраняем вылеты.
                    artwork.setMatrix(new AffineTransform());
                    artwork.setBBox(sourcePage.getMediaBox());

                    try (var content = new PDPageContentStream(
                            output,
                            sheet,
                            PDPageContentStream.AppendMode.APPEND,
                            true,
                            true
                    )) {
                        for (var placement : layout.placements()) {
                            drawArtwork(
                                    content,
                                    artwork,
                                    placement,
                                    trimBox.getLowerLeftX(),
                                    trimBox.getLowerLeftY()
                            );
                        }
                    }
                }

                output.save(outputPath.toFile());
            }
        }
    }

    private void drawArtwork(
            PDPageContentStream content,
            PDFormXObject artwork,
            ArtworkPlacement placement,
            float trimX,
            float trimY
    ) throws IOException {

        content.saveGraphicsState();

        // Перемещаем начало координат в позицию изделия.
        content.transform(
                Matrix.getTranslateInstance(
                        placement.x(), placement.y()
                )
        );

        // Ограничиваем вылеты, чтобы соседние макеты
        // не перекрывали друг друга.
        content.addRect(
                placement.clipX(),
                placement.clipY(),
                placement.clipWidth(),
                placement.clipHeight()
        );
        content.clip();

        // Совмещаем угол TrimBox с выбранной позицией.
        content.transform(
                Matrix.getTranslateInstance(-trimX, -trimY)
        );

        content.drawForm(artwork);

        content.restoreGraphicsState();
    }

    private float millimetersToPoints(float millimeters) {
        return millimeters * 72 / 25.4f;
    }
}
