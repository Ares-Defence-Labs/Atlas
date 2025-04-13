package com.architect.atlas.navigationEngine.tasks.routingEngine

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
abstract class SwiftUIFlowsGeneratorTask : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputIosDir: DirectoryProperty

    @get:Input
    abstract var projectCoreName: String

    init {
        group = "AtlasNavigation"
        description = "Generates the platform-specific navigation engine implementations."
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun generateNavigatorClass() {
        generateIOSSwiftBridge()
    }

    private fun generateIOSSwiftBridge() {
        val swiftBridge = buildString {
            appendLine("import Foundation")
            appendLine("import SwiftUI")
            appendLine("import Combine")
            appendLine("import $projectCoreName")
            appendLine()

            appendLine(
                """
                    protocol KMPConvertible {
                        associatedtype KotlinType
                        static func fromKotlin(_ value: KotlinType) -> Self
                        func toKotlin() -> KotlinType
                    }
                    
                    extension String: KMPConvertible {
                        typealias KotlinType = NSString
                        static func fromKotlin(_ value: NSString) -> String {
                            value as String
                        }
                        func toKotlin() -> NSString {
                            self as NSString
                        }
                    }

                    extension Bool: KMPConvertible {
                        typealias KotlinType = NSNumber
                        static func fromKotlin(_ value: NSNumber) -> Bool {
                            value.boolValue
                        }
                        func toKotlin() -> NSNumber {
                            self as NSNumber
                        }
                    }

                    extension Int: KMPConvertible {
                        typealias KotlinType = NSNumber
                        static func fromKotlin(_ value: NSNumber) -> Int {
                            value.intValue
                        }
                        func toKotlin() -> NSNumber {
                            self as NSNumber
                        }
                    }

                    extension Double: KMPConvertible {
                        typealias KotlinType = NSNumber
                        static func fromKotlin(_ value: NSNumber) -> Double {
                            value.doubleValue
                        }
                        func toKotlin() -> NSNumber {
                            self as NSNumber
                        }
                    }

                    // MARK: - One-way binding
                    extension CFlow {
                        func bind<SwiftValue: KMPConvertible>() -> SwiftValue where SwiftValue.KotlinType == T {
                            return SwiftValue.fromKotlin(self.getValue())
                        }

                        func observe<SwiftValue: KMPConvertible>(
                            _ handler: @escaping (SwiftValue) -> Void
                        ) -> Kotlinx_coroutines_coreDisposableHandle? where SwiftValue.KotlinType == T {
                            return self.observe { value in
                                DispatchQueue.main.async {
                                    handler(SwiftValue.fromKotlin(value))
                                }
                            }
                        }
                    }

                    // MARK: - Two-way SwiftUI binding
                    extension MutableCFlow {
                        func bindTwoWay<SwiftValue: KMPConvertible>() -> Binding<SwiftValue>
                        where SwiftValue.KotlinType == T {
                            let initial = SwiftValue.fromKotlin(self.getValue())
                            let subject = CurrentValueSubject<SwiftValue, Never>(initial)

                            _ = self.observe { value in
                                DispatchQueue.main.async {
                                    subject.send(SwiftValue.fromKotlin(value))
                                }
                            }

                            return Binding(
                                get: { subject.value },
                                set: { newValue in
                                    self.setValue(value: newValue.toKotlin())
                                }
                            )
                        }
                    }
                    
                    final class SwiftMPFlowState<T: KMPConvertible>: ObservableObject {
                        @Published var value: T
                        private var handle: Kotlinx_coroutines_coreDisposableHandle?

                        init(flow: CFlow<T.KotlinType>) {
                            self.value = T.fromKotlin(flow.getValue())
                            self.handle = flow.observe { [weak self] value in
                                DispatchQueue.main.async {
                                    self?.value = T.fromKotlin(value)
                                }
                            }
                        }

                        deinit {
                            handle?.dispose()
                        }
                    }
            """.trimIndent()
            )
        }

        val iosOut = outputIosDir.get().asFile
        iosOut.mkdirs()
        File(iosOut, "flowsSwiftExtensions.swift").writeText(swiftBridge)
    }
}

data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

data class ScreenMetadata(
    val viewModel: String,
    val screen: String,
    val isInitial: Boolean
)

fun String.normalizeToAscii(): String =
    this.map { if (it.code in 32..126) it else ' ' }.joinToString("")