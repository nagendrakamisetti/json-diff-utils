import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Iterator;
import java.util.Map;

public class JsonDiffUtil {

    /**
     * Recursively compares two JsonNode trees and prints all differences.
     *
     * @param expected the expected JSON
     * @param actual the actual JSON
     * @param path current JSON path (root = "")
     */
    public static void printDifferences(JsonNode expected, JsonNode actual, String path) {
        if (expected == null && actual == null) return;

        // Case: missing expected or actual node
        if (expected == null) {
            System.out.printf("⚠️  Missing expected node at '%s', but actual has value: %s%n", path, actual);
            return;
        }
        if (actual == null) {
            System.out.printf("⚠️  Missing actual node at '%s', expected value: %s%n", path, expected);
            return;
        }

        // Case: both are objects
        if (expected.isObject() && actual.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = expected.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldName = entry.getKey();
                String currentPath = path.isEmpty() ? fieldName : path + "." + fieldName;
                JsonNode expectedValue = entry.getValue();
                JsonNode actualValue = actual.get(fieldName);

                if (actualValue == null) {
                    System.out.printf("❌ Missing field in actual: %s (expected: %s)%n", currentPath, expectedValue);
                } else {
                    printDifferences(expectedValue, actualValue, currentPath);
                }
            }

            // Check for extra fields in actual JSON
            Iterator<String> actualFields = actual.fieldNames();
            while (actualFields.hasNext()) {
                String field = actualFields.next();
                if (!expected.has(field)) {
                    String currentPath = path.isEmpty() ? field : path + "." + field;
                    System.out.printf("⚠️  Extra field in actual: %s (value: %s)%n", currentPath, actual.get(field));
                }
            }
            return;
        }

        // Case: both are arrays
        if (expected.isArray() && actual.isArray()) {
            ArrayNode expArray = (ArrayNode) expected;
            ArrayNode actArray = (ArrayNode) actual;

            int minSize = Math.min(expArray.size(), actArray.size());
            for (int i = 0; i < minSize; i++) {
                printDifferences(expArray.get(i), actArray.get(i), path + "[" + i + "]");
            }

            if (expArray.size() > actArray.size()) {
                System.out.printf("❌ Array '%s' has %d extra expected elements%n", path, expArray.size() - actArray.size());
            } else if (actArray.size() > expArray.size()) {
                System.out.printf("⚠️  Array '%s' has %d extra actual elements%n", path, actArray.size() - expArray.size());
            }
            return;
        }

        // Case: leaf node (primitives)
        if (!expected.equals(actual)) {
            System.out.printf("❌ Mismatch at '%s' → expected: %s, actual: %s%n", path, expected, actual);
        }
    }

    // 🔍 Test / demo
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        String expectedJson = """
            {
              "id": 1,
              "name": "John",
              "details": {
                "city": "London",
                "phones": ["123", "456"],
                "address": {"line1": "10 Downing", "zip": "SW1"}
              }
            }
        """;

        String actualJson = """
            {
              "id": 1,
              "name": "Johnny",
              "details": {
                "city": "Paris",
                "phones": ["123"],
                "address": {"line1": "11 Downing", "zip": "SW1", "country": "UK"}
              },
              "extraField": "ignored"
            }
        """;

        JsonNode expected = mapper.readTree(expectedJson);
        JsonNode actual = mapper.readTree(actualJson);

        printDifferences(expected, actual, "");
    }
}
