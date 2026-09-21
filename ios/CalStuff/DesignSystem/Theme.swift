import SwiftUI

private struct CalStuffColorsKey: EnvironmentKey {
    static let defaultValue = CalStuffColors.light
}

extension EnvironmentValues {
    var calStuffColors: CalStuffColors {
        get { self[CalStuffColorsKey.self] }
        set { self[CalStuffColorsKey.self] = newValue }
    }
}

extension View {
    /// Mirrors Android's `CalStuffTheme(darkTheme:)` — applies the full semantic token set to
    /// this view's environment based on the resolved theme.
    func calStuffTheme(darkTheme: Bool) -> some View {
        environment(\.calStuffColors, darkTheme ? .dark : .light)
    }
}
