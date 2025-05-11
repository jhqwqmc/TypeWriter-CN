import "package:collection/collection.dart";
import "package:flutter/material.dart";
import "package:hooks_riverpod/hooks_riverpod.dart";
import "package:typewriter/models/entry_blueprint.dart";
import "package:typewriter/widgets/components/general/admonition.dart";
import "package:typewriter/widgets/inspector/editors.dart";

class FieldEditor extends HookConsumerWidget {
  const FieldEditor({
    required this.path,
    required this.dataBlueprint,
    super.key,
  }) : super();
  final String path;
  final DataBlueprint dataBlueprint;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final filters = ref.watch(editorFiltersProvider);

    final editor = filters
        .firstWhereOrNull((filter) => filter.canEdit(dataBlueprint))
        ?.build(path, dataBlueprint);

    if (editor == null) {
      return _NoEditorFound(path: path, dataBlueprint: dataBlueprint);
    }

    return editor;
  }
}

class _NoEditorFound extends StatelessWidget {
  const _NoEditorFound({
    required this.path,
    required this.dataBlueprint,
  });

  final String path;
  final DataBlueprint dataBlueprint;

  @override
  Widget build(BuildContext context) {
    return Admonition.danger(
      child: Text(
        "找不到$path的编辑器，数据蓝图类型为${dataBlueprint.runtimeType}",
      ),
    );
  }
}
