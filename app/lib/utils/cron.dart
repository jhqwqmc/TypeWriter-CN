import "package:typewriter/utils/extensions.dart";

class CronExpression {
  const CronExpression(
    this.seconds,
    this.minutes,
    this.hours,
    this.dayOfMonth,
    this.month,
    this.dayOfWeek,
  );

  final SimpleField? seconds;
  final SimpleField minutes;
  final SimpleField hours;
  final DayOfMonthField dayOfMonth;
  final MonthField month;
  final DayOfWeekField dayOfWeek;

  String toHumanReadableString() {
    var text = "";
    if (seconds != null) {
      text += seconds!.toHumanReadableString("秒");

      if (!minutes.isWildcard) {
        text += " 过 ";
        text += minutes.toHumanReadableString("分钟");
      }
    } else {
      text += minutes.toHumanReadableString("分钟");
    }

    if (!hours.isWildcard) {
      text += " 过 ";
      text += hours.toHumanReadableString("小时");
    }

    if (!dayOfMonth.isWildcard) {
      text += " ";
      text += dayOfMonth.toHumanReadableString();
    }

    if (!month.isWildcard) {
      text += " 在 ";
      text += month.toHumanReadableString();
    }

    if (!dayOfWeek.isWildcard) {
      text += " ";
      text += dayOfWeek.toHumanReadableString();
    }

    return text.capitalize;
  }

  static CronExpression? parse(String expression) {
    final parts = expression.split(" ");
    if (parts.length < 5 || parts.length > 6) {
      return null;
    }

    final hasSeconds = parts.length == 6;
    final iter = parts.iterator;

    final seconds =
        hasSeconds ? SimpleField.parse(iter.nextOrNull, 0, 59) : null;
    final minutes = SimpleField.parse(iter.nextOrNull, 0, 59);
    final hours = SimpleField.parse(iter.nextOrNull, 0, 23);
    final dayOfMonth = DayOfMonthField.parse(iter.nextOrNull);
    final month = MonthField.parse(iter.nextOrNull);
    final dayOfWeek = DayOfWeekField.parse(iter.nextOrNull);

    if ((hasSeconds && seconds == null) ||
        minutes == null ||
        hours == null ||
        dayOfMonth == null ||
        month == null ||
        dayOfWeek == null) {
      return null;
    }

    return CronExpression(
      seconds,
      minutes,
      hours,
      dayOfMonth,
      month,
      dayOfWeek,
    );
  }
}

abstract class CronPart {
  const CronPart();

  bool valid(int min, int max) => true;

  static CronPart? parse(String value) {
    return WildcardPart.parse(value) ??
        ValuePart.parse(value) ??
        RangePart.parse(value) ??
        IncrementPart.parse(value);
  }
}

class WildcardPart extends CronPart {
  const WildcardPart();

  static WildcardPart? parse(String value) =>
      value == "*" || value == "?" ? const WildcardPart() : null;
}

class ValuePart extends CronPart {
  const ValuePart(this.value);
  final int value;

  @override
  bool valid(int min, int max) => value >= min && value <= max;

  static ValuePart? parse(String value) {
    final tryParse = int.tryParse(value);
    return tryParse == null ? null : ValuePart(tryParse);
  }
}

class RangePart extends CronPart {
  const RangePart(this.start, this.end);

  final ValuePart start;
  final ValuePart end;

  @override
  bool valid(int min, int max) => start.value >= min && end.value <= max;

  static RangePart? parse(String value) {
    final parts = value.split("-");
    if (parts.length != 2) {
      return null;
    }
    final start = ValuePart.parse(parts[0]);
    final end = ValuePart.parse(parts[1]);
    if (start == null || end == null) {
      return null;
    }
    return RangePart(start, end);
  }
}

class IncrementPart extends CronPart {
  const IncrementPart(this.part, this.increment);

  final CronPart part;
  final int increment;

