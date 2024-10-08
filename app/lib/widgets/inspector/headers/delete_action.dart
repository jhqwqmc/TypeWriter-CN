import "package:flutter/material.dart";
import "package:hooks_riverpod/hooks_riverpod.dart";
import "package:typewriter/utils/extensions.dart";
import "package:typewriter/utils/icons.dart";
import "package:typewriter/utils/popups.dart";
import "package:typewriter/widgets/components/general/iconify.dart";
import "package:typewriter/widgets/inspector/editors.dart";

class RemoveHeaderAction extends HookConsumerWidget {
  const RemoveHeaderAction({
    required this.path,
    required this.onRemove,
    super.key,
  }) : super();

  final String path;
  final VoidCallback onRemove;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final name = ref.watch(pathDisplayNameProvider(path)).singular;
    return IconButton(
      icon: const Iconify(TWIcons.trash, size: 12),
      color: Theme.of(context).colorScheme.error,
      tooltip: "移除$name",
      onPressed: () => showConfirmationDialogue(
        context: context,
        title: "移除$name？",
        content: "你确定要删除该项吗？",
        onConfirm: onRemove,
      ),
    );
  }
}
