import Foundation
import os

/// Minimal logging facade — mirrors the intent of Android's `AppLogger` (core-logging module)
/// without pulling in a dependency; wraps `os_log` so output shows up in Console.app/Xcode.
enum AppLog {
    private static let logger = Logger(subsystem: "com.dusht.calstuff.ios", category: "app")

    static func error(_ message: String, _ error: Error) {
        logger.error("\(message): \(error.localizedDescription)")
    }

    static func info(_ message: String) {
        logger.info("\(message)")
    }
}
