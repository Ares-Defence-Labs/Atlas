import SwiftUI
import shared
import BottomSheet

//@AtlasSwiftTab(CoreDashboardTabViewModel::class, position = 0, holder = TabParentViewModel::class)
struct ContentView: View {
    let vm: CoreDashboardTabViewModel
    
    let sample = SheetTransitioningDelegate()
    var body: some View {
        Text("Dashboard Screen")
            .background(Color.red)
            .containerShape(Rectangle())
            .frame(maxWidth: .infinity, maxHeight: 50).onTapGesture {
                vm.openDroidScreen()
            }
    }
}

class BottomSheetHostController {
    static let shared = BottomSheetHostController()
    private var currentSheet: UIViewController?

    func present(sheetID: String, params: [String: Any]) {
        dismiss(animated: false)

        let newSheet = UIHostingController(rootView: sheetContent(for: sheetID, params: params))
        newSheet.modalPresentationStyle = .pageSheet
        newSheet.isModalInPresentation = true

        if let sheet = newSheet.sheetPresentationController {
            sheet.detents = [.medium()]
            sheet.preferredCornerRadius = 24
            sheet.prefersGrabberVisible = false
            sheet.largestUndimmedDetentIdentifier = .medium
        }

        UIApplication.topViewController?.present(newSheet, animated: true)
        currentSheet = newSheet
    }

    func dismiss(animated: Bool = true) {
        if let currentSheet = currentSheet {
            currentSheet.dismiss(animated: animated)
            self.currentSheet = nil
        }
    }

    private func sheetContent(for sheetID: String, params: [String: Any]) -> some View {
        switch sheetID {
        case "mySheet":
            return AnyView(Text("Hello there my friend"))
        default:
            return AnyView(Text("Unknown sheet"))
        }
    }
}

class PresentationCoordinator {
    static let shared = PresentationCoordinator()

    private var isPresenting = false
    private var presentationQueue: [() -> Void] = []

    func enqueue(_ presentation: @escaping () -> Void) {
        DispatchQueue.main.async {
            self.presentationQueue.append(presentation)
            self.tryPresentNext()
        }
    }

    private func tryPresentNext() {
        guard !isPresenting, !presentationQueue.isEmpty else { return }

        isPresenting = true
        let next = presentationQueue.removeFirst()
        next()
    }

    func markPresentationComplete() {
        isPresenting = false
        tryPresentNext()
    }
}






