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
    @QueryParam() this.port,
    @QueryParam() this.token = "",
    @QueryParam() this.secure = false,
    super.key,
  });

  final String hostname;
  final int? port;
  final String token;
  final bool secure;

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
          ref.read(socketProvider.notifier).init(
                hostname,
                port,
                token: token.isEmpty ? null : token,
                secure: secure,
              );
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
            "等待连接中...",
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
        "调整通信频率",
        "启动通信协议",
        "协商连接参数",
        "分析网络流量",
        "建立心灵感应链接",
        "激活量子通信",
        "建立虚拟专用连接",
        "检测网络干扰",
        "黑入母体矩阵",
        "召唤跨维度传送门",
        "打开星界通道",
        "建立与彼岸世界的连接",
        "连接宇宙意识",
        "联系外星智慧生命",
        "拨通时空连续体",
        "从未来下载思维数据",
        "建立平行宇宙链接",
        "连接宇宙意识网络",
        "调谐宇宙频率",
        "启动银河系间通信",
        "扭曲现实结构",
        "同步宇宙时钟",
        "激活跨维度中继站",
        "建立念力连接",
        "引导宇宙能量",
        "解锁宇宙奥秘",
        "联系全视之眼",
        "时空传送",
        "连接高维空间",
        "接通超凡领域",
        "从阿卡西记录下载知识",
        "建立心灵链接",
        "开启宇宙之门",
        "同步宇宙频率",
        "调谐宇宙振动",
        "连接量子场",
        "建立神圣连接",
        "引导宇宙智慧",
      ]..shuffle(),
      style: style,
    );
  }
}
