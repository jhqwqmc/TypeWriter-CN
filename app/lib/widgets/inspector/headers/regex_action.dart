import "package:flutter/material.dart";
import "package:hooks_riverpod/hooks_riverpod.dart";
import "package:typewriter/models/adapter.dart";
import "package:typewriter/utils/icons.dart";
import "package:typewriter/widgets/inspector/header.dart";
import "package:typewriter/widgets/inspector/headers/info_action.dart";

class RegexHeaderActionFilter extends HeaderActionFilter {
  @override
  bool shouldShow(String path, FieldInfo field) =>
      field.getModifier("regex") != null;

  @override
  HeaderActionLocation location(String path, FieldInfo field) =>
      HeaderActionLocation.trailing;

  @override
  Widget build(String path, FieldInfo field) => const RegexHeaderInfo();
}

class RegexHeaderInfo extends HookConsumerWidget {
  const RegexHeaderInfo({
    super.key,
  }) : super();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return const InfoHeaderAction(
      tooltip: "支持正则表达式。点击查看更多信息。（译者注：这个网站其实是可以使用中文描述的）",
      icon: TWIcons.asterisk,
      color: Color(0xFFf731d6),
      url: "https://www.autoregex.xyz/",
    );
  }
}
