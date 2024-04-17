import "package:flutter/material.dart";
import "package:font_awesome_flutter/font_awesome_flutter.dart";
import "package:hooks_riverpod/hooks_riverpod.dart";
import "package:typewriter/models/adapter.dart";
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
      tooltip: "支持正则表达式。 点击了解更多信息。",
      icon: FontAwesomeIcons.asterisk,
      color: Color(0xFFf731d6),
      url:
          "https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Guide/Regular_Expressions",
    );
  }
}
