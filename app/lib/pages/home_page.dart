import "dart:async";

import "package:auto_route/auto_route.dart";
import "package:flutter/material.dart" hide FilledButton;
import "package:hooks_riverpod/hooks_riverpod.dart";
import "package:rive/rive.dart";
import "package:typewriter/app_router.dart";
import "package:typewriter/utils/fonts.dart";
import "package:typewriter/utils/icons.dart";
import "package:typewriter/widgets/components/general/copyable_text.dart";
import "package:typewriter/widgets/components/general/filled_button.dart";
import "package:typewriter/widgets/components/general/iconify.dart";

@RoutePage()
class HomePage extends HookConsumerWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return const Scaffold(
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Spacer(),
          Expanded(
            flex: 2,
            child: RiveAnimation.asset(
              "assets/game_character.riv",
              stateMachines: ["State Machine"],
            ),
          ),
          Text(
            "您的旅程从这里开始",
            textAlign: TextAlign.center,
            style: TextStyle(fontSize: 40, fontWeight: FontWeight.bold),
          ),
          Text(
            "在您的服务器上运行以下命令以开始编辑",
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 20,
              color: Colors.grey,
              fontVariations: [thinWeight],
            ),
          ),
          SizedBox(height: 24),
          CopyableText(text: "/typewriter connect"),
          SizedBox(height: 24),
          _ConnectButtons(),
          SizedBox(height: 24),
          Spacer(),
        ],
      ),
    );
  }
}

class _ConnectButtons extends HookConsumerWidget {
  const _ConnectButtons();

  void connectTo(
    WidgetRef ref,
    String hostname,
    int? port, {
    String token = "",
    bool secure = false,
  }) {
    ref.read(appRouter).replaceAll(
      [
        ConnectRoute(
          hostname: hostname,
          port: port,
          token: token,
          secure: secure,
        ),
      ],
    );
  }

  Future<void> customConnectToPopup(BuildContext context, WidgetRef ref) async {
    final controller = TextEditingController();
    final url = await showDialog<String?>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text("连接到"),
          content: TextField(
            controller: controller,
            decoration: const InputDecoration(
              hintText: "填写要连接的URL",
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text("取消"),
            ),
            FilledButton.icon(
              icon: const Iconify(TWIcons.externalLink),
              label: const Text("连接"),
              onPressed: () => Navigator.of(context).pop(controller.text),
            ),
          ],
        );
      },
    );

    if (url == null) return;

    final uri = Uri.parse(url.replaceAll("#/", ""));
    // Get the hostname and port and token from the url query parameters
    // The token is optional and the hostname can be "hostname" or "host"
    final hostname =
        uri.queryParameters["host"] ?? uri.queryParameters["hostname"];
    final port = int.tryParse(uri.queryParameters["port"] ?? "");
    final token = uri.queryParameters["token"] ?? "";
    final secure = uri.queryParameters["secure"] == "true";

    debugPrint("正在连接到 $hostname:$port (使用令牌 $token)");

    if (hostname == null) {
      return;
    }

    connectTo(ref, hostname, port, token: token, secure: secure);
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        FilledButton.icon(
          color: Colors.green,
          icon: const Iconify(TWIcons.home),
          label: const Text("连接本地主机"),
          onPressed: () => connectTo(ref, "localhost", 9092),
        ),
        const SizedBox(width: 24),
        FilledButton.icon(
          icon: const Iconify(TWIcons.connect),
          label: const Text("连接自定义地址"),
          onPressed: () => customConnectToPopup(context, ref),
        ),
      ],
    );
  }
}
