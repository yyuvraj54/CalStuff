import SwiftUI
import UIKit

/// Forces a specific status bar icon style (light/dark) for whichever screen applies it —
/// SwiftUI has no direct API for this, so a zero-sized child view controller is embedded to
/// override `preferredStatusBarStyle` (a standard, well-known technique; the embedded controller
/// becomes a real child of the hosting controller, so its override participates in the normal
/// UIKit status-bar-style resolution chain).
private struct StatusBarStyleHost: UIViewControllerRepresentable {
    let style: UIStatusBarStyle

    func makeUIViewController(context: Context) -> StatusBarStyleViewController {
        StatusBarStyleViewController(style: style)
    }

    func updateUIViewController(_ uiViewController: StatusBarStyleViewController, context: Context) {
        uiViewController.style = style
    }
}

private final class StatusBarStyleViewController: UIViewController {
    var style: UIStatusBarStyle {
        didSet { setNeedsStatusBarAppearanceUpdate() }
    }

    init(style: UIStatusBarStyle) {
        self.style = style
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    override var preferredStatusBarStyle: UIStatusBarStyle { style }
}

extension View {
    /// Use on a screen with a full-bleed colored header (e.g. Profile's orange gradient) where
    /// the default dark status bar icons don't have enough contrast.
    func statusBarStyle(_ style: UIStatusBarStyle) -> some View {
        background(StatusBarStyleHost(style: style).frame(width: 0, height: 0))
    }
}
