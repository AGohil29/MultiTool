//
//  SwiftPaymentService.swift
//  iosApp
//
//  Created by Arun Gohil on 29/04/26.
//
import Foundation
import ComposeApp

class SwiftPaymentService {
    // 1. We wire this up in AppDelegate or at startup
    func setupBridge(bridge: IOSPaymentGateway) {
        bridge.provideImplementation { amount, completion in
            self.executePayment(amount: Double(truncating: amount)) { result in
                completion(result)
            }
        }
    }
    
    // 2. Pure Swift logic - no @objc or protocols needed
    private func executePayment(amount: Double, completion: @escaping (PaymentResult) -> Void) {
        print("Processing Swift-native payment: \(amount)")
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            // Simply return the Kotlin result
            completion(PaymentResult.Success())
        }
    }
}
