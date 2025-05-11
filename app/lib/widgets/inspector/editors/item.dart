import "package:auto_size_text/auto_size_text.dart";
import "package:flutter/material.dart";
import "package:hooks_riverpod/hooks_riverpod.dart";
import "package:typewriter/models/entry_blueprint.dart";
import "package:typewriter/models/materials.dart";
import "package:typewriter/utils/extensions.dart";
import "package:typewriter/utils/icons.dart";
import "package:typewriter/widgets/components/app/input_field.dart";
import "package:typewriter/widgets/components/general/admonition.dart";
import "package:typewriter/widgets/components/general/iconify.dart";
import "package:typewriter/widgets/inspector/editors.dart";
import "package:typewriter/widgets/inspector/editors/field.dart";
import "package:typewriter/widgets/inspector/editors/material.dart";
import "package:typewriter/widgets/inspector/header.dart";
import "package:typewriter/widgets/inspector/section_title.dart";

class ItemEditorFilter extends EditorFilter {
  @override
  bool canEdit(DataBlueprint dataBlueprint) =>
      dataBlueprint is CustomBlueprint && dataBlueprint.editor == "item";
  @override
  Widget build(String path, DataBlueprint dataBlueprint) =>
      ItemEditor(path: path, customBlueprint: dataBlueprint as CustomBlueprint);

  @override
  (HeaderActions, Iterable<(String, HeaderContext, DataBlueprint)>)
      headerActions(
    Ref<Object?> ref,
    String path,
    DataBlueprint dataBlueprint,
    HeaderContext context,
  ) {
    final actions = super.headerActions(ref, path, dataBlueprint, context);
    final shape = (dataBlueprint as CustomBlueprint).shape;

    final shapeActions = headerActionsFor(
      ref,
      path,
      shape,
      context.copyWith(parentBlueprint: dataBlueprint),
    );

    return (
      actions.$1.merge(shapeActions.$1),
      actions.$2.followedBy(shapeActions.$2)
    );
  }
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
    final algebraicBlueprint = customBlueprint.shape is AlgebraicBlueprint
        ? customBlueprint.shape as AlgebraicBlueprint?
        : null;
    if (algebraicBlueprint == null) {
      return Admonition.danger(
        child:
            Text("物品字段的形状不是代数蓝图: $path"),
      );
    }
    return FieldHeader(
      path: path,
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

  @override
  (HeaderActions, Iterable<(String, HeaderContext, DataBlueprint)>)
      headerActions(
    Ref<Object?> ref,
    String path,
    DataBlueprint dataBlueprint,
    HeaderContext context,
  ) {
    final actions = super.headerActions(ref, path, dataBlueprint, context);
    final shape = (dataBlueprint as CustomBlueprint).shape;

    final shapeActions = headerActionsFor(
      ref,
      path,
      shape,
      context.copyWith(parentBlueprint: dataBlueprint),
    );

    return (
      actions.$1.merge(shapeActions.$1),
      actions.$2.followedBy(shapeActions.$2)
    );
  }
}

class SerializedItemEditor extends HookConsumerWidget {
  const SerializedItemEditor({
    required this.path,
    required this.customBlueprint,
    super.key,
  });

  final String path;
  final CustomBlueprint customBlueprint;

  DataBlueprint get amountBlueprint {
    final shape = customBlueprint.shape;
    if (shape is! ObjectBlueprint) {
      return const PrimitiveBlueprint(type: PrimitiveType.integer);
    }
    final field = shape.fields["amount"];
    if (field == null) {
      return const PrimitiveBlueprint(type: PrimitiveType.integer);
    }
    return field;
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final value =
        ref.watch(fieldValueProvider(path, customBlueprint.defaultValue()));

    if (value is! Map<String, dynamic>) {
      return Admonition.danger(
        child: Text("序列化物品字段的值不是映射: $path"),
      );
    }

    final material = value["material"] as String? ?? "AIR";
    final name = value["name"] as String? ?? "";
    final bytes = value["bytes"] as String? ?? "";

    if (bytes.isEmpty) {
      return const Admonition.warning(
        child: Text(
          "您尚未捕获该物品。点击蓝色相机图标可捕获游戏中您持有的物品。",
        ),
      );
    }

    final minecraftMaterial = materials[material.toLowerCase()];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      spacing: 8,
      children: [
        if (minecraftMaterial != null) ...[
          const SectionTitle(title: "材质"),
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
          FieldEditor(
            path: path.join("amount"),
            dataBlueprint: amountBlueprint,
          ),
          const SizedBox(height: 0),
          const Text(
            "此物品已从游戏中捕获。如需修改，可重新捕获该物品。",
          ),
        ],
      ],
    );
  }
}
