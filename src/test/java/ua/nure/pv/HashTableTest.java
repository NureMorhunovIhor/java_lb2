package ua.nure.pv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HashTableTest {

    private static final String ASSERT_FAILED_MESSAGE_PREFIX = "Failed at ";
    
    private static final String FILL_SIMPLE = "fillSimple";

	private static final String FILL_AND_REMOVE = "fillAndRemove";

	private static final String FILL_AND_REFILL = "fillAndRefill";

	private static final String KEYS = "keys.txt";

	private static final String FILL = "fill.txt";

    @ParameterizedTest
    @MethodSource(FILL_SIMPLE)
    void testSimpleFilling(String testCaseName, int[] elements, String expectedKeys) throws Exception {
        HashTable hashtable = HashTable.getInstance();
        for (int element : elements) {
            hashtable.insert(element, element);
        }
        assertEquals(expectedKeys, Arrays.toString(hashtable.keys()), () -> ASSERT_FAILED_MESSAGE_PREFIX + testCaseName);
    }

    @ParameterizedTest
    @MethodSource(FILL_AND_REMOVE)
    void testFillAndRemove(String testCaseName, int[] elements, List<String> expectedKeysList) throws IOException {
        HashTable hashtable = HashTable.getInstance();
        for (int element : elements) {
            hashtable.insert(element, element);
        }
        List<String> actualKeysList = new ArrayList<>();
        actualKeysList.add(Arrays.toString(hashtable.keys()));
        for (int element : elements) {
            hashtable.remove(element);
            actualKeysList.add(Arrays.toString(hashtable.keys()));
        }
        assertEquals(
                String.join("\n", expectedKeysList),
                String.join("\n", actualKeysList),
                () -> ASSERT_FAILED_MESSAGE_PREFIX + testCaseName);
    }

    @ParameterizedTest
    @MethodSource(FILL_AND_REFILL)
    void testFillAndRefill(String testCaseName, int[] elements, List<String> expectedKeysList) throws IOException {
        HashTable hashtable = HashTable.getInstance();
        Random prng = new Random(Integer.parseInt(testCaseName) * elements[0] + elements[0]);
        List<Integer> elementsInCertainOrder = Arrays.stream(elements).boxed().collect(Collectors.toList());
        List<String> actualKeysList = new ArrayList<>();

        Collections.shuffle(elementsInCertainOrder, prng);
        for (int element : elementsInCertainOrder) {
            hashtable.insert(element, element);
            actualKeysList.add(Arrays.toString(hashtable.keys()));
        }

        Collections.shuffle(elementsInCertainOrder, prng);
        for (int element : elementsInCertainOrder) {
            hashtable.remove(element);
            actualKeysList.add(Arrays.toString(hashtable.keys()));
        }

        Collections.shuffle(elementsInCertainOrder, prng);
        for (int element : elementsInCertainOrder) {
            hashtable.insert(element, element);
            actualKeysList.add(Arrays.toString(hashtable.keys()));
        }

        assertEquals(
                String.join("\n", expectedKeysList),
                String.join("\n", actualKeysList),
                () -> ASSERT_FAILED_MESSAGE_PREFIX + testCaseName);
    }

    @Test
    public void testSearch(){
        HashTable hashtable = HashTable.getInstance();

        hashtable.insert(10,10);
        hashtable.insert(18,18);
        hashtable.insert(34,34);

        assertEquals(10, hashtable.search(10));
        assertEquals(18, hashtable.search(18));
        assertEquals(34, hashtable.search(34));

        hashtable.remove(18);

        assertEquals(10, hashtable.search(10));
        assertEquals(null, hashtable.search(18));
        assertEquals(34, hashtable.search(34));

        hashtable.remove(10);

        assertEquals(null, hashtable.search(10));
        assertEquals(null, hashtable.search(18));
        assertEquals(34, hashtable.search(34));

        hashtable.insert(10,10);
        hashtable.insert(18,18);
        hashtable.insert(34,34);
        hashtable.insert(42,42);
        hashtable.insert(50,50);
        hashtable.insert(58,58);
        hashtable.insert(66,66);
        hashtable.insert(74,74);

        hashtable.remove(2);
        assertEquals(10, hashtable.search(10));

        hashtable.remove(10);

        assertEquals(null, hashtable.search(10));
        assertEquals(18, hashtable.search(18));
        assertEquals(34, hashtable.search(34));
        assertEquals(42, hashtable.search(42));
        assertEquals(50, hashtable.search(50));
        assertEquals(58, hashtable.search(58));
        assertEquals(66, hashtable.search(66));
        assertEquals(74, hashtable.search(74));

        hashtable.remove(50);

        assertEquals(null, hashtable.search(10));
        assertEquals(18, hashtable.search(18));
        assertEquals(34, hashtable.search(34));
        assertEquals(42, hashtable.search(42));
        assertEquals(null, hashtable.search(50));
        assertEquals(58, hashtable.search(58));
        assertEquals(66, hashtable.search(66));
        assertEquals(74, hashtable.search(74));
    }

    @Test
    public void testOverflow(){
        HashTable hashtable = HashTable.getInstance();
        for (int i = 0; i < 32; i+=2) {
            hashtable.insert(i, i);
        }
        assertThrows(IllegalStateException.class, () -> hashtable.insert(42, 42));
    }

    static Stream<Arguments> fillSimple() throws IOException {
        return readTestCases(FILL_SIMPLE, HashTableTest::readKeys);
    }

    static Stream<Arguments> fillAndRemove() throws IOException {
        return readTestCases(FILL_AND_REMOVE, HashTableTest::readKeysSeries);
    }

    static Stream<Arguments> fillAndRefill() throws IOException {
        return readTestCases(FILL_AND_REFILL, HashTableTest::readKeysSeries);
    }
    
    private static Stream<Arguments> readTestCases(String s, Function<Path, Object> keysExtractor) throws IOException {
        Path testCaseRoot = Paths.get("src", "test", "resources", s);
        return Files.walk(testCaseRoot, 1)
                .filter(Files::isDirectory)
                .filter(path -> !testCaseRoot.equals(path))
                .map(testCase -> arguments(
                        testCase.getFileName().toString(),
                        readElements(testCase),
                        keysExtractor.apply(testCase)
                ));
    }

    private static int[] readElements(Path testCase) {
        try (Stream<String> lines = Files.lines(testCase.resolve(FILL))) {
            return lines.mapToInt(Integer::valueOf).toArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String readKeys(Path testCase) {
        try {
            return Files.readString(testCase.resolve(KEYS));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<String> readKeysSeries(Path testCase) {
        try {
            return Files.readAllLines(testCase.resolve(KEYS));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
