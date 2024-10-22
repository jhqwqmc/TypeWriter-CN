import "package:flutter_test/flutter_test.dart";
import "package:typewriter/models/entry.dart";

void main() {
  final rawDynamicEntry = {
    "id": "1",
    "name": "test",
    "type": "test_blueprint_id",
    "simple_list": [1, 2, 3],
    "complex_list": [
      {"id": "1", "name": "test1"},
      {"id": "2", "name": "test2"},
      {"id": "3", "name": "test3"},
    ],
    "simple_map": {"key1": "value1", "key2": "value2"},
    "complex_map": {
      "key1": {
        "id": "1",
        "name": "test1",
        "inner_list": [
          {"id": "1", "name": "test1"},
          {"id": "2", "name": "test2"},
          {"id": "3", "name": "test3"},
        ],
      },
      "key2": {
        "id": "2",
        "name": "test2",
        "inner_list": [
          {"id": "1", "name": "test_a"},
          {"id": "2", "name": "test_b"},
          {"id": "3", "name": "test_c"},
        ],
      },
    },
  };

  group("从条目获取字段", () {
    test("当解析条目时，期望能够获取其字段",
        () {
      final entry = Entry(rawDynamicEntry);

      expect(entry.id, "1");
      expect(entry.name, "test");
      expect(entry.blueprintId, "test_blueprint_id");

      expect(entry.get("simple_list"), [1, 2, 3]);
      expect(entry.get("simple_list.1"), 2);

      expect(entry.get("complex_list"), [
        {"id": "1", "name": "test1"},
        {"id": "2", "name": "test2"},
        {"id": "3", "name": "test3"},
      ]);
      expect(entry.get("complex_list.1.name"), "test2");

      expect(entry.get("simple_map"), {"key1": "value1", "key2": "value2"});
      expect(entry.get("simple_map.key1"), "value1");

      expect(entry.get("complex_map.key2.inner_list.1.name"), "test_b");
    });

    test("当找不到键时，返回 null", () {
      final entry = Entry(rawDynamicEntry);
      expect(entry.get("not_found"), null);
      expect(entry.get("simple_list.4"), null);
      expect(entry.get("complex_list.4.name"), null);
      expect(entry.get("complex_list.1.not_found"), null);
      expect(entry.get("simple_map.key3"), null);
    });

    test("当找不到键时，返回默认值", () {
      final entry = Entry(rawDynamicEntry);
      expect(entry.get("not_found", "default"), "default");
      expect(entry.get("simple_list.4", "default"), "default");
      expect(entry.get("complex_list.4.name", "default"), "default");
      expect(entry.get("complex_list.1.not_found", "default"), "default");
      expect(entry.get("simple_map.key3", "default"), "default");
    });
  });

  group("从条目中获取所有字段", () {
    test("获取路径时，应返回所有值", () {
      final entry = Entry(rawDynamicEntry);
      expect(entry.getAll("simple_list.*"), [1, 2, 3]);
      expect(entry.getAll("simple_map.*"), ["value1", "value2"]);
      expect(entry.getAll("complex_map.*.name"), ["test1", "test2"]);
      expect(
        entry.getAll("complex_map.*.inner_list.*.name"),
        ["test1", "test2", "test3", "test_a", "test_b", "test_c"],
      );
      expect(
        entry.getAll("complex_map.key2.inner_list.*.name"),
        ["test_a", "test_b", "test_c"],
      );
    });
  });

  group("复制具有新值的条目", () {
    test("更新动态条目时，返回新值", () {
      final entry = Entry(rawDynamicEntry);
      var newEntry = entry.copyWith("simple_list.1", 4);
      expect(newEntry.get("simple_list.1"), 4);

      newEntry = entry.copyWith("complex_list.1.name", "new_name");
      expect(newEntry.get("complex_list.1.name"), "new_name");

      newEntry = entry.copyWith("simple_map.key1", "new_value");
      expect(newEntry.get("simple_map.key1"), "new_value");

      newEntry =
          entry.copyWith("complex_map.key2.inner_list.1.name", "new_name");
      expect(newEntry.get("complex_map.key2.inner_list.1.name"), "new_name");
    });

    test("更新列表时，期望返回新列表", () {
      final entry = Entry(rawDynamicEntry);
      final newEntry = entry.copyWith("simple_list", [4, 5, 6]);
      expect(newEntry.get("simple_list"), [4, 5, 6]);
    });

    test("更新地图时，期望返回新地图", () {
      final entry = Entry(rawDynamicEntry);
      final newEntry = entry
          .copyWith("simple_map", {"key1": "new_value", "key2": "new_value"});
      expect(newEntry.get("simple_map"), {
        "key1": "new_value",
        "key2": "new_value",
      });
    });

    test("更新条目时，期望原始条目保持不变",
        () {
      final entry = Entry(rawDynamicEntry);
      // ignore: cascade_invocations
      entry.copyWith("complex_map.key2.inner_list.1.name", "new_name");
      expect(entry.get("complex_map.key2.inner_list.1.name"), "test_b");
    });

    test(
        "当用不存在的路径更新条目时，期望该路径被创建",
        () {
      final entry = Entry(rawDynamicEntry);
      final newEntry = entry.copyWith("new_path.1.something", "new_value");
      expect(newEntry.get("new_path.1.something"), "new_value");
    });

    test(
        "当用带有通配符的不存在路径更新条目时，期望什么都不会发生",
        () {
      final entry = Entry(rawDynamicEntry);
      final newEntry = entry.copyWith("new_path.*.something", "new_value");
      expect(newEntry.get("new_path.*.something"), null);
    });
  });

  group("复制已映射", () {
    test(
        "在修改简单静态字段时复制，期望字段会改变",
        () {
      final entry = Entry(rawDynamicEntry);
      final newEntry = entry.copyMapped("simple_list.1", (value) => value + 1);

      expect(
        newEntry.get("simple_list.0"),
        1,
        reason: "simple_list.0 should not have changed",
      );
      expect(
        newEntry.get("simple_list.1"),
        3,
        reason: "simple_list.1 should have changed",
      );
      expect(
        newEntry.get("simple_list.2"),
        3,
        reason: "simple_list.2 should not have changed",
      );
    });

    test(
        "在修改复杂静态字段时复制，期望字段会改变",
        () {
      final entry = Entry(rawDynamicEntry);
      final newEntry =
          entry.copyMapped("complex_list.1.name", (value) => value + "_new");

      expect(
        newEntry.get("complex_list.0.name"),
        "test1",
        reason: "complex_list.0.name should not have changed",
      );
      expect(
        newEntry.get("complex_list.1.name"),
        "test2_new",
        reason: "complex_list.1.name should have changed",
      );
      expect(
        newEntry.get("complex_list.2.name"),
        "test3",
        reason: "complex_list.2.name should not have changed",
      );
    });

    test(
        "在修改简单动态字段时复制，期望字段会改变",
        () {
      final entry = Entry(rawDynamicEntry);
      final newEntry =
          entry.copyMapped("simple_map.*", (value) => value + "_new");

      expect(
        newEntry.get("simple_map.key1"),
        "value1_new",
        reason: "simple_map.key1 should have changed",
      );
      expect(
        newEntry.get("simple_map.key2"),
        "value2_new",
        reason: "simple_map.key2 should have changed",
      );
    });

    test(
        "在修改复杂动态字段时复制，期望字段会改变",
        () {
      final entry = Entry(rawDynamicEntry);
      final newEntry =
          entry.copyMapped("complex_map.*.name", (value) => value + "_new");

      expect(
        newEntry.get("complex_map.key1.name"),
        "test1_new",
        reason: "complex_map.key1.name should have changed",
      );
      expect(
        newEntry.get("complex_map.key2.name"),
        "test2_new",
        reason: "complex_map.key2.name should have changed",
      );
    });

    test(
        "在修改带有多个*和最终字段的字段时复制，期望字段会改变",
        () {
      final entry = Entry(rawDynamicEntry);
      final newEntry = entry.copyMapped(
        "complex_map.*.inner_list.*.name",
        (value) => value + "_new",
      );

      expect(newEntry.get("complex_map.key1.inner_list.0.name"), "test1_new");
      expect(newEntry.get("complex_map.key1.inner_list.1.name"), "test2_new");
      expect(newEntry.get("complex_map.key1.inner_list.2.name"), "test3_new");
      expect(newEntry.get("complex_map.key2.inner_list.0.name"), "test_a_new");
      expect(newEntry.get("complex_map.key2.inner_list.1.name"), "test_b_new");
      expect(newEntry.get("complex_map.key2.inner_list.2.name"), "test_c_new");
    });

    test(
        "在修改简单动态字段为null时复制，期望字段被移除",
        () {
      final entry = Entry(rawDynamicEntry);
      final newEntry = entry.copyMapped(
        "simple_list.*",
        (value) => value == 2 ? null : value,
      );

      expect(
        newEntry.get("simple_list"),
        [1, 3],
        reason: "Should have removed the value of 2",
      );
    });

    test(
        "在修改复杂动态字段为null时复制，期望字段被移除",
        () {
      final entry = Entry(rawDynamicEntry);
      final newEntry = entry.copyMapped(
        "complex_map.*.inner_list.*",
        (value) => ["test1", "test2"].contains(value["name"]) ? null : value,
      );

      expect(newEntry.get("complex_map.key1.inner_list"), [
        {"id": "3", "name": "test3"},
      ]);
      expect(newEntry.get("complex_map.key2.inner_list"), [
        {"id": "1", "name": "test_a"},
        {"id": "2", "name": "test_b"},
        {"id": "3", "name": "test_c"},
      ]);
    });
  });

  group("新路径", () {
    test("静态路径仅返回该路径", () {
      final entry = Entry(rawDynamicEntry);
      final paths = entry.newPaths("simple_map.key1");

      expect(paths, ["simple_map.key1"]);
    });
    test("静态不存在路径返回路径", () {
      final entry = Entry(rawDynamicEntry);
      final paths = entry.newPaths("non_existing");

      expect(paths, ["non_existing"]);
    });
    test("简单列表返回带有下一个索引的路径", () {
      final entry = Entry(rawDynamicEntry);
      final paths = entry.newPaths("simple_list.*");

      expect(paths, ["simple_list.3"]);
    });
    test("带有固定结尾的复杂列表返回所有路径", () {
      final entry = Entry(rawDynamicEntry);
      final paths = entry.newPaths("complex_list.*.name");

      expect(paths, [
        "complex_list.0.name",
        "complex_list.1.name",
        "complex_list.2.name",
      ]);
    });
    test("带有通配符结尾的简单映射返回所有路径", () {
      final entry = Entry(rawDynamicEntry);
      final paths = entry.newPaths("simple_map.*");

      expect(paths, ["simple_map.key1", "simple_map.key2"]);
    });
    test("带有结尾列表的复杂映射返回下一个索引", () {
      final entry = Entry(rawDynamicEntry);
      final paths = entry.newPaths("complex_map.*.inner_list.*");

      expect(paths, [
        "complex_map.key1.inner_list.3",
        "complex_map.key2.inner_list.3",
      ]);
    });
    test("带有固定结尾的多通配符路径返回所有路径", () {
      final entry = Entry(rawDynamicEntry);
      final paths = entry.newPaths("complex_map.*.inner_list.*.name");

      expect(paths, [
        "complex_map.key1.inner_list.0.name",
        "complex_map.key1.inner_list.1.name",
        "complex_map.key1.inner_list.2.name",
        "complex_map.key2.inner_list.0.name",
        "complex_map.key2.inner_list.1.name",
        "complex_map.key2.inner_list.2.name",
      ]);
    });
  });
}
