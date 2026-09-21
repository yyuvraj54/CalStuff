import UIKit
import FirebaseCore
import FirebaseAuth

/// Only exists so FirebaseAuth's phone-auth silent-push verification has a delegate to forward
/// APNs callbacks to — the app is otherwise pure SwiftUI lifecycle (see `CalStuffApp.swift`).
/// Without this, FirebaseAuth logs a warning on every launch and always falls back to the
/// reCAPTCHA/URL-scheme verification path.
///
/// `FirebaseApp.configure()` lives here (not in `CalStuffApp.init()`) because FirebaseAuth's
/// notification-forwarding self-check runs a probe against `UIApplication.shared.delegate`
/// immediately once configured — that delegate isn't installed yet during the SwiftUI `App`
/// struct's own `init()`, only by the time UIKit calls `didFinishLaunchingWithOptions` here.
final class AppDelegate: NSObject, UIApplicationDelegate {
    // Built here rather than as a `lazy var` on the `CalStuffApp` struct: `App.body` is a
    // non-mutating getter, so a struct-owned `lazy var` can't be initialized from inside it.
    // AppDelegate is a class, so it can freely construct this once Firebase is configured below.
    private(set) var dependencies: AppDependencies!

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        FirebaseApp.configure()
        dependencies = AppDependencies()
        #if !targetEnvironment(simulator)
        // Simulators hand back a local fake push token, which tricks Firebase into attempting
        // silent-push verification it can never actually receive — better to skip straight to
        // the reCAPTCHA/URL-scheme fallback there instead of registering at all.
        application.registerForRemoteNotifications()
        #endif
        return true
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        Auth.auth().setAPNSToken(deviceToken, type: .unknown)
    }

    func application(
        _ application: UIApplication,
        didReceiveRemoteNotification notification: [AnyHashable: Any],
        fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void
    ) {
        if Auth.auth().canHandleNotification(notification) {
            completionHandler(.noData)
            return
        }
        completionHandler(.newData)
    }
}