  @override
  bool valid(int min, int max) =>
      part.valid(min, max) && increment > 0 && increment <= max;

  static IncrementPart? parse(String value) {
    final parts = value.split("/");
    if (parts.length != 2) {
      return null;
    }
    final part = CronPart.parse(parts[0]);
    final increment = int.tryParse(parts[1]);
    if (part == null || increment == null) {
      return null;
    }

    if (part is IncrementPart) {
      return null;
    }

    return IncrementPart(part, increment);
  }
}

class SimpleField {
  const SimpleField(this.parts);

  final List<CronPart> parts;

  bool get isWildcard => parts.length == 1 && parts[0] is WildcardPart;

  String humanReadablePart(String field, CronPart part) {
    if (part is WildcardPart) {
      return "每 $field";
    } else if (part is ValuePart) {
      return "$field ${part.value}";
    } else if (part is RangePart) {
      return "每 $field 从 ${part.start.value} 到 ${part.end.value}";
    } else if (part is IncrementPart) {
      final subPart = part.part;
      if (subPart is WildcardPart) {
        return "每 ${part.increment.ordinal} $field";
      } else if (subPart is ValuePart) {
        return "每 ${part.increment.ordinal} $field 从 $field ${subPart.value} 开始";
      } else if (subPart is RangePart) {
        return "每 ${part.increment.ordinal} $field 从 ${subPart.start.value} 到 ${subPart.end.value}";
      }
    }
    return "";
  }

  String toHumanReadableString(String field) {
    return parts.map((part) => humanReadablePart(field, part)).join(", 和 ");
  }

  static SimpleField? parse(String? value, int min, int max) {
    if (value == null) {
      return null;
    }
    final parts = value.split(",");
    final parsedParts = parts.map(CronPart.parse).toList();
    if (parsedParts.any((part) => part == null)) {
      return null;
    }

    final trueParts = parsedParts.whereType<CronPart>().toList();

    if (trueParts.any((part) => !part.valid(min, max))) {
      return null;
    }

    return SimpleField(trueParts);
  }
}

abstract class DayOfMonthField {
  const DayOfMonthField();

  bool get isWildcard => false;

  String toHumanReadableString();

  static DayOfMonthField? parse(String? value) {
    return LastNearestWeekdayOfMonthField.parse(value) ??
        LastDayOfMonthField.parse(value) ??
        NearestWeekdayOfMonthField.parse(value) ??
        SimpleDayOfMonthField.parse(value);
  }
}

class SimpleDayOfMonthField extends DayOfMonthField {
  const SimpleDayOfMonthField(this.parts);

  final List<CronPart> parts;

  @override
  bool get isWildcard => parts.length == 1 && parts[0] is WildcardPart;

  String _humanReadablePart(CronPart part) {
    if (part is WildcardPart) {
      return "每天";
    } else if (part is ValuePart) {
      return "在每月的第 ${part.value.ordinal} 天";
    } else if (part is RangePart) {
      return "每月从第 ${part.start.value.ordinal} 天到第 ${part.end.value.ordinal} 天";
    } else if (part is IncrementPart) {
      final subPart = part.part;
      if (subPart is WildcardPart) {
        return "每 ${part.increment.ordinal} 天";
      } else if (subPart is ValuePart) {
        return "每 ${part.increment.ordinal} 天 从第 ${subPart.value.ordinal} 天开始";
      } else if (subPart is RangePart) {
        return "每 ${part.increment.ordinal} 天 从第 ${subPart.start.value.ordinal} 天到第 ${subPart.end.value.ordinal} 天";
      }
    }
    return "";
  }

  @override
  String toHumanReadableString() {
    return parts.map(_humanReadablePart).join(", 和 ");
  }

  static SimpleDayOfMonthField? parse(String? value) {
    final simpleField = SimpleField.parse(value, 1, 31);
    if (simpleField == null) {
      return null;
    }
    return SimpleDayOfMonthField(simpleField.parts);
  }
}

