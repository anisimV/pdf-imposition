package ru.anisim.pdfimposition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import java.util.function.IntConsumer;

public class BatchImpositionService {

    private final PdfImpositionService pdfService;

    public BatchImpositionService(PdfImpositionService pdfService) {
        this.pdfService = pdfService;
    }

    public List<String> process(List<Path> inputPaths, Path destinationDirectory) {
        return process(inputPaths, destinationDirectory, completed -> {
        });
    }

    public List<String> process(List<Path> inputPaths, Path destinationDirectory, IntConsumer onProgress) {
        List<String> results = new ArrayList<>();
        var completed = 0;

        var outputDirectory = destinationDirectory.toAbsolutePath().normalize().resolve("спуски");

        for (var inputPath : inputPaths) {
            try {
                var source = inputPath.toAbsolutePath().normalize();

                Files.createDirectories(outputDirectory);

                var outputPath = findAvailablePath(outputDirectory, source);

                pdfService.impose(source, outputPath);

                results.add("Создан: " + outputPath.getFileName());
            } catch (IOException e) {
                results.add("Ошибка: " + inputPath.getFileName() + " — " + e.getMessage());
            }

            completed++;
            onProgress.accept(completed);
        }

        return results;
    }

    private Path findAvailablePath(Path directory, Path inputPath) {
        var fileName = inputPath.getFileName().toString();
        var dotIndex = fileName.lastIndexOf('.');

        var baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;

        var outputBaseName = baseName + "_" + pdfService.getLayoutName() + "_SRA3";

        var outputPath = directory.resolve(outputBaseName + ".pdf");
        var number = 1;

        while (Files.exists(outputPath)) {
            outputPath = directory.resolve(outputBaseName + "_" + number + ".pdf");
            number++;
        }

        return outputPath;
    }
}
