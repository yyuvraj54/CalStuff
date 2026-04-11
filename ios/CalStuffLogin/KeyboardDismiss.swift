import SwiftUI
import UIKit

enum KeyboardDismiss {
    static func endEditing() {
        UIApplication.shared.sendAction(
            #selector(UIResponder.resignFirstResponder),
            to: nil,
            from: nil,
            for: nil
        )
    }
}

extension View {
    /// Phone/number pads have no "Return" — adds a **Done** bar above the keyboard.
    func keyboardDoneButton(title: String = "Done") -> some View {
        toolbar {
            ToolbarItemGroup(placement: .keyboard) {
                Spacer()
                Button(title) {
                    KeyboardDismiss.endEditing()
                }
            }
        }
    }
}
