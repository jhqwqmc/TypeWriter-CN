import "dart:async";

import "package:auto_route/auto_route.dart";
import "package:flutter/material.dart" hide Page;
import "package:flutter_animate/flutter_animate.dart";
import "package:flutter_hooks/flutter_hooks.dart";
import "package:hooks_riverpod/hooks_riverpod.dart";
import "package:rive/rive.dart";
import "package:typewriter/app_router.dart";
import "package:typewriter/hooks/delayed_execution.dart";
import "package:typewriter/models/communicator.dart";
import "package:typewriter/widgets/components/general/text_scroller.dart";

@RoutePage()
class ConnectPage extends HookConsumerWidget {
  const ConnectPage({
    @QueryParam("host") this.hostname = "",
    @QueryParam() this.port = 9092,
    @QueryParam() this.token = "",
    super.key,
  });

  final String hostname;
  final int port;
  final String token;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // If the hostname is empty, we want to go back to the home page.
    useDelayedExecution(() {
      if (hostname.isEmpty) {
        ref.read(appRouter).replaceAll([const HomeRoute()]);
        return;
      }
    });

    // We want to wait a second before we connect to the server.
    // This is to give the user a chance to read the text.
    useEffect(
      () {
        final timer = Timer(1.seconds, () {
          ref
              .read(socketProvider.notifier)
              .init(hostname, port, token.isEmpty ? null : token);
        });
        return timer.cancel;
      },
      [],
    );

    return const Scaffold(
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Spacer(),
          Expanded(
            flex: 8,
            child: RiveAnimation.asset(
              "assets/tour.riv",
              stateMachines: ["state_machine"],
            ),
          ),
          SizedBox(height: 24),
          Text(
            "等待连接",
            style: TextStyle(fontSize: 40, fontWeight: FontWeight.bold),
          ),
          ConnectionScroller(
            style: TextStyle(fontSize: 20, color: Colors.grey),
          ),
          SizedBox(height: 24),
          Spacer(),
        ],
      ),
    );
  }
}

class ConnectionScroller extends HookWidget {
  const ConnectionScroller({this.style, super.key});

  final TextStyle? style;

  @override
  Widget build(BuildContext context) {
    return TextScroller(
      texts: [
        "建立星际连接",
        "调整通讯频率",
        "启动通讯协议",
        "协商连接参数",
        "分析网络流量",
        "建立心灵感应链接",
        "激活量子通讯",
        "建立虚拟私人连接",
        "检查网络干扰",
        "入侵矩阵",
        "召唤跨维度门户",
        "打开通往星界的门户",
        "建立与彼岸的连接",
        "连接宇宙意识",
        "联系地外智慧",
        "拨通时空连线",
        "从未来下载思想",
        "建立到平行宇宙的链接",
        "建立与普遍意识的链接",
        "调整到宇宙频率",
        "启动星际通讯",
        "弯曲现实之网",
        "与宇宙钟同步",
        "激活跨维度中继",
        "建立念力连接",
        "引导宇宙能量",
        "揭示宇宙奥秘",
        "联系全知之眼",
        "穿越时空传送",
        "调整至更高维度",
        "连接至无垠之外",
        "从阿卡西记录下载知识",
        "建立心灵链接",
        "激活宇宙门户",
        "与宇宙频率同步",
        "调谐宇宙振动",
        "连接量子场",
        "建立与神明的联系",
        "引导宇宙智慧",
      ]..shuffle(),
      style: style,
    );
  }
}
