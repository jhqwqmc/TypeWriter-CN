import "package:auto_size_text/auto_size_text.dart";
import "package:flutter/material.dart";
import "package:hooks_riverpod/hooks_riverpod.dart";
import "package:typewriter/models/entry_blueprint.dart";
import "package:typewriter/models/materials.dart";
import "package:typewriter/utils/icons.dart";
import "package:typewriter/widgets/components/app/input_field.dart";
import "package:typewriter/widgets/components/general/admonition.dart";
import "package:typewriter/widgets/components/general/iconify.dart";
import "package:typewriter/widgets/inspector/editors.dart";
import "package:typewriter/widgets/inspector/editors/field.dart";
import "package:typewriter/widgets/inspector/editors/material.dart";
import "package:typewriter/widgets/inspector/editors/number.dart";
import "package:typewriter/widgets/inspector/header.dart";
import "package:typewriter/widgets/inspector/section_title.dart";

class ItemEditorFilter extends EditorFilter {
  @override
  bool canEdit(DataBlueprint dataBlueprint) =>
      dataBlueprint is CustomBlueprint && dataBlueprint.editor == "item";
  @override
  Widget build(String path, DataBlueprint dataBlueprint) =>
      ItemEditor(path: path, customBlueprint: dataBlueprint as CustomBlueprint);
}

class ItemEditor extends HookConsumerWidget {
  const ItemEditor({
    required this.path,
    required this.customBlueprint,
    super.key,
  });

  final String path;
  final CustomBlueprint customBlueprint;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // final value = ref.watch(fieldValueProvider(path));
    final algebraicBlueprint = customBlueprint.shape is AlgebraicBlueprint
        ? customBlueprint.shape as AlgebraicBlueprint?
        : null;
    if (algebraicBlueprint == null) {
      return Admonition.danger(
        child:
            Text("项目字段的形状不是代数蓝图：$path"),
      );
    }
    return FieldHeader(
      path: path,
      dataBlueprint: customBlueprint,
      canExpand: true,
      child: FieldEditor(path: path, dataBlueprint: algebraicBlueprint),
    );
  }
}

class SerializedItemEditorFilter extends EditorFilter {
  @override
  bool canEdit(DataBlueprint dataBlueprint) =>
      dataBlueprint is CustomBlueprint &&
      dataBlueprint.editor == "serialized_item";

  @override
  Widget build(String path, DataBlueprint dataBlueprint) =>
      SerializedItemEditor(
        path: path,
        customBlueprint: dataBlueprint as CustomBlueprint,
      );
}

class SerializedItemEditor extends HookConsumerWidget {
  const SerializedItemEditor({
    required this.path,
    required this.customBlueprint,
    super.key,
  });

  final String path;
  final CustomBlueprint customBlueprint;

  PrimitiveBlueprint get amountBlueprint {
    final shape = customBlueprint.shape;
    if (shape is! ObjectBlueprint) {
      return const PrimitiveBlueprint(type: PrimitiveType.integer);
    }
    final field = shape.fields["amount"];
    if (field == null || field is! PrimitiveBlueprint) {
      return const PrimitiveBlueprint(type: PrimitiveType.integer);
    }
    return field;
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final value =
        ref.watch(fieldValueProvider(path)) ?? customBlueprint.defaultValue();

    if (value is! Map<String, dynamic>) {
      return Admonition.danger(
        child: Text("序列化项目字段的值不是映射：$path"),
      );
    }

    final material = value["material"] as String? ?? "AIR";
    final name = value["name"] as String? ?? "";
    final bytes = value["bytes"] as String? ?? "";

    if (bytes.isEmpty) {
      return const Admonition.warning(
        child: Text(
          "您尚未捕获该物品。点击蓝色的相机图标来捕获您在游戏中持有的物品。",
        ),
      );
    }

    final minecraftMaterial = materials[material.toLowerCase()];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      spacing: 8,
      children: [
        if (minecraftMaterial != null) ...[
          const SectionTitle(title: "材料"),
          Opacity(
            opacity: 0.5,
            child: InputField(
              child: MaterialItem(
                id: material.toLowerCase(),
                material: minecraftMaterial,
              ),
            ),
          ),
        ],
        if (name.isNotEmpty) ...[
          const SectionTitle(title: "物品名称"),
          Opacity(
            opacity: 0.5,
            child: InputField.icon(
              icon: const Iconify(TWIcons.book),
              child: AutoSizeText(
                name,
                maxLines: 1,
                style: const TextStyle(fontSize: 14),
              ),
            ),
          ),
          const SectionTitle(title: "数量"),
          NumberEditor(
            path: "$path.amount",
            primitiveBlueprint: amountBlueprint,
          ),
          const SizedBox(height: 0),
          const Text(
            "该物品已从游戏中捕获。如果你想更改它，你可以重新捕获该物品。",
          ),
        ],
      ],
    );
  }
}
