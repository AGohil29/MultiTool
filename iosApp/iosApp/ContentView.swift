import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    let someText: String
    
    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.MainViewController(someText: someText)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        VStack {
            Text("I am a Native Swift Label")
                .font(.headline)
                .foregroundStyle(.white)
                .padding()
            
            ComposeView(someText: "Hello from Swift")
                .ignoresSafeArea(.all)
        }
        
    }
}



