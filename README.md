List<String> diffs = JsonDiffUtil.collectDifferences(expected, actual);
assertTrue(diffs.isEmpty(), () -> "JSON mismatch:\n" + String.join("\n", diffs));
