import "package:flutter/material.dart";
import "package:hooks_riverpod/hooks_riverpod.dart";
import "package:typewriter/models/entry_blueprint.dart";
import "package:typewriter/utils/icons.dart";
import "package:typewriter/widgets/components/app/cord_property.dart";
import "package:typewriter/widgets/components/general/iconify.dart";
import "package:typewriter/widgets/inspector/editors.dart";
import "package:typewriter/widgets/inspector/header.dart";
import "package:typewriter/widgets/inspector/headers/info_action.dart";

class FloatRangeFilter extends EditorFilter {
  @override
  bool canEdit(DataBlueprint dataBlueprint) =>
      dataBlueprint is CustomBlueprint && dataBlueprint.editor == "floatRange";

  @override
  Widget build(String path, DataBlueprint dataBlueprint) => FloatRangeEditor(
        path: path,
        customBlueprint: dataBlueprint as CustomBlueprint,
      );
}

class FloatRangeEditor extends HookConsumerWidget {
  const FloatRangeEditor({
    required this.path,
    required this.customBlueprint,
    super.key,
  });
  final String path;

  final CustomBlueprint customBlueprint;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return FieldHeader(
      path: path,
      dataBlueprint: customBlueprint,
      trailing: const [
        InfoHeaderAction(
          tooltip: "最大值包含在范围内。",
          icon: TWIcons.inclusive,
          color: Color(0xFF0ccf92),
          url: "",
        ),
      ],
      child: Row(
        children: [
          CordPropertyEditor(
            path: "$path.start",
            label: "最小值",
            color: Colors.red,
          ),
          const Iconify(TWIcons.range),
          CordPropertyEditor(
            path: "$path.end",
            label: "最大值",
            color: Colors.green,
          ),
        ],
      ),
    );
  }
}
