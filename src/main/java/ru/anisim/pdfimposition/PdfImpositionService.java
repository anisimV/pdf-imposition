package ru.anisim.pdfimposition;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class PdfImpositionService {

    // Координаты из образца, в PDF-пунктах.
    // Начало координат — снизу слева.
    private final ImpositionLayout layout;
    private final float inputBleedMm;

    public PdfImpositionService(ImpositionLayout layout) {
        this(layout, 0f);
    }

    public PdfImpositionService(
            ImpositionLayout layout,
            float inputBleedMm
    ) {
        this.layout = Objects.requireNonNull(
                layout,
                "Настройки спуска обязательны"
        );

        if (!Float.isFinite(inputBleedMm) || inputBleedMm < 0) {
            throw new IllegalArgumentException(
                    "Вылеты должны быть конечным неотрицательным числом"
            );
        }

        this.inputBleedMm = inputBleedMm;
    }

    public String getLayoutName() {
        return layout.name();
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
                    var trimBox = resolveTrimBox(
                            sourcePage.getTrimBox(),
                            sourcePage.getMediaBox(),
                            pageIndex
                    );

                    if (sourcePage.getRotation() != 0) {
                        throw new IOException(
                                "Страница " + (pageIndex + 1)
                                        + ": сначала уберите поворот страницы"
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

    private boolean matchesSize(
            PDRectangle box,
            float width,
            float height
    ) {
        return Math.abs(box.getWidth() - width) <= 0.6f
                && Math.abs(box.getHeight() - height) <= 0.6f;
    }

    private PDRectangle resolveTrimBox(
            PDRectangle trimBox,
            PDRectangle mediaBox,
            int pageIndex
    ) throws IOException {
        var width = millimetersToPoints(layout.artworkWidthMm());
        var height = millimetersToPoints(layout.artworkHeightMm());

        // Если граница реза уже правильная, используем её.
        if (matchesSize(trimBox, width, height)) {
            return trimBox;
        }

        var bleed = millimetersToPoints(inputBleedMm);

        // Восстанавливаем границу реза только при явно заданных
        // вылетах и совпадении размеров исходной страницы.
        if (inputBleedMm > 0
                && matchesSize(mediaBox, width + 2 * bleed, height + 2 * bleed)
                && matchesSize(
                trimBox,
                mediaBox.getWidth(),
                mediaBox.getHeight()
        )
                && Math.abs(trimBox.getLowerLeftX()
                - mediaBox.getLowerLeftX()) <= 0.6f
                && Math.abs(trimBox.getLowerLeftY()
                - mediaBox.getLowerLeftY()) <= 0.6f) {

            return new PDRectangle(
                    mediaBox.getLowerLeftX()
                            + (mediaBox.getWidth() - width) / 2,
                    mediaBox.getLowerLeftY()
                            + (mediaBox.getHeight() - height) / 2,
                    width,
                    height
            );
        }

        throw new IOException(
                "Страница " + (pageIndex + 1)
                        + ": TrimBox должен быть "
                        + layout.artworkWidthMm() + " × "
                        + layout.artworkHeightMm() + " мм"
                        + ". Проверьте размер страницы и вылеты."
        );
    }
}
