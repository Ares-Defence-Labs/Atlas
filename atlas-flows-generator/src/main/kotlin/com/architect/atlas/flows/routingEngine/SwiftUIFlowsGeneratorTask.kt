package com.architect.atlas.flows.routingEngine

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
abstract class SwiftUIFlowsGeneratorTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputIosDir: DirectoryProperty

    @get:Input
    abstract var projectCoreName: String

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputHashFile: RegularFileProperty

    init {
        group = "AtlasFlows"
        description = "Generates the SwiftUI flow binding implementations"
    }

    @TaskAction
    fun generateNavigatorClass() {
        if(!File(outputIosDir.get().asFile, "FlowsSwiftExtensions.swift").exists()) {
            generateIOSSwiftBridge()
        }
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
                    
struct FlowAdapter<SwiftType>: Sendable {
    let fromKotlin: @Sendable (Any) -> SwiftType
    let toKotlin: @Sendable (SwiftType) -> Any
}

func makeAdapter<T>(_ type: T.Type) -> FlowAdapter<T> {
    FlowAdapter<T>(
        fromKotlin: { ${'$'}0 as! T },
        toKotlin: { ${'$'}0 as AnyObject }
    )
}

func makeListAdapter<T>(_ type: T.Type) -> FlowAdapter<[T]> {
    FlowAdapter<[T]>(
        fromKotlin: { (${'$'}0 as! NSArray).compactMap { ${'$'}0 as? T } },
        toKotlin: { ${'$'}0 as NSArray }
    )
}

// MARK: - Built-in Adapters

let stringAdapter = makeAdapter(String.self)
let boolAdapter = makeAdapter(Bool.self)
let intAdapter = makeAdapter(Int.self)
let doubleAdapter = makeAdapter(Double.self)

// MARK: - SwiftUI ObservableObject for KMP Flow
extension FlowBinding where T: RandomAccessCollection, T: MutableCollection, T.Index == Int {
    var listBinding: Binding<T> {
        Binding(
            get: { self.value },
            set: { newValue in
                self.value = newValue
                self.flow.setValue(value: self.adapter.toKotlin(newValue))
            }
        )
    }
}
struct ForEachBinding<Data: MutableCollection, ID: Hashable, Content: View>: View where Data.Index == Int {
    let data: Binding<Data>
    let id: KeyPath<Data.Element, ID>
    let content: (Binding<Data.Element>) -> Content

    init(
        _ data: Binding<Data>,
        id: KeyPath<Data.Element, ID>,
        @ViewBuilder content: @escaping (Binding<Data.Element>) -> Content
    ) {
        self.data = data
        self.id = id
        self.content = content
    }

    var body: some View {
        let items = data.wrappedValue.enumerated().map { index, element in
            (id: element[keyPath: id], index: index)
        }

        return ForEach(items, id: \.id) { item in
            content(
                Binding(
                    get: { data.wrappedValue[item.index] },
                    set: { data.wrappedValue[item.index] = ${'$'}0 }
                )
            )
        }
    }
}

struct BindingField<T> {
    let get: () -> T

    func value() -> T {
        get()
    }
}

extension Binding {
    subscript<Field>(_ keyPath: KeyPath<Value, Field>) -> BindingField<Field> {
        BindingField { self.wrappedValue[keyPath: keyPath] }
    }
}

@MainActor
final class FlowBinding<T>: ObservableObject {
    @Published var value: T
    private var handle: DisposableHandle?
    private let flow: AnyKmpObjectFlow
    private let adapter: FlowAdapter<T>

    init(flow: AnyKmpObjectFlow, adapter: FlowAdapter<T>) {
        self.flow = flow
        self.adapter = adapter
        self.value = adapter.fromKotlin(flow.getValue())

        self.handle = flow.observe { [weak self] newValue in
            DispatchQueue.main.async {
                self?.value = self?.adapter.fromKotlin(newValue) ?? self!.value
            }
        }
    }

    var binding: Binding<T> {
        Binding(
            get: { self.value },
            set: {
                self.value = ${'$'}0
                self.flow.setValue(value: self.adapter.toKotlin(${'$'}0))
            }
        )
    }

    deinit {
        Task { @MainActor [weak self] in
            self?.handle?.dispose()
        }
    }
}

// MARK: - SwiftUI View Modifiers

extension View {
    func bindTextTwoWay(
        _ flow: AnyKmpObjectFlow,
        adapter: FlowAdapter<String> = stringAdapter
    ) -> some View {
        let binding = FlowBinding(flow: flow, adapter: adapter)
        return TextField("", text: binding.binding)
    }

    func bindToggleTwoWay(
        _ flow: AnyKmpObjectFlow,
        adapter: FlowAdapter<Bool> = boolAdapter
    ) -> some View {
        let binding = FlowBinding(flow: flow, adapter: adapter)
        return Toggle("", isOn: binding.binding)
    }

    func bindHidden<T>(
        _ flow: AnyKmpObjectFlow,
        adapter: FlowAdapter<T>,
        predicate: @escaping (T) -> Bool
    ) -> some View {
        let binding = FlowBinding(flow: flow, adapter: adapter)
        return self.hidden()
    }

    @ViewBuilder
    func bindGone<T>(
        _ flow: AnyKmpObjectFlow,
        adapter: FlowAdapter<T>,
        isVisible: @escaping (T) -> Bool
    ) -> some View {
        let binding = FlowBinding(flow: flow, adapter: adapter)
        if isVisible(binding.value) {
            self
        }
    }
}

// MARK: - Text Extension

@MainActor
extension Text {
    init(flow: AnyKmpObjectFlow, adapter: FlowAdapter<String> = stringAdapter) {
        let binding = FlowBinding(flow: flow, adapter: adapter)
        self.init(binding.value)
    }
}

// MARK: - Generic Observer

@MainActor
func observe<T>(
    _ flow: AnyKmpObjectFlow,
    adapter: FlowAdapter<T>,
    onUpdate: @escaping (T) -> Void
) -> DisposableHandle? {
    flow.observe { value in
        DispatchQueue.main.async {
            onUpdate(adapter.fromKotlin(value))
        }
    }
}

@MainActor
extension TextField where Label == Text {
    init(
        placeholder: String = "",
        flow: AnyKmpObjectFlow,
        adapter: FlowAdapter<String> = stringAdapter
    ) {
        let binding = FlowBinding(flow: flow, adapter: adapter)
        self.init(placeholder, text: binding.binding)
    }
}

            """.trimIndent()
            )
        }

        val iosOut = outputIosDir.get().asFile
        iosOut.mkdirs()
        File(iosOut, "FlowsSwiftExtensions.swift").writeText(swiftBridge)
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