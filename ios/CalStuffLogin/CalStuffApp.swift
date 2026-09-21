//
//  CalStuffApp.swift
//  CalStuff
//

import SwiftUI
import FirebaseAnalytics
import FirebaseAuth

@main
struct CalStuffApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    var body: some Scene {
        WindowGroup {
            RootView(dependencies: appDelegate.dependencies)
                .onOpenURL { url in
                    if Auth.auth().canHandle(url) { return }
                    _ = FirebaseAuthService.handleGoogleURL(url)
                }
        }
    }
}
