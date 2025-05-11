import "package:flutter/foundation.dart";
import "package:flutter/scheduler.dart";
import "package:flutter/widgets.dart";
import "package:flutter_hooks/flutter_hooks.dart";

/// Create a multi usage [TickerProvider].
///
/// See also:
///  * [TickerProviderStateMixin]
TickerProvider useTickerProvider({List<Object?>? keys}) {
  return use(
    keys != null ? _TickerProviderHook(keys) : const _TickerProviderHook(),
  );
}

class _TickerProviderHook extends Hook<TickerProvider> {
  const _TickerProviderHook([List<Object?>? keys]) : super(keys: keys);

  @override
  _TickerProviderHookState createState() => _TickerProviderHookState();
}

class _TickerProviderHookState
    extends HookState<TickerProvider, _TickerProviderHook>
    implements TickerProvider {
  Set<Ticker>? _tickers;
  ValueListenable<bool>? _tickerModeNotifier;

  @override
  Ticker createTicker(TickerCallback onTick) {
    if (_tickerModeNotifier == null) {
      // Setup TickerMode notifier before we vend the first ticker.
      _updateTickerModeNotifier();
    }
    assert(_tickerModeNotifier != null, "TickerMode未初始化");
    _tickers ??= <Ticker>{};
    final result = Ticker(onTick, debugLabel: "由 $context 创建")
      ..muted = !_tickerModeNotifier!.value;
    _tickers!.add(result);
    return result;
  }

  @override
  void dispose() {
    assert(
      () {
        if (_tickers != null) {
          for (final ticker in _tickers!) {
            if (ticker.isActive) {
              throw FlutterError.fromParts(<DiagnosticsNode>[
                ErrorSummary("$this 在Ticker仍活跃时被销毁"),
                ErrorDescription(
                  "$runtimeType 通过TickerProviderStateMixin创建了Ticker， "
                  "但在调用mixin的dispose()时，该Ticker仍处于活跃状态。 "
                  "在调用super.dispose()前必须释放所有Ticker。",
                ),
                ErrorHint(
                  "AnimationController使用的Ticker "
                  "应通过调用AnimationController自身的dispose()来释放。 "
                  "否则会导致ticker泄漏。",
                ),
                ticker.describeForError("违规的ticker信息"),
              ]);
            }
          }
        }
        return true;
      }(),
      "Ticker未被释放",
    );
    _tickerModeNotifier?.removeListener(_updateTickers);
    _tickerModeNotifier = null;
    super.dispose();
  }

  @override
  TickerProvider build(BuildContext context) {
    _updateTickerModeNotifier();
    _updateTickers();
    return this;
  }

  void _updateTickers() {
    if (_tickers != null) {
      final muted = !_tickerModeNotifier!.value;
      for (final ticker in _tickers!) {
        ticker.muted = muted;
      }
    }
  }

  void _updateTickerModeNotifier() {
    final newNotifier = TickerMode.getNotifier(context);
    if (newNotifier == _tickerModeNotifier) {
      return;
    }
    _tickerModeNotifier?.removeListener(_updateTickers);
    newNotifier.addListener(_updateTickers);
    _tickerModeNotifier = newNotifier;
  }

  @override
  String get debugLabel => "useTickerProvider";

  @override
  bool get debugSkipValue => true;
}
