package taskmanager.ui;

import config.Config;

import java.awt.FontMetrics;

public class TextUtils {
	private static final String[] PREFIXES = {"", "K", "M", "G", "T", "P"};

	public enum ValueType {
		Percentage,
		Bytes,
		BytesPerSecond,
		Bits,
		BitsPerSecond,
		Millis,
		TimeFull,
		Time,
		Raw
	}

	public static String valueToString(long value, ValueType type) {
		if (type == ValueType.Percentage) {
			return String.format("%.1f%%", 100 * value / (double) Config.DOUBLE_TO_LONG);
		} else if (type == ValueType.Bytes) {
			return bytesToString(value);
		} else if (type == ValueType.BytesPerSecond) {
			return bytesToString(value) + "/s";
		} else if (type == ValueType.Bits) {
			return bitsToString(value);
		} else if (type == ValueType.BitsPerSecond) {
			return bitsToString(value) + "ps";
		} else if (type == ValueType.Millis) {
			return value + "ms";
		} else if (type == ValueType.TimeFull) {
			long seconds = value % 60;
			long minutes = (value / 60) % 60;
			long hours = ((value / 60) / 60) % 24;
			long days = ((value / 60) / 60) / 24;
			if (days > 0)
				return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
			return String.format("%02d:%02d:%02d", hours, minutes, seconds);
		} else if (type == ValueType.Time) {
			double trueValue = value / (double) Config.DOUBLE_TO_LONG;
			if (trueValue > 60*60*24) {
				return String.format("%.1f d", trueValue / (60 * 60 * 24));
			} else if (trueValue > 60*60) {
				return String.format("%.1f h", trueValue / (60 * 60));
			} else if (trueValue > 60) {
				return String.format("%.1f m", trueValue / 60);
			} else {
				return String.format("%.1f s", trueValue);
			}
		} else if (type == ValueType.Raw) {
			return Long.toString(value);
		}
		throw new UnsupportedOperationException("Not implemented for: " + type);
	}

	public static String ratioToString(long value1, long value2, ValueType type) {
		if (type == ValueType.Bytes) {
			int factor = Math.max(getFactor(value1), getFactor(value2));
			return applyFactor(value1, factor, 1) + "/" + applyFactor(value2, factor, 1) + " " + PREFIXES[factor] + "B";
		} else if (type == ValueType.Raw) {
			return value1 + "/" + value2;
		}
		throw new UnsupportedOperationException("Not implemented for: " + type);
	}


	public static String bytesToString(long valueInBytes) {
		return bytesToString(valueInBytes, 1);
	}

	public static String bytesToString(long valueInBytes, int decimals) {
		int factor = getFactor(valueInBytes);
		return applyFactor(valueInBytes, factor, decimals) + " " + PREFIXES[factor] + "B";
	}

	public static String bitsToString(long valueInBytes) {
		return bitsToString(valueInBytes, 1);
	}

	public static String bitsToString(long valueInBytes, int decimals) {
		valueInBytes *= 8;
		int factor = getFactor(valueInBytes);
		return applyFactor(valueInBytes, factor, decimals) + " " + PREFIXES[factor] + "b";
	}

	private static int getFactor(long value) {
		int factor = 0;
		while (value > 1024) {
			value /= 1024;
			factor++;
		}

		return factor;
	}

	private static String applyFactor(long value, int factor, int decimals) {
		for (int i = 0; i < factor - 1; i++) {
			value /= 1024;
		}
		if (factor > 0) {
			return String.format("%." + decimals + "f", value / 1024f);
		}
		return String.format("%d", value);
	}


	public static String limitWidth(String text, int maxWidth, FontMetrics metrics) {
		if (metrics.stringWidth(text) < maxWidth) {
			return text;
		}

		for (int i = text.length()-1; i >= 0; i--) {
			if (metrics.stringWidth(text.substring(0, i) + "...") < maxWidth) {
				return text.substring(0, i) + "...";
			}
		}

		return "...";
	}
}
