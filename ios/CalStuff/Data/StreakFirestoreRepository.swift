import Foundation
import FirebaseAuth
import FirebaseFirestore
import CalStuffShared

/// Firestore-backed `StreakRepository` — `users/{uid}/streaks/current`. The increment/reset math
/// itself lives in shared `StreakLogic.next(existing:todayDate:)` (commonMain), so Android and iOS
/// compute identical results from one place — this class only owns the Firestore read/write.
final class StreakFirestoreRepository: StreakRepository {

    private let firestore = Firestore.firestore()
    private var listener: ListenerRegistration?
    private var streakFlow: (any MutableStateFlow)?

    private func currentUserId() -> String? {
        Auth.auth().currentUser?.uid
    }

    private func streakDoc(uid: String) -> DocumentReference {
        firestore.collection("users").document(uid).collection("streaks").document("current")
    }

    func observeStreak() -> any Flow {
        if let existing = streakFlow {
            return existing
        }
        let flow = StateFlowKt.MutableStateFlow(value: StreakData(currentStreak: 0, longestStreak: 0, lastLoggedDate: ""))
        streakFlow = flow

        guard let uid = currentUserId() else {
            return flow
        }
        listener?.remove()
        listener = streakDoc(uid: uid).addSnapshotListener { snapshot, error in
            if let error {
                AppLog.error("StreakFirestoreRepository.observeStreak", error)
                return
            }
            guard let data = snapshot?.data(), snapshot?.exists == true else {
                flow.setValue(StreakData(currentStreak: 0, longestStreak: 0, lastLoggedDate: ""))
                return
            }
            flow.setValue(Self.streak(from: data))
        }
        return flow
    }

    func updateStreak(todayDate: String) async throws -> Any? {
        guard let uid = currentUserId() else {
            throw NSError(domain: "CalStuff", code: 10, userInfo: [NSLocalizedDescriptionKey: "Not authenticated"])
        }
        let ref = streakDoc(uid: uid)
        let snapshot = try await ref.getDocument()
        let existing = snapshot.data().map(Self.streak(from:)) ?? StreakData(currentStreak: 0, longestStreak: 0, lastLoggedDate: "")
        let updated = StreakLogic.shared.next(existing: existing, todayDate: todayDate)
        if updated == existing {
            return updated
        }
        try await ref.setData(Self.dict(from: updated))
        return updated
    }

    deinit {
        listener?.remove()
    }

    private static func streak(from data: [String: Any]) -> StreakData {
        StreakData(
            currentStreak: (data["currentStreak"] as? NSNumber)?.int32Value ?? 0,
            longestStreak: (data["longestStreak"] as? NSNumber)?.int32Value ?? 0,
            lastLoggedDate: data["lastLoggedDate"] as? String ?? ""
        )
    }

    private static func dict(from streak: StreakData) -> [String: Any] {
        [
            "currentStreak": streak.currentStreak,
            "longestStreak": streak.longestStreak,
            "lastLoggedDate": streak.lastLoggedDate,
            "updatedAt": Int64(Date().timeIntervalSince1970 * 1000),
        ]
    }
}
