import SwiftUI
import CalStuffShared

/// Mirrors Android's `ProfileScreen` — gradient header, a standalone Theme card (accordion
/// selector for Light/Dark/System, matching the just-built Android pattern), then a second card
/// for Personal Info / About / Log Out.
struct ProfileView: View {
    @StateObject private var viewModel: ProfileViewModel
    @ObservedObject private var rootViewModel: RootViewModel
    @Environment(\.calStuffColors) private var colors
    @Environment(\.dismiss) private var dismiss
    let onLoggedOut: () -> Void

    init(dependencies: AppDependencies, rootViewModel: RootViewModel, onLoggedOut: @escaping () -> Void) {
        _viewModel = StateObject(wrappedValue: ProfileViewModel(dependencies: dependencies))
        self.rootViewModel = rootViewModel
        self.onLoggedOut = onLoggedOut
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                header
                ThemeSectionCard(rootViewModel: rootViewModel)
                sectionsCard
            }
            .padding(.bottom, 32)
        }
        .background(colors.background.ignoresSafeArea())
        .statusBarStyle(.lightContent)
        .navigationBarBackButtonHidden(true)
        .toolbar(.hidden, for: .navigationBar)
        .alert("Log Out", isPresented: $viewModel.showLogoutDialog) {
            Button("Cancel", role: .cancel) { viewModel.logoutDismissed() }
            Button("Yes, Log Out", role: .destructive) { viewModel.logoutConfirmed(onLoggedOut: onLoggedOut) }
        } message: {
            Text("Are you sure you want to log out of CalStuff?")
        }
    }

    private var header: some View {
        ZStack(alignment: .topLeading) {
            LinearGradient(colors: [colors.gradientStart, colors.gradientEnd], startPoint: .topLeading, endPoint: .bottomTrailing)
                .ignoresSafeArea(edges: .top)

            VStack(alignment: .leading, spacing: 8) {
                Button(action: { dismiss() }) {
                    Image(systemName: "chevron.left")
                        .font(.body.weight(.semibold))
                        .foregroundStyle(colors.onGradient)
                        .frame(width: 36, height: 36)
                        .background(Color.black.opacity(0.12))
                        .clipShape(Circle())
                }
                .padding(.bottom, 4)

                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Profile")
                            .font(.largeTitle.bold())
                            .foregroundStyle(colors.onGradient)
                        Text("Member since \(viewModel.memberSinceText)")
                            .font(.subheadline.weight(.medium))
                            .foregroundStyle(colors.onGradientMuted)
                    }
                    Spacer()
                    ZStack {
                        Circle().fill(colors.accentSoft).frame(width: 64, height: 64)
                        Text(initials(for: viewModel.userName))
                            .font(.title2.bold())
                            .foregroundStyle(colors.onAccentSoft)
                    }
                }
            }
            .padding(24)
            .padding(.top, 24)
        }
        .clipShape(UnevenRoundedRectangle(bottomLeadingRadius: 32, bottomTrailingRadius: 32))
    }

    private var sectionsCard: some View {
        VStack(spacing: 0) {
            ProfileRow(icon: "person", title: "Personal Info", subtitle: "Name, age, goals", tint: colors.accent, showChevron: true) {}
            SectionDivider()
            ProfileRow(icon: "info.circle", title: "About CalStuff", subtitle: "Version, licenses, credits", tint: colors.accent, showChevron: true) {}
            SectionDivider()
            ProfileRow(icon: "rectangle.portrait.and.arrow.right", title: "Log Out", subtitle: "Sign out of your account", tint: colors.error, showChevron: false, titleColor: colors.error) {
                viewModel.logoutTapped()
            }
        }
        .background(colors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
        .padding(.horizontal, 16)
    }

    private func initials(for name: String) -> String {
        let words = name.trimmingCharacters(in: .whitespaces).split(separator: " ")
        if words.count >= 2 { return "\(words[0].prefix(1))\(words[1].prefix(1))".uppercased() }
        if let first = words.first { return String(first.prefix(1)).uppercased() }
        return ""
    }
}

private struct ProfileRow: View {
    let icon: String
    let title: String
    let subtitle: String
    let tint: Color
    let showChevron: Bool
    var titleColor: Color?
    let action: () -> Void
    @Environment(\.calStuffColors) private var colors

    var body: some View {
        Button(action: action) {
            HStack(spacing: 14) {
                ZStack {
                    RoundedRectangle(cornerRadius: 12).fill(tint.opacity(0.12)).frame(width: 40, height: 40)
                    Image(systemName: icon).foregroundStyle(tint)
                }
                VStack(alignment: .leading, spacing: 2) {
                    Text(title).fontWeight(.semibold).foregroundStyle(titleColor ?? colors.textPrimary)
                    Text(subtitle).font(.caption).foregroundStyle(colors.textSecondary)
                }
                Spacer()
                if showChevron {
                    Image(systemName: "chevron.right").foregroundStyle(colors.textSecondary)
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
        }
        .buttonStyle(.plain)
    }
}

private struct SectionDivider: View {
    @Environment(\.calStuffColors) private var colors
    var body: some View {
        Divider().overlay(colors.divider).padding(.horizontal, 20)
    }
}

/// Standalone Theme card — mirrors Android's separated `ThemeSectionCard` (its own card, above
/// the Personal Info / About / Log Out card, not merged into it).
private struct ThemeSectionCard: View {
    @ObservedObject var rootViewModel: RootViewModel
    @State private var expanded = false
    @Environment(\.calStuffColors) private var colors

    var body: some View {
        VStack(spacing: 0) {
            Button(action: { withAnimation { expanded.toggle() } }) {
                HStack(spacing: 14) {
                    ZStack {
                        RoundedRectangle(cornerRadius: 12).fill(colors.accent.opacity(0.12)).frame(width: 40, height: 40)
                        Image(systemName: "moon.fill").foregroundStyle(colors.accent)
                    }
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Theme").fontWeight(.semibold).foregroundStyle(colors.textPrimary)
                        Text(label(for: rootViewModel.themeMode)).font(.caption).foregroundStyle(colors.textSecondary)
                    }
                    Spacer()
                    Image(systemName: expanded ? "chevron.up" : "chevron.down").foregroundStyle(colors.textSecondary)
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 16)
            }
            .buttonStyle(.plain)

            if expanded {
                VStack(spacing: 0) {
                    themeOption(.light, label: "Light")
                    themeOption(.dark, label: "Dark")
                    themeOption(.system, label: "System")
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 12)
            }
        }
        .background(colors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
        .padding(.horizontal, 16)
    }

    private func themeOption(_ mode: ThemeMode, label: String) -> some View {
        let isSelected = rootViewModel.themeMode == mode
        return Button(action: {
            rootViewModel.setThemeMode(mode)
            withAnimation { expanded = false }
        }) {
            HStack {
                Text(label)
                    .fontWeight(isSelected ? .semibold : .regular)
                    .foregroundStyle(isSelected ? colors.onAccentSoft : colors.textSecondary)
                Spacer()
                if isSelected {
                    Image(systemName: "checkmark").foregroundStyle(colors.onAccentSoft)
                }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 12)
            .background(isSelected ? colors.accentSoft : Color.clear)
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        }
        .buttonStyle(.plain)
    }

    private func label(for mode: ThemeMode) -> String {
        switch mode {
        case .light: return "Light"
        case .dark: return "Dark"
        default: return "System"
        }
    }
}
