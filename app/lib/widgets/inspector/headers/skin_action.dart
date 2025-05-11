import "dart:convert";

import "package:flutter/material.dart";
import "package:flutter_hooks/flutter_hooks.dart";
import "package:hooks_riverpod/hooks_riverpod.dart";
import "package:http/http.dart" as http;
import "package:ktx/collections.dart";
import "package:typewriter/models/entry_blueprint.dart";
import "package:typewriter/utils/extensions.dart";
import "package:typewriter/utils/icons.dart";
import "package:typewriter/utils/passing_reference.dart";
import "package:typewriter/widgets/components/app/header_button.dart";
import "package:typewriter/widgets/components/general/admonition.dart";
import "package:typewriter/widgets/components/general/dropdown.dart";
import "package:typewriter/widgets/components/general/formatted_text_field.dart";
import "package:typewriter/widgets/components/general/iconify.dart";
import "package:typewriter/widgets/components/general/loading_button.dart";
import "package:typewriter/widgets/inspector/header.dart";
import "package:typewriter/widgets/inspector/inspector.dart";

class SkinFetchFromUUIDHeaderActionFilter extends HeaderActionFilter {
  @override
  bool shouldShow(
    String path,
    HeaderContext context,
    DataBlueprint dataBlueprint,
  ) =>
      dataBlueprint is CustomBlueprint && dataBlueprint.editor == "skin";

  @override
  HeaderActionLocation location(
    String path,
    HeaderContext context,
    DataBlueprint dataBlueprint,
  ) =>
      HeaderActionLocation.actions;

  @override
  Widget build(
    String path,
    HeaderContext context,
    DataBlueprint dataBlueprint,
  ) =>
      SkinFetchFromUUIDHeaderAction(
        path: path,
      );
}

class SkinFetchFromUUIDHeaderAction extends HookConsumerWidget {
  const SkinFetchFromUUIDHeaderAction({
    required this.path,
    super.key,
  });

  final String path;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return HeaderButton(
      tooltip: "从UUID获取",
      icon: TWIcons.accountTag,
      color: Colors.orange,
      onTap: () => showDialog(
        context: context,
        builder: (context) => _FetchFromMineSkinDialogue(
          path: path,
          bodyKey: "user",
          icon: TWIcons.accountTag,
        ),
      ),
    );
  }
}

class SkinFetchFromURLHeaderActionFilter extends HeaderActionFilter {
  @override
  bool shouldShow(
    String path,
    HeaderContext context,
    DataBlueprint dataBlueprint,
  ) =>
      dataBlueprint is CustomBlueprint && dataBlueprint.editor == "skin";

  @override
  HeaderActionLocation location(
    String path,
    HeaderContext context,
    DataBlueprint dataBlueprint,
  ) =>
      HeaderActionLocation.actions;

  @override
  Widget build(
    String path,
    HeaderContext context,
    DataBlueprint dataBlueprint,
  ) =>
      SkinFetchFromURLHeaderAction(
        path: path,
      );
}

class SkinFetchFromURLHeaderAction extends HookConsumerWidget {
  const SkinFetchFromURLHeaderAction({
    required this.path,
    super.key,
  });

  final String path;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return HeaderButton(
      tooltip: "从URL获取",
      icon: TWIcons.url,
      color: Colors.blue,
      onTap: () => showDialog(
        context: context,
        builder: (context) => _FetchFromMineSkinDialogue(
          path: path,
          bodyKey: "url",
          icon: TWIcons.url,
        ),
      ),
    );
  }
}

enum SkinVariant {
  classic,
  slim,
  unknown,
}

class _FetchFromMineSkinDialogue extends HookConsumerWidget {
  const _FetchFromMineSkinDialogue({
    required this.path,
    required this.bodyKey,
    required this.icon,
  });

  final String path;
  final String bodyKey;
  final String icon;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final controller = useTextEditingController();
    final focus = useFocusNode();
    final error = useState<String?>(null);
    final selectedVariant = useState<SkinVariant>(SkinVariant.unknown);

    return AlertDialog(
      title: const Text("获取皮肤"),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (error.value != null) ...[
            Admonition.danger(
              child: Text(error.value!),
            ),
            const SizedBox(height: 8),
          ],
          FormattedTextField(
            focus: focus,
            controller: controller,
            icon: icon,
            hintText: "输入 $bodyKey 以获取皮肤",
          ),
          const SizedBox(height: 16),
          Dropdown<SkinVariant>(
            value: selectedVariant.value,
            values: SkinVariant.values,
            icon: TWIcons.skin,
            onChanged: (value) {
              selectedVariant.value = value;
            },
            builder: (context, value) {
              return Text(value.name.formatted);
            },
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text("取消"),
        ),
        LoadingButton.icon(
          icon: const Iconify(TWIcons.download),
          onPressed: () async {
            final navigator = Navigator.of(context);
            final result = await _fetchSkin(
              ref.passing,
              controller.text,
              selectedVariant.value,
            );
            if (result == null) {
              navigator.pop();
              return;
            }
            focus.requestFocus();
            error.value = result;
          },
          label: const Text("获取"),
        ),
      ],
    );
  }

  Future<String?> _fetchSkin(
    PassingRef ref,
    String data,
    SkinVariant variant,
  ) async {
    final headers = {
      "User-Agent": "Typewriter/1.0",
      "Content-Type": "application/json",
    };

    final body = {
      "visibility": "public",
      bodyKey: data,
      "variant": variant.name,
    };

    final response = await http.post(
      Uri.parse("https://api.mineskin.org/v2/generate"),
      headers: headers,
      body: jsonEncode(body),
    );

    if (response.statusCode != 200) {
      final data = jsonDecode(response.body);
      if (data is Map<String, dynamic> && data.containsKey("errors")) {
        final errors = data["errors"];
        if (errors is List<dynamic> && errors.isNotEmpty) {
          return errors.mapNotNull((e) {
            if (e is Map<String, dynamic> && e.containsKey("message")) {
              return e["message"];
            }
            return null;
          }).join("\n");
        }
      }
      return "发生未知错误";
    }

    final result = jsonDecode(response.body);
    if (result is! Map<String, dynamic>) {
      return "发生未知错误";
    }

    if (!result.containsKey("skin")) {
      return "在响应中找不到皮肤数据";
    }

    final textureObject = result["skin"]["texture"];
    if (textureObject == null || textureObject is! Map<String, dynamic>) {
      return "响应中的纹理数据无效";
    }

    final textureData = textureObject["data"];
    if (textureData == null || textureData is! Map<String, dynamic>) {
      return "响应中的纹理数据无效";
    }

    final texture = textureData["value"];
    final signature = textureData["signature"];

    if (texture is! String || signature is! String) {
      return "响应中的纹理或签名无效";
    }

    final definition = ref.read(inspectingEntryDefinitionProvider);
    if (definition == null) {
      return "当前未检查任何条目";
    }

    await definition.updateField(ref, path, {
      "texture": texture,
      "signature": signature,
    });

    return null;
  }
}
