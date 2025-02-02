package org.stella;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.file.*;
import java.util.stream.Stream;

class MainTest {

    private static Stream<String> getFilesFromDirectory(String directoryPath) throws IOException {
        Path dir = Paths.get(directoryPath);
        if (!Files.exists(dir)) {
            throw new FileNotFoundException("Directory not found: " + directoryPath);
        }
        return Files.walk(dir)
                .filter(Files::isRegularFile)
                .map(Path::toString);
    }

    @ParameterizedTest(name = "{index} Typechecking well-typed program {0}")
    @MethodSource("wellTypedFiles")
    void testWellTyped(String filepath) throws Exception {
        String[] args = new String[0];
        final InputStream original = System.in;
        final FileInputStream fips = new FileInputStream(filepath);
        System.setIn(fips);
        Assertions.assertDoesNotThrow(() -> Main.main(args), "Test failed for file: " + filepath);
        System.setIn(original);
    }

    @ParameterizedTest(name = "{index} Typechecking ill-typed program {0}")
    @MethodSource("illTypedFiles")
    void testIllTyped(String filepath) throws Exception {
        String[] args = new String[0];
        final FileInputStream fips = new FileInputStream(filepath);
        System.setIn(fips);
        Exception exception = assertThrows(Exception.class, () -> Main.main(args), "Expected the type checker to fail for file: " + filepath);
        System.out.println("Type Error in file " + filepath + ": " + exception.getMessage());
    }

    static Stream<String> wellTypedFiles() throws IOException {
        return getFilesFromDirectory("tests/well-typed");
    }

    static Stream<String> illTypedFiles() throws IOException {
        return getFilesFromDirectory("tests/ill-typed");
    }
}
