<br/>
<p align="center">
    <a href="https://github.com/TheArchitect123/Atlas"><img src="./kotlin.jpg" align="center" width=350/></a>
</p>

<p align="center">
Atlas is a powerful Kotlin Multiplatform (KMP) SDK that provides a complete ecosystem for building scalable, structured, and maintainable applications across All Platforms for KMP. It combines MVVM architecture, navigation, CLI tools, and an IoC container into one seamless experience.
    <br/>
<strong>Currently only 12.3 kB in size</strong>

</p>
<br/>

<p align="center">
   <a href="https://github.com/TheArchitect123/Atlas">
    <img alt="GitHub" src="https://img.shields.io/maven-central/v/io.github.thearchitect123/atlas-core">
  </a>

  <a href="https://github.com/TheArchitect123/Atlas">
    <img alt="GitHub" src="https://img.shields.io/badge/All%20Platforms-Android%20•%20AppleWatch%20•%20iOS%20•%20WASM%20•%20JS%20•%20NodeJS%20•%20JVM-blue?style=flat-square&logo=kotlin">
  </a>
</p>

<br/>

## How it works
Atlas is a complete SDK for Kotlin Multiplatform with everything you need for your development.

**Supports all Platforms**

**Currently Still in Development but with basic features already supported**

Full documentation will be developed once the SDK is finished.

**Currently supported features**:
1. Compile Time Dependency Injection either via DSL or with Annotations.

Supported Annotations:
```
   1. @Singleton - Marks a class or function as a singleton, meaning that only one instance of it will be created and shared across the entire application.
                Use this when a service should have a single instance throughout the app lifecycle.
                
   Example Usage: 
   
   @Singleton
   class MyServiceComponent {
    fun getString() = "My Sample String"
   }
   
   //to use your service, please use the DSL Api.
   val result = AtlasDI.resolve<MyServiceComponent>().getString() 
   
   // result = "My Sample String"
   
   2. @Factory - Marks a class or function as a factory, meaning a new instance is created every time it is requested.
	             Use this when you need to regenerate a fresh instance each time.
	
   3. @Scoped - Marks a class or function as a scoped instance, meaning the instance is tied to a specific scope ID.
		        Use this when an instance should be shared within a specific scope (e.g., User Session, Request, Activity, etc.).	 
            
   4. @ViewModels - Marks a class as a ViewModel, meaning it will be lazily initialized and tied to an Android/Fragment owner lifecycle.
		            Use this for ViewModels in an MVVM architecture if you are using Jetpack Lifecycle Components. 
		            
		            On other platforms generates a single instance that is used once per class (example: ViewController for iOS)
		            
   5. @Module, @Provides - Marks a class as a dependency module, meaning it provides dependencies using @Provides.
	                       Use this when you need to define multiple dependencies inside one module.
   
   @Module
   class MyServiceComponentModule {
       @Provides
       fun provideHttpClient(): HttpClient {
           return HttpClient()
       }
   }	            
```

To use the SDK please make sure to import both the plugin and the Atlas Core package.
To import the plugin please write in your **shared.gradle** file:
```
plugins {
    id("io.github.thearchitect123.atlasGraphGenerator") // get latest version
}

 kotlin {
   sourceSets {
        commonMain.dependencies {
            implementation("io.github.thearchitect123:atlas-core:+") // import latest package
        }      
   }
 }
 
 // make sure also to add this service (this will activate the plugin that generates your dependency graph at compile time)
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("generateDependencyGraph")
}

```

Make sure as well that your app supports a minimum of version 2.1.0 of kotlin. 
This is so the kotlin coroutines will work (when using @ViewModels)

If you're using a toml file, please write:
```
[versions]
kotlin = "2.1.0"  
```

To initialize the DSL and Atlas Container, please use (after first compiling your project):
```AtlasDI.injectContainer(com.architect.atlas.container.AtlasContainer)```

Once the DSL is registered, you will be able to fetch/register services as normal:
```
   AtlasDI.registerSingleton(MySingletonComponent())
   AtlasDI.registerInstance(MySingletonComponent())
   AtlasDI.registerFactory(MySingletonComponent())
   AtlasDI.registerViewModel(MySingletonComponent())
```

## License

This software is licensed under the MIT license. See [LICENSE](./LICENSE) for full disclosure.
