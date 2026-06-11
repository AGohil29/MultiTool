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
    @State var authState: AuthState = AuthState.Loading()
    @State private var userName: String = "Waiting..."
    private let repository = MainRepository()
    
    var body: some View {
        VStack {
            Text("I am a Native Swift Label")
                .font(.headline)
                .foregroundStyle(.white)
                .padding()
            
            Text(userName)
                .font(.largeTitle)
                .fontWeight(.bold)
            
//            switch onEnum(of: authState) {
//            case .loading:
//                ProgressView("Fetching data...")
//                    .padding()
//            case .authenticated(let data):
//                Text("Wecome, \(data.userName)")
//                    .font(.title)
//                    .padding()
//            case .error(let error):
//                Text("Error, \(error.message)")
//                    .foregroundColor(.red)
//                    .padding()
//            }
//            
//            Button(action: {
//                // This simulates the Kotlin logic returning a success state
//                authState = AuthState.Authenticated(userName: "Arun", token: "xyz123")
//            }) {
//                Text("Simulate Login Success")
//                    .padding()
//                    .background(Color.green)
//                    .foregroundColor(.white)
//                    .cornerRadius(8)
//            }
//            
//            Button("Simulate Error") {
//                authState = AuthState.Error(message: "Invalid Credentials")
//            }
            
            // --- DEVELOPER DEBUG TOOL ---
            Button(action: {
                // Calls your shared expect/actual object directly from Swift
                GarbageCollector.shared.forceCollect()
            }) {
                HStack {
                    Image(systemName: "trash.fill")
                    Text("Force Kotlin GC Sweep")
                }
                .padding()
                .background(Color.orange)
                .foregroundColor(.white)
                .cornerRadius(8)
            }
            .padding(.top, 10)
            
            ComposeView(someText: "Hello from Swift")
                .ignoresSafeArea(.all)
        }
        .task {
            // This task starts when the view appears and cancels when it disappears
            do {
                for await user in repository.userFlow {
                    self.userName = user.name
                    print("Received update from Kotlin: \(user.name)")
                }
            } catch {
                print("Flow error: \(error)")
                self.userName = "Connection Lost..."
            }
        }
        .onAppear {
            // Trigger the Kotlin suspend function to start updates
            Task {
                try? await repository.startUpdating()
            }
        }
        
    }
}



