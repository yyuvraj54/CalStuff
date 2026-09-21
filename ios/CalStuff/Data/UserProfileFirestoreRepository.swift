import Foundation
import FirebaseAuth
import FirebaseFirestore
import CalStuffShared

/// Firestore-backed `UserProfileRepository` — path `users/{uid}` (same document Android writes
/// via `UserProfileRepositoryImpl`/`UserProfileDto`).
///
/// Deliberately simpler than Android's implementation: Android hand-rolls a Room cache with
/// manual `pendingSync`/conflict-resolution bookkeeping because it never uses Firestore realtime
/// listeners. The Firebase iOS SDK gives disk persistence + offline writes + live updates for
/// free, so this uses a real `addSnapshotListener` instead — same Firestore documents and field
/// names, a more idiomatic implementation for this platform. See ios/CHANGES.md.
final class UserProfileFirestoreRepository: UserProfileRepository {

    private let firestore = Firestore.firestore()
    private let displayNameStore: DisplayNameStore
    private var profileListener: ListenerRegistration?
    private var profileFlow: (any MutableStateFlow)?

    init(displayNameStore: DisplayNameStore) {
        self.displayNameStore = displayNameStore
    }

    func currentUserId() -> String? {
        Auth.auth().currentUser?.uid
    }

    func currentPhoneNumber() -> String? {
        Auth.auth().currentUser?.phoneNumber
    }

    func observeProfile() -> any Flow {
        if let existing = profileFlow {
            return existing
        }
        let flow = StateFlowKt.MutableStateFlow(value: nil)
        profileFlow = flow

        guard let uid = currentUserId() else {
            return flow
        }
        profileListener?.remove()
        profileListener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error in
                if let error {
                    AppLog.error("UserProfileFirestoreRepository.observeProfile", error)
                    return
                }
                guard let data = snapshot?.data(), snapshot?.exists == true else {
                    flow.setValue(nil)
                    return
                }
                flow.setValue(Self.profile(from: data, uid: uid))
            }
        return flow
    }

    func getProfile() async throws -> UserProfile? {
        guard let uid = currentUserId() else { return nil }
        let snapshot = try await firestore.collection("users").document(uid).getDocument()
        guard let data = snapshot.data(), snapshot.exists else { return nil }
        return Self.profile(from: data, uid: uid)
    }

    func saveProfile(profile: UserProfile) async throws -> Any? {
        guard let uid = currentUserId() else {
            throw NSError(domain: "CalStuff", code: 10, userInfo: [NSLocalizedDescriptionKey: "Not authenticated"])
        }
        // Cache locally before the network call, not after — mirrors Android's
        // UserProfileRepositoryImpl.saveProfile, which caches the display name even when the
        // Firestore write fails, so Profile's fast-path name read survives being offline.
        displayNameStore.set(name: profile.name)
        try await firestore.collection("users").document(uid).setData(Self.dictionary(from: profile))
        return nil
    }

    deinit {
        profileListener?.remove()
    }

    // MARK: - Mapping (mirrors Android's UserProfileDto <-> UserProfile)

    private static func profile(from data: [String: Any], uid: String) -> UserProfile {
        UserProfile(
            uid: uid,
            name: string(data, "name"),
            age: int32(data, "age"),
            heightCm: float(data, "heightCm"),
            weightKg: float(data, "weightKg"),
            gender: string(data, "gender"),
            activityLevel: string(data, "activityLevel"),
            dailyCalorieGoal: int32(data, "dailyCalorieGoal", default: 2000),
            createdAt: int64(data, "createdAt"),
            updatedAt: int64(data, "updatedAt"),
            phoneNumber: string(data, "phoneNumber")
        )
    }

    private static func dictionary(from profile: UserProfile) -> [String: Any] {
        [
            "name": profile.name,
            "age": profile.age,
            "heightCm": profile.heightCm,
            "weightKg": profile.weightKg,
            "gender": profile.gender,
            "activityLevel": profile.activityLevel,
            "dailyCalorieGoal": profile.dailyCalorieGoal,
            "createdAt": profile.createdAt,
            "updatedAt": profile.updatedAt,
            "phoneNumber": profile.phoneNumber,
        ]
    }

    private static func string(_ data: [String: Any], _ key: String) -> String {
        data[key] as? String ?? ""
    }

    private static func int32(_ data: [String: Any], _ key: String, default def: Int32 = 0) -> Int32 {
        ((data[key] as? NSNumber)?.int32Value) ?? def
    }

    private static func int64(_ data: [String: Any], _ key: String) -> Int64 {
        ((data[key] as? NSNumber)?.int64Value) ?? 0
    }

    private static func float(_ data: [String: Any], _ key: String) -> Float {
        ((data[key] as? NSNumber)?.floatValue) ?? 0
    }
}