class LastDayOfMonthField extends DayOfMonthField {
  const LastDayOfMonthField(this.part);

  final ValuePart? part;

  @override
  String toHumanReadableString() {
    if (part == null) {
      return "在每月的最后一天";
    }
    return "在每月倒数第 ${part!.value.ordinal} 天";
  }

  static LastDayOfMonthField? parse(String? value) {
    if (value == null) {
      return null;
    }
    if (value == "L") {
      return const LastDayOfMonthField(null);
    }
    if (!value.startsWith("L-")) {
      return null;
    }
    final part = ValuePart.parse(value.substring(2));
    if (part == null) {
      return null;
    }
    return LastDayOfMonthField(part);
  }
}

class NearestWeekdayOfMonthField extends DayOfMonthField {
  const NearestWeekdayOfMonthField(this.part);

  final ValuePart part;

  @override
  String toHumanReadableString() {
    return "在每月的第 ${part.value.ordinal} 天的最近工作日";
  }

  static NearestWeekdayOfMonthField? parse(String? value) {
    if (value == null) {
      return null;
    }
    if (!value.endsWith("W")) {
      return null;
    }
    final part = ValuePart.parse(value.substring(0, value.length - 1));
    if (part == null) {
      return null;
    }
    return NearestWeekdayOfMonthField(part);
  }
}

class LastNearestWeekdayOfMonthField extends DayOfMonthField {
  const LastNearestWeekdayOfMonthField();

  @override
  String toHumanReadableString() {
    return "在每月最后一天的最近工作日";
  }

  static LastNearestWeekdayOfMonthField? parse(String? value) =>
      value == "LW" ? const LastNearestWeekdayOfMonthField() : null;
}

class MonthField {
  const MonthField(this.parts);

  final List<CronPart> parts;

  static const _monthNames = [
    "一月",
    "二月",
    "三月",
    "四月",
    "五月",
    "六月",
    "七月",
    "八月",
    "九月",
    "十月",
    "十一月",
    "十二月",
  ];

  bool get isWildcard => parts.length == 1 && parts[0] is WildcardPart;

  static String? _replaceMonth(String? value) {
    return value
        ?.replaceAll("JAN", "1")
        .replaceAll("FEB", "2")
        .replaceAll("MAR", "3")
        .replaceAll("APR", "4")
        .replaceAll("MAY", "5")
        .replaceAll("JUN", "6")
        .replaceAll("JUL", "7")
        .replaceAll("AUG", "8")
        .replaceAll("SEP", "9")
        .replaceAll("OCT", "10")
        .replaceAll("NOV", "11")
        .replaceAll("DEC", "12");
  }

  String _humanReadablePart(CronPart part) {
    if (part is WildcardPart) {
      return "每个月";
    } else if (part is ValuePart) {
      return _monthNames[part.value - 1];
    } else if (part is RangePart) {
      return "每月从 ${_monthNames[part.start.value - 1]} 到 ${_monthNames[part.end.value - 1]}";
    } else if (part is IncrementPart) {
      final subPart = part.part;
      if (subPart is WildcardPart) {
        return "每 ${part.increment.ordinal} 个月";
      } else if (subPart is ValuePart) {
        return "每 ${part.increment.ordinal} 个月 从 ${_monthNames[subPart.value - 1]} 开始";
      } else if (subPart is RangePart) {
        return "每 ${part.increment.ordinal} 个月 从 ${_monthNames[subPart.start.value - 1]} 到 ${_monthNames[subPart.end.value - 1]}";
      }
    }
    return "";
  }

  String toHumanReadableString() {
    return parts.map(_humanReadablePart).join(", 和 ");
  }

  static MonthField? parse(String? value) {
    final simpleField = SimpleField.parse(_replaceMonth(value), 1, 12);
    if (simpleField == null) {
      return null;
    }
    return MonthField(simpleField.parts);
  }
}

