import "package:auto_route/auto_route.dart";
import "package:flutter/material.dart" hide FilledButton;
import "package:hooks_riverpod/hooks_riverpod.dart";
import "package:rive/rive.dart";
import "package:typewriter/hooks/delayed_execution.dart";
import "package:typewriter/models/communicator.dart";
import "package:typewriter/widgets/components/general/copyable_text.dart";

@RoutePage()
class ErrorConnectPage extends HookConsumerWidget {
  const ErrorConnectPage({
    super.key,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    useDelayedExecution(() {
      // Make sure the socket gets cleaned up
      ref.invalidate(socketProvider);
    });

    return const Scaffold(
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Spacer(),
          Expanded(
            flex: 6,
            child: MouseRegion(
              cursor: SystemMouseCursors.zoomIn,
              child: RiveAnimation.asset(
                "assets/robot_island.riv",
                stateMachines: ["Motion"],
              ),
            ),
          ),
          SizedBox(height: 24),
          Text(
            "通信故障",
            style: TextStyle(
              fontSize: 40,
              fontWeight: FontWeight.bold,
              color: Colors.red,
            ),
          ),
          Text(
            "与服务器通信时出错。\n请检查你的连接并重试。",
            textAlign: TextAlign.center,
            style: TextStyle(fontSize: 20, color: Colors.grey),
          ),
          SizedBox(height: 24),
          CopyableText(text: "/typewriter connect"),
          SizedBox(height: 24),
          Spacer(),
        ],
      ),
    );
  }
}
