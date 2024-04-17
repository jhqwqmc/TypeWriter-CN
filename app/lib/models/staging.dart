import "package:flutter/material.dart";
import "package:hooks_riverpod/hooks_riverpod.dart";

class PublishPagesIntent extends Intent {
  const PublishPagesIntent();
}

final stagingStateProvider = StateProvider((ref) => StagingState.production);

enum StagingState {
  publishing("发布中", Colors.lightBlue),
  staging("暂存中", Colors.orange),
  production("生产中", Colors.green);

  const StagingState(this.label, this.color);

  final String label;
  final Color color;
}