abstract class DayOfWeekField {
  const DayOfWeekField();

  bool get isWildcard => false;

  String toHumanReadableString();

  static DayOfWeekField? parse(String? value) {
    return LastDayOfWeekField.parse(value) ??
        NthDayOfWeekField.parse(value) ??
        SimpleDayOfWeekField.parse(value);
  }
}

class SimpleDayOfWeekField extends DayOfWeekField {
  const SimpleDayOfWeekField(this.parts);

  final List<CronPart> parts;

  static const _dayNames = [
    "星期一",
    "星期二",
    "星期三",
    "星期四",
    "星期五",
    "星期六",
    "星期天",
  ];

  @override
  bool get isWildcard => parts.length == 1 && parts[0] is WildcardPart;

  static String? _replaceDayOfWeek(String? value) {
    return value
        ?.replaceAll("MON", "1")
        .replaceAll("TUE", "2")
        .replaceAll("WED", "3")
        .replaceAll("THU", "4")
        .replaceAll("FRI", "5")
        .replaceAll("SAT", "6")
        .replaceAll("SUN", "7");
  }

  String _humanReadablePart(CronPart part) {
    if (part is WildcardPart) {
      return "每周的每一天";
    } else if (part is ValuePart) {
      return "在 ${_dayNames[part.value - 1]}";
    } else if (part is RangePart) {
      return "从 ${_dayNames[part.start.value - 1]} 到 ${_dayNames[part.end.value - 1]}";
    } else if (part is IncrementPart) {
      final subPart = part.part;
      if (subPart is WildcardPart) {
        return "每 ${part.increment.ordinal} 天";
      } else if (subPart is ValuePart) {
        return "每 ${part.increment.ordinal} 天 从 ${_dayNames[subPart.value - 1]} 开始";
      } else if (subPart is RangePart) {
        return "每 ${part.increment.ordinal} 天 从 ${_dayNames[subPart.start.value - 1]} 到 ${_dayNames[subPart.end.value - 1]}";
      }
    }
    return "";
  }

  @override
  String toHumanReadableString() {
    return parts.map(_humanReadablePart).join(", 和 ");
  }

  static SimpleDayOfWeekField? parse(String? value) {
    final simpleField = SimpleField.parse(_replaceDayOfWeek(value), 1, 7);
    if (simpleField == null) {
      return null;
    }
    return SimpleDayOfWeekField(simpleField.parts);
  }
}

class LastDayOfWeekField extends DayOfWeekField {
  const LastDayOfWeekField();

  @override
  String toHumanReadableString() {
    return "在星期天";
  }

  static LastDayOfWeekField? parse(String? value) {
    if (value == null) {
      return null;
    }
    if (value != "L") {
      return null;
    }
    return const LastDayOfWeekField();
  }
}

class NthDayOfWeekField extends DayOfWeekField {
  const NthDayOfWeekField(this.part, this.nth);

  final ValuePart part;
  final ValuePart nth;

  static const _dayNames = [
    "星期一",
    "星期二",
    "星期三",
    "星期四",
    "星期五",
    "星期六",
    "星期天",
  ];

  @override
  String toHumanReadableString() {
    return "在每月的第 ${nth.value.ordinal} 个 ${_dayNames[part.value - 1]}";
  }

  static NthDayOfWeekField? parse(String? value) {
    if (value == null) {
      return null;
    }
    if (!value.contains("#")) {
      return null;
    }
    final parts = value.split("#");
    if (parts.length != 2) {
      return null;
    }
    final part = ValuePart.parse(parts[0]);
    final nth = ValuePart.parse(parts[1]);
    if (part == null || nth == null) {
      return null;
    }

    if (!part.valid(0, 7) || !nth.valid(1, 5)) {
      return null;
    }

    return NthDayOfWeekField(part, nth);
  }
}
