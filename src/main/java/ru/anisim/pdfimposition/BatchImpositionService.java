package ru.anisim.pdfimposition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BatchImpositionService {

    private final PdfImpositionService pdfService;

    public BatchImpositionService(PdfImpositionService pdfService) {
        this.pdfService = pdfService;
    }

    public List<String> process(List<Path> inputPaths) {
        List<String> results = new ArrayList<>();

        for (var inputPath : inputPaths) {
            try {
                var source = inputPath.toAbsolutePath().normalize();
                var outputDirectory = source.getParent().resolve("спуски");

                Files.createDirectories(outputDirectory);

                var outputPath = findAvailablePath(outputDirectory, source);

                pdfService.impose(source, outputPath);

                results.add("Создан: " + outputPath.getFileName());
            } catch (IOException e) {
                results.add("Ошибка: " + inputPath.getFileName() + " — " + e.getMessage());
            }
        }

        return results;
    }

    private Path findAvailablePath(Path directory, Path inputPath) {
        var fileName = inputPath.getFileName().toString();
        var dotIndex = fileName.lastIndexOf('.');

        var baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;

        var outputPath = directory.resolve(baseName + "_А5_SRA3.pdf");
        var number = 1;

        while (Files.exists(outputPath)) {
            outputPath = directory.resolve(baseName + "_А5_SRA3_" + number + ".pdf");
            number++;
        }

        return outputPath;
    }
}