#import <Foundation/NSArray.h>
#import <Foundation/NSDictionary.h>
#import <Foundation/NSError.h>
#import <Foundation/NSObject.h>
#import <Foundation/NSSet.h>
#import <Foundation/NSString.h>
#import <Foundation/NSValue.h>

@class SharedAnyKmpObjectFlow, SharedAtlasColorsCompanion, SharedAtlasContainer, SharedAtlasDICompanion, SharedAtlasFontsCompanion, SharedAtlasImagesCompanion, SharedAtlasStringsCompanion, SharedAtomicfuNativeMutexNode, SharedAtomicfuSynchronizedObject, SharedAtomicfuSynchronizedObjectLockState, SharedAtomicfuSynchronizedObjectStatus, SharedBaseComps, SharedBufferOverflow, SharedCFlow<T>, SharedChannelFactory, SharedChannelFlow<T>, SharedCloseableCoroutineDispatcher, SharedCompTestStandardCompanion, SharedCoroutineDispatcher, SharedCoroutineDispatcherKey, SharedCoroutineExceptionHandlerKey, SharedCoroutineName, SharedCoroutineNameKey, SharedCoroutineStart, SharedDispatchers, SharedGlobalScope, SharedJobKey, SharedJobSupport, SharedKotlinAbstractCoroutineContextElement, SharedKotlinAbstractCoroutineContextKey<B, E>, SharedKotlinArray<T>, SharedKotlinAtomicReference<T>, SharedKotlinCancellationException, SharedKotlinEnum<E>, SharedKotlinEnumCompanion, SharedKotlinException, SharedKotlinIllegalStateException, SharedKotlinIntArray, SharedKotlinIntIterator, SharedKotlinIntProgression, SharedKotlinIntProgressionCompanion, SharedKotlinIntRange, SharedKotlinIntRangeCompanion, SharedKotlinLongArray, SharedKotlinLongIterator, SharedKotlinLongProgression, SharedKotlinLongProgressionCompanion, SharedKotlinLongRange, SharedKotlinLongRangeCompanion, SharedKotlinNoSuchElementException, SharedKotlinNothing, SharedKotlinRuntimeException, SharedKotlinThrowable, SharedKotlinUnit, SharedLockFreeLinkedListNode, SharedMainCoroutineDispatcher, SharedMutableAtlasFlowState<T>, SharedMutableCFlow<T>, SharedNonCancellable, SharedNonDisposableHandle, SharedPerson, SharedPlatformColor, SharedSampleProcess, SharedSharingCommand, SharedSharingStartedCompanion, SharedSwiftClassGeneratorCompanion, SharedTestIOSCompanion, SharedTestSingle, SharedThreadSafeHeap<T>, SharedTimeoutCancellationException, SharedViewModel, UIColor, UIFont, UIImage, UILabel, UISlider, UISwitch, UITextField, UIView;

@protocol SharedAtlasContainerContract, SharedBroadcastChannel, SharedCancellableContinuation, SharedChannel, SharedChannelIterator, SharedChildHandle, SharedChildJob, SharedCompletableDeferred, SharedCompletableJob, SharedCopyableThrowable, SharedCoroutineExceptionHandler, SharedCoroutineScope, SharedDNSTest, SharedDeferred, SharedDisposableHandle, SharedDisposableHandle_, SharedFlow, SharedFlowCollector, SharedFusibleFlow, SharedHello, SharedJob, SharedKotlinAutoCloseable, SharedKotlinClosedRange, SharedKotlinComparable, SharedKotlinContinuation, SharedKotlinContinuationInterceptor, SharedKotlinCoroutineContext, SharedKotlinCoroutineContextElement, SharedKotlinCoroutineContextKey, SharedKotlinFunction, SharedKotlinIterable, SharedKotlinIterator, SharedKotlinKAnnotatedElement, SharedKotlinKClass, SharedKotlinKClassifier, SharedKotlinKDeclarationContainer, SharedKotlinLazy, SharedKotlinOpenEndRange, SharedKotlinSequence, SharedKotlinSuspendFunction0, SharedKotlinSuspendFunction1, SharedKotlinSuspendFunction2, SharedKotlinSuspendFunction3, SharedKotlinSuspendFunction4, SharedKotlinSuspendFunction5, SharedKotlinSuspendFunction6, SharedMainDispatcherFactory, SharedMutableSharedFlow, SharedMutableStateFlow, SharedMutex, SharedParentJob, SharedPlatform, SharedPoppable, SharedProducerScope, SharedPushable, SharedReceiveChannel, SharedRunnable, SharedSelectBuilder, SharedSelectClause, SharedSelectClause0, SharedSelectClause1, SharedSelectClause2, SharedSelectInstance, SharedSemaphore, SharedSendChannel, SharedSharedFlow, SharedSharingStarted, SharedStateFlow, SharedTestProcess;

NS_ASSUME_NONNULL_BEGIN
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunknown-warning-option"
#pragma clang diagnostic ignored "-Wincompatible-property-type"
#pragma clang diagnostic ignored "-Wnullability"

#pragma push_macro("_Nullable_result")
#if !__has_feature(nullability_nullable_result)
#undef _Nullable_result
#define _Nullable_result _Nullable
#endif

__attribute__((swift_name("KotlinBase")))
@interface SharedBase : NSObject
- (instancetype)init __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
+ (void)initialize __attribute__((objc_requires_super));
@end

@interface SharedBase (SharedBaseCopying) <NSCopying>
@end

__attribute__((swift_name("KotlinMutableSet")))
@interface SharedMutableSet<ObjectType> : NSMutableSet<ObjectType>
@end

__attribute__((swift_name("KotlinMutableDictionary")))
@interface SharedMutableDictionary<KeyType, ObjectType> : NSMutableDictionary<KeyType, ObjectType>
@end

@interface NSError (NSErrorSharedKotlinException)
@property (readonly) id _Nullable kotlinException;
@end

__attribute__((swift_name("KotlinNumber")))
@interface SharedNumber : NSNumber
- (instancetype)initWithChar:(char)value __attribute__((unavailable));
- (instancetype)initWithUnsignedChar:(unsigned char)value __attribute__((unavailable));
- (instancetype)initWithShort:(short)value __attribute__((unavailable));
- (instancetype)initWithUnsignedShort:(unsigned short)value __attribute__((unavailable));
- (instancetype)initWithInt:(int)value __attribute__((unavailable));
- (instancetype)initWithUnsignedInt:(unsigned int)value __attribute__((unavailable));
- (instancetype)initWithLong:(long)value __attribute__((unavailable));
- (instancetype)initWithUnsignedLong:(unsigned long)value __attribute__((unavailable));
- (instancetype)initWithLongLong:(long long)value __attribute__((unavailable));
- (instancetype)initWithUnsignedLongLong:(unsigned long long)value __attribute__((unavailable));
- (instancetype)initWithFloat:(float)value __attribute__((unavailable));
- (instancetype)initWithDouble:(double)value __attribute__((unavailable));
- (instancetype)initWithBool:(BOOL)value __attribute__((unavailable));
- (instancetype)initWithInteger:(NSInteger)value __attribute__((unavailable));
- (instancetype)initWithUnsignedInteger:(NSUInteger)value __attribute__((unavailable));
+ (instancetype)numberWithChar:(char)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedChar:(unsigned char)value __attribute__((unavailable));
+ (instancetype)numberWithShort:(short)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedShort:(unsigned short)value __attribute__((unavailable));
+ (instancetype)numberWithInt:(int)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedInt:(unsigned int)value __attribute__((unavailable));
+ (instancetype)numberWithLong:(long)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedLong:(unsigned long)value __attribute__((unavailable));
+ (instancetype)numberWithLongLong:(long long)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedLongLong:(unsigned long long)value __attribute__((unavailable));
+ (instancetype)numberWithFloat:(float)value __attribute__((unavailable));
+ (instancetype)numberWithDouble:(double)value __attribute__((unavailable));
+ (instancetype)numberWithBool:(BOOL)value __attribute__((unavailable));
+ (instancetype)numberWithInteger:(NSInteger)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedInteger:(NSUInteger)value __attribute__((unavailable));
@end

__attribute__((swift_name("KotlinByte")))
@interface SharedByte : SharedNumber
- (instancetype)initWithChar:(char)value;
+ (instancetype)numberWithChar:(char)value;
@end

__attribute__((swift_name("KotlinUByte")))
@interface SharedUByte : SharedNumber
- (instancetype)initWithUnsignedChar:(unsigned char)value;
+ (instancetype)numberWithUnsignedChar:(unsigned char)value;
@end

__attribute__((swift_name("KotlinShort")))
@interface SharedShort : SharedNumber
- (instancetype)initWithShort:(short)value;
+ (instancetype)numberWithShort:(short)value;
@end

__attribute__((swift_name("KotlinUShort")))
@interface SharedUShort : SharedNumber
- (instancetype)initWithUnsignedShort:(unsigned short)value;
+ (instancetype)numberWithUnsignedShort:(unsigned short)value;
@end

__attribute__((swift_name("KotlinInt")))
@interface SharedInt : SharedNumber
- (instancetype)initWithInt:(int)value;
+ (instancetype)numberWithInt:(int)value;
@end

__attribute__((swift_name("KotlinUInt")))
@interface SharedUInt : SharedNumber
- (instancetype)initWithUnsignedInt:(unsigned int)value;
+ (instancetype)numberWithUnsignedInt:(unsigned int)value;
@end

__attribute__((swift_name("KotlinLong")))
@interface SharedLong : SharedNumber
- (instancetype)initWithLongLong:(long long)value;
+ (instancetype)numberWithLongLong:(long long)value;
@end

__attribute__((swift_name("KotlinULong")))
@interface SharedULong : SharedNumber
- (instancetype)initWithUnsignedLongLong:(unsigned long long)value;
+ (instancetype)numberWithUnsignedLongLong:(unsigned long long)value;
@end

__attribute__((swift_name("KotlinFloat")))
@interface SharedFloat : SharedNumber
- (instancetype)initWithFloat:(float)value;
+ (instancetype)numberWithFloat:(float)value;
@end

__attribute__((swift_name("KotlinDouble")))
@interface SharedDouble : SharedNumber
- (instancetype)initWithDouble:(double)value;
+ (instancetype)numberWithDouble:(double)value;
@end

__attribute__((swift_name("KotlinBoolean")))
@interface SharedBoolean : SharedNumber
- (instancetype)initWithBool:(BOOL)value;
+ (instancetype)numberWithBool:(BOOL)value;
@end

__attribute__((swift_name("ViewModel")))
@interface SharedViewModel : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)onAppearing __attribute__((swift_name("onAppearing()")));
- (void)onBackground __attribute__((swift_name("onBackground()")));
- (void)onCleared __attribute__((swift_name("onCleared()")));
- (void)onDestroy __attribute__((swift_name("onDestroy()")));
- (void)onDisappearing __attribute__((swift_name("onDisappearing()")));
- (void)onForeground __attribute__((swift_name("onForeground()")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)onInitializeWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("onInitialize(completionHandler:)")));
@property (readonly) id<SharedCoroutineScope> viewModelScope __attribute__((swift_name("viewModelScope")));
@end

__attribute__((swift_name("AtlasNavigationService")))
@protocol SharedAtlasNavigationService
@required
- (void)dismissModalAnimate:(BOOL)animate params:(id _Nullable)params __attribute__((swift_name("dismissModal(animate:params:)")));
- (NSArray<SharedViewModel *> *)getNavigationStack __attribute__((swift_name("getNavigationStack()")));
- (void)navigateToPageViewModelClass:(id<SharedKotlinKClass>)viewModelClass params:(id _Nullable)params __attribute__((swift_name("navigateToPage(viewModelClass:params:)")));
- (void)navigateToPageModalViewModelClass:(id<SharedKotlinKClass>)viewModelClass params:(id _Nullable)params __attribute__((swift_name("navigateToPageModal(viewModelClass:params:)")));
- (void)navigateToPagePushAndReplaceViewModelClass:(id<SharedKotlinKClass>)viewModelClass params:(id _Nullable)params __attribute__((swift_name("navigateToPagePushAndReplace(viewModelClass:params:)")));
- (void)popPageAnimate:(BOOL)animate params:(id _Nullable)params __attribute__((swift_name("popPage(animate:params:)")));
- (void)popPagesWithCountCountOfPages:(int32_t)countOfPages animate:(BOOL)animate params:(id _Nullable)params __attribute__((swift_name("popPagesWithCount(countOfPages:animate:params:)")));
- (void)popToPageRoute:(NSString *)route params:(id _Nullable)params __attribute__((swift_name("popToPage(route:params:)")));
- (void)popToRootAnimate:(BOOL)animate params:(id _Nullable)params __attribute__((swift_name("popToRoot(animate:params:)")));
- (void)setNavigationStackStack:(NSArray<SharedViewModel *> *)stack params:(id _Nullable)params __attribute__((swift_name("setNavigationStack(stack:params:)")));
@end

__attribute__((swift_name("AtlasTabNavigationService")))
@protocol SharedAtlasTabNavigationService
@required
- (void)navigateToTabIndexViewModelClass:(id<SharedKotlinKClass>)viewModelClass params:(id _Nullable)params __attribute__((swift_name("navigateToTabIndex(viewModelClass:params:)")));
@end

__attribute__((swift_name("Poppable")))
@protocol SharedPoppable
@required
- (void)onPopParamsParams:(id)params __attribute__((swift_name("onPopParams(params:)")));
@end

__attribute__((swift_name("Pushable")))
@protocol SharedPushable
@required
- (void)onPushParamsParams:(id)params __attribute__((swift_name("onPushParams(params:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AnyKmpObjectFlow")))
@interface SharedAnyKmpObjectFlow : SharedBase
- (instancetype)initWithGetter:(id (^)(void))getter setter:(void (^ _Nullable)(id))setter collector:(id<SharedJob> (^)(id<SharedKotlinSuspendFunction1>))collector __attribute__((swift_name("init(getter:setter:collector:)"))) __attribute__((objc_designated_initializer));
- (id)getValue __attribute__((swift_name("getValue()")));
- (id<SharedDisposableHandle>)observeBlock:(void (^)(id))block __attribute__((swift_name("observe(block:)")));
- (void)setValueValue:(id)value __attribute__((swift_name("setValue(value:)")));
@end

__attribute__((swift_name("CFlow")))
@interface SharedCFlow<T> : SharedBase
- (instancetype)initWithFlow:(id<SharedStateFlow>)flow __attribute__((swift_name("init(flow:)"))) __attribute__((objc_designated_initializer));
- (id<SharedDisposableHandle>)observeBlock:(void (^)(T _Nullable))block __attribute__((swift_name("observe(block:)")));
- (id<SharedDisposableHandle>)observeMainBlock:(void (^)(T _Nullable))block __attribute__((swift_name("observeMain(block:)")));
@end

__attribute__((swift_name("DisposableHandle")))
@protocol SharedDisposableHandle
@required
- (void)dispose __attribute__((swift_name("dispose()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MutableAtlasFlowState")))
@interface SharedMutableAtlasFlowState<T> : SharedBase
- (instancetype)initWithInitialValue:(T)initialValue __attribute__((swift_name("init(initialValue:)"))) __attribute__((objc_designated_initializer));
- (id<SharedMutableStateFlow>)asMutableStateFlow __attribute__((swift_name("asMutableStateFlow()")));
- (id<SharedStateFlow>)asStateFlow __attribute__((swift_name("asStateFlow()")));
- (SharedAnyKmpObjectFlow *)asSwiftFlow __attribute__((swift_name("asSwiftFlow()")));
- (T)getCurrentValue __attribute__((swift_name("getCurrentValue()")));
- (void)postValueOnMainThreadValue:(T)value __attribute__((swift_name("postValueOnMainThread(value:)")));
- (void)setValueOnContextValue:(T)value __attribute__((swift_name("setValueOnContext(value:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MutableCFlow")))
@interface SharedMutableCFlow<T> : SharedCFlow<T>
- (instancetype)initWithFlow:(id<SharedMutableStateFlow>)flow __attribute__((swift_name("init(flow:)"))) __attribute__((objc_designated_initializer));
- (void)setValueValue:(T _Nullable)value __attribute__((swift_name("setValue(value:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("BoolFlow")))
@interface SharedBoolFlow : SharedBase
- (instancetype)initWithStateFlow:(id<SharedMutableStateFlow>)stateFlow __attribute__((swift_name("init(stateFlow:)"))) __attribute__((objc_designated_initializer));
- (BOOL)getValue __attribute__((swift_name("getValue()")));
- (id<SharedDisposableHandle_>)observeBlock:(void (^)(SharedBoolean *))block __attribute__((swift_name("observe(block:)")));
- (id<SharedDisposableHandle_>)observeMainBlock:(void (^)(SharedBoolean *))block __attribute__((swift_name("observeMain(block:)")));
- (void)setValueValue:(BOOL)value __attribute__((swift_name("setValue(value:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DoubleFlow")))
@interface SharedDoubleFlow : SharedBase
- (instancetype)initWithStateFlow:(id<SharedMutableStateFlow>)stateFlow __attribute__((swift_name("init(stateFlow:)"))) __attribute__((objc_designated_initializer));
- (double)getValue __attribute__((swift_name("getValue()")));
- (id<SharedDisposableHandle_>)observeBlock:(void (^)(SharedDouble *))block __attribute__((swift_name("observe(block:)")));
- (id<SharedDisposableHandle_>)observeMainBlock:(void (^)(SharedDouble *))block __attribute__((swift_name("observeMain(block:)")));
- (void)setValueValue:(double)value __attribute__((swift_name("setValue(value:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("IntFlow")))
@interface SharedIntFlow : SharedBase
- (instancetype)initWithStateFlow:(id<SharedMutableStateFlow>)stateFlow __attribute__((swift_name("init(stateFlow:)"))) __attribute__((objc_designated_initializer));
- (int32_t)getValue __attribute__((swift_name("getValue()")));
- (id<SharedDisposableHandle_>)observeBlock:(void (^)(SharedInt *))block __attribute__((swift_name("observe(block:)")));
- (id<SharedDisposableHandle_>)observeMainBlock:(void (^)(SharedInt *))block __attribute__((swift_name("observeMain(block:)")));
- (void)setValueValue:(int32_t)value __attribute__((swift_name("setValue(value:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("StringFlow")))
@interface SharedStringFlow : SharedBase
- (instancetype)initWithStateFlow:(id<SharedMutableStateFlow>)stateFlow __attribute__((swift_name("init(stateFlow:)"))) __attribute__((objc_designated_initializer));
- (NSString *)getValue __attribute__((swift_name("getValue()")));
- (id<SharedDisposableHandle_>)observeBlock:(void (^)(NSString *))block __attribute__((swift_name("observe(block:)")));
- (id<SharedDisposableHandle_>)observeMainBlock:(void (^)(NSString *))block __attribute__((swift_name("observeMain(block:)")));
- (void)setValueValue:(NSString *)value __attribute__((swift_name("setValue(value:)")));
@end

__attribute__((swift_name("AtlasContainerContract")))
@protocol SharedAtlasContainerContract
@required
- (void)registerClazz:(id<SharedKotlinKClass>)clazz instance:(id _Nullable)instance factory:(id (^ _Nullable)(void))factory scopeId:(NSString * _Nullable)scopeId viewModel:(BOOL)viewModel __attribute__((swift_name("register(clazz:instance:factory:scopeId:viewModel:)")));
- (void)resetViewModelClazz:(id<SharedKotlinKClass>)clazz __attribute__((swift_name("resetViewModel(clazz:)")));
- (id)resolveClazz:(id<SharedKotlinKClass>)clazz __attribute__((swift_name("resolve(clazz:)")));
- (id)resolveByNameClazz:(NSString *)clazz __attribute__((swift_name("resolveByName(clazz:)")));
- (id _Nullable)resolveViewModelClazz:(id<SharedKotlinKClass>)clazz __attribute__((swift_name("resolveViewModel(clazz:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AtlasContainer")))
@interface SharedAtlasContainer : SharedBase <SharedAtlasContainerContract>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)atlasContainer __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedAtlasContainer *shared __attribute__((swift_name("shared")));
- (void)registerClazz:(id<SharedKotlinKClass>)clazz instance:(id _Nullable)instance factory:(id (^ _Nullable)(void))factory scopeId:(NSString * _Nullable)scopeId viewModel:(BOOL)viewModel __attribute__((swift_name("register(clazz:instance:factory:scopeId:viewModel:)")));
- (void)resetViewModelClazz:(id<SharedKotlinKClass>)clazz __attribute__((swift_name("resetViewModel(clazz:)")));
- (id)resolveClazz:(id<SharedKotlinKClass>)clazz __attribute__((swift_name("resolve(clazz:)")));
- (id)resolveByNameClazz:(NSString *)clazz __attribute__((swift_name("resolveByName(clazz:)")));
- (id _Nullable)resolveViewModelClazz:(id<SharedKotlinKClass>)clazz __attribute__((swift_name("resolveViewModel(clazz:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AtlasDI")))
@interface SharedAtlasDI : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@property (class, readonly, getter=companion) SharedAtlasDICompanion *companion __attribute__((swift_name("companion")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AtlasDI.Companion")))
@interface SharedAtlasDICompanion : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedAtlasDICompanion *shared __attribute__((swift_name("shared")));
- (void)injectContainerContainerInstance:(id<SharedAtlasContainerContract>)containerInstance __attribute__((swift_name("injectContainer(containerInstance:)")));
- (void)registerFactoryFactory:(id (^ _Nullable)(void))factory __attribute__((swift_name("registerFactory(factory:)")));
- (void)registerInstanceInstance:(id _Nullable)instance __attribute__((swift_name("registerInstance(instance:)")));
- (void)registerInterfaceToInstanceClazz:(id<SharedKotlinKClass>)clazz instance:(id)instance __attribute__((swift_name("registerInterfaceToInstance(clazz:instance:)")));
- (void)registerScopedScopeId:(NSString * _Nullable)scopeId __attribute__((swift_name("registerScoped(scopeId:)")));
- (void)registerServiceInstance:(id _Nullable)instance factory:(id (^ _Nullable)(void))factory scopeId:(NSString * _Nullable)scopeId viewModel:(BOOL)viewModel __attribute__((swift_name("registerService(instance:factory:scopeId:viewModel:)")));
- (void)registerSingletonInstance:(id _Nullable)instance __attribute__((swift_name("registerSingleton(instance:)")));
- (void)registerViewModelInstance:(id _Nullable)instance __attribute__((swift_name("registerViewModel(instance:)")));
- (void)resetViewModelClazz:(id<SharedKotlinKClass>)clazz __attribute__((swift_name("resetViewModel(clazz:)")));
- (id<SharedKotlinLazy>)resolveLazyService __attribute__((swift_name("resolveLazyService()")));
- (id)resolveService __attribute__((swift_name("resolveService()")));
- (id _Nullable)resolveServiceNullableClazz:(id<SharedKotlinKClass>)clazz __attribute__((swift_name("resolveServiceNullable(clazz:)")));
- (id _Nullable)resolveServiceNullableByNameClazz:(NSString *)clazz __attribute__((swift_name("resolveServiceNullableByName(clazz:)")));
- (NSString *)resolveServiceNullableByNameServiceClazz:(id)clazz __attribute__((swift_name("resolveServiceNullableByNameService(clazz:)")));
- (id _Nullable)resolveViewModelClazz:(id<SharedKotlinKClass>)clazz __attribute__((swift_name("resolveViewModel(clazz:)")));
@property id<SharedAtlasContainerContract> _Nullable container __attribute__((swift_name("container")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SwiftClassGenerator")))
@interface SharedSwiftClassGenerator : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@property (class, readonly, getter=companion) SharedSwiftClassGeneratorCompanion *companion __attribute__((swift_name("companion")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SwiftClassGenerator.Companion")))
@interface SharedSwiftClassGeneratorCompanion : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedSwiftClassGeneratorCompanion *shared __attribute__((swift_name("shared")));
- (NSString *)getClazzType:(id)type __attribute__((swift_name("getClazz(type:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("WeakReference")))
@interface SharedWeakReference<T> : SharedBase
- (instancetype)initWithValue:(T _Nullable)value __attribute__((swift_name("init(value:)"))) __attribute__((objc_designated_initializer));
- (T _Nullable)get __attribute__((swift_name("get()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AtlasColors")))
@interface SharedAtlasColors : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@property (class, readonly, getter=companion) SharedAtlasColorsCompanion *companion __attribute__((swift_name("companion")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AtlasColors.Companion")))
@interface SharedAtlasColorsCompanion : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedAtlasColorsCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) SharedPlatformColor *black __attribute__((swift_name("black")));
@property (readonly) SharedPlatformColor *green __attribute__((swift_name("green")));
@property (readonly) SharedPlatformColor *primary __attribute__((swift_name("primary")));
@property (readonly) SharedPlatformColor *secondary __attribute__((swift_name("secondary")));
@property (readonly) SharedPlatformColor *white __attribute__((swift_name("white")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PlatformColor")))
@interface SharedPlatformColor : SharedBase
- (instancetype)initWithRaw:(NSString *)raw __attribute__((swift_name("init(raw:)"))) __attribute__((objc_designated_initializer));
- (NSString *)swiftUIColor __attribute__((swift_name("swiftUIColor()")));
- (UIColor *)uiColor __attribute__((swift_name("uiColor()")));
@property (readonly) NSString *raw __attribute__((swift_name("raw")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AtlasFonts")))
@interface SharedAtlasFonts : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@property (class, readonly, getter=companion) SharedAtlasFontsCompanion *companion __attribute__((swift_name("companion")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AtlasFonts.Companion")))
@interface SharedAtlasFontsCompanion : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedAtlasFontsCompanion *shared __attribute__((swift_name("shared")));
- (UIFont *)roboto_blackSize:(double)size __attribute__((swift_name("roboto_black(size:)")));
- (UIFont *)roboto_blackitalicSize:(double)size __attribute__((swift_name("roboto_blackitalic(size:)")));
- (UIFont *)roboto_boldSize:(double)size __attribute__((swift_name("roboto_bold(size:)")));
- (UIFont *)roboto_bolditalicSize:(double)size __attribute__((swift_name("roboto_bolditalic(size:)")));
- (UIFont *)roboto_condensed_blackSize:(double)size __attribute__((swift_name("roboto_condensed_black(size:)")));
- (UIFont *)roboto_condensed_blackitalicSize:(double)size __attribute__((swift_name("roboto_condensed_blackitalic(size:)")));
- (UIFont *)roboto_condensed_boldSize:(double)size __attribute__((swift_name("roboto_condensed_bold(size:)")));
- (UIFont *)roboto_condensed_bolditalicSize:(double)size __attribute__((swift_name("roboto_condensed_bolditalic(size:)")));
- (UIFont *)roboto_condensed_extraboldSize:(double)size __attribute__((swift_name("roboto_condensed_extrabold(size:)")));
- (UIFont *)roboto_condensed_extrabolditalicSize:(double)size __attribute__((swift_name("roboto_condensed_extrabolditalic(size:)")));
- (UIFont *)roboto_condensed_extralightSize:(double)size __attribute__((swift_name("roboto_condensed_extralight(size:)")));
- (UIFont *)roboto_condensed_extralightitalicSize:(double)size __attribute__((swift_name("roboto_condensed_extralightitalic(size:)")));
- (UIFont *)roboto_condensed_italicSize:(double)size __attribute__((swift_name("roboto_condensed_italic(size:)")));
- (UIFont *)roboto_condensed_lightSize:(double)size __attribute__((swift_name("roboto_condensed_light(size:)")));
- (UIFont *)roboto_condensed_lightitalicSize:(double)size __attribute__((swift_name("roboto_condensed_lightitalic(size:)")));
- (UIFont *)roboto_condensed_mediumSize:(double)size __attribute__((swift_name("roboto_condensed_medium(size:)")));
- (UIFont *)roboto_condensed_mediumitalicSize:(double)size __attribute__((swift_name("roboto_condensed_mediumitalic(size:)")));
- (UIFont *)roboto_condensed_regularSize:(double)size __attribute__((swift_name("roboto_condensed_regular(size:)")));
- (UIFont *)roboto_condensed_semiboldSize:(double)size __attribute__((swift_name("roboto_condensed_semibold(size:)")));
- (UIFont *)roboto_condensed_semibolditalicSize:(double)size __attribute__((swift_name("roboto_condensed_semibolditalic(size:)")));
- (UIFont *)roboto_condensed_thinSize:(double)size __attribute__((swift_name("roboto_condensed_thin(size:)")));
- (UIFont *)roboto_condensed_thinitalicSize:(double)size __attribute__((swift_name("roboto_condensed_thinitalic(size:)")));
- (UIFont *)roboto_extraboldSize:(double)size __attribute__((swift_name("roboto_extrabold(size:)")));
- (UIFont *)roboto_extrabolditalicSize:(double)size __attribute__((swift_name("roboto_extrabolditalic(size:)")));
- (UIFont *)roboto_extralightSize:(double)size __attribute__((swift_name("roboto_extralight(size:)")));
- (UIFont *)roboto_extralightitalicSize:(double)size __attribute__((swift_name("roboto_extralightitalic(size:)")));
- (UIFont *)roboto_italicSize:(double)size __attribute__((swift_name("roboto_italic(size:)")));
- (UIFont *)roboto_lightSize:(double)size __attribute__((swift_name("roboto_light(size:)")));
- (UIFont *)roboto_lightitalicSize:(double)size __attribute__((swift_name("roboto_lightitalic(size:)")));
- (UIFont *)roboto_mediumSize:(double)size __attribute__((swift_name("roboto_medium(size:)")));
- (UIFont *)roboto_mediumitalicSize:(double)size __attribute__((swift_name("roboto_mediumitalic(size:)")));
- (UIFont *)roboto_regularSize:(double)size __attribute__((swift_name("roboto_regular(size:)")));
- (UIFont *)roboto_semiboldSize:(double)size __attribute__((swift_name("roboto_semibold(size:)")));
- (UIFont *)roboto_semibolditalicSize:(double)size __attribute__((swift_name("roboto_semibolditalic(size:)")));
- (UIFont *)roboto_semicondensed_blackSize:(double)size __attribute__((swift_name("roboto_semicondensed_black(size:)")));
- (UIFont *)roboto_semicondensed_blackitalicSize:(double)size __attribute__((swift_name("roboto_semicondensed_blackitalic(size:)")));
- (UIFont *)roboto_semicondensed_boldSize:(double)size __attribute__((swift_name("roboto_semicondensed_bold(size:)")));
- (UIFont *)roboto_semicondensed_bolditalicSize:(double)size __attribute__((swift_name("roboto_semicondensed_bolditalic(size:)")));
- (UIFont *)roboto_semicondensed_extraboldSize:(double)size __attribute__((swift_name("roboto_semicondensed_extrabold(size:)")));
- (UIFont *)roboto_semicondensed_extrabolditalicSize:(double)size __attribute__((swift_name("roboto_semicondensed_extrabolditalic(size:)")));
- (UIFont *)roboto_semicondensed_extralightSize:(double)size __attribute__((swift_name("roboto_semicondensed_extralight(size:)")));
- (UIFont *)roboto_semicondensed_extralightitalicSize:(double)size __attribute__((swift_name("roboto_semicondensed_extralightitalic(size:)")));
- (UIFont *)roboto_semicondensed_italicSize:(double)size __attribute__((swift_name("roboto_semicondensed_italic(size:)")));
- (UIFont *)roboto_semicondensed_lightSize:(double)size __attribute__((swift_name("roboto_semicondensed_light(size:)")));
- (UIFont *)roboto_semicondensed_lightitalicSize:(double)size __attribute__((swift_name("roboto_semicondensed_lightitalic(size:)")));
- (UIFont *)roboto_semicondensed_mediumSize:(double)size __attribute__((swift_name("roboto_semicondensed_medium(size:)")));
- (UIFont *)roboto_semicondensed_mediumitalicSize:(double)size __attribute__((swift_name("roboto_semicondensed_mediumitalic(size:)")));
- (UIFont *)roboto_semicondensed_regularSize:(double)size __attribute__((swift_name("roboto_semicondensed_regular(size:)")));
- (UIFont *)roboto_semicondensed_semiboldSize:(double)size __attribute__((swift_name("roboto_semicondensed_semibold(size:)")));
- (UIFont *)roboto_semicondensed_semibolditalicSize:(double)size __attribute__((swift_name("roboto_semicondensed_semibolditalic(size:)")));
- (UIFont *)roboto_semicondensed_thinSize:(double)size __attribute__((swift_name("roboto_semicondensed_thin(size:)")));
- (UIFont *)roboto_semicondensed_thinitalicSize:(double)size __attribute__((swift_name("roboto_semicondensed_thinitalic(size:)")));
- (UIFont *)roboto_thinSize:(double)size __attribute__((swift_name("roboto_thin(size:)")));
- (UIFont *)roboto_thinitalicSize:(double)size __attribute__((swift_name("roboto_thinitalic(size:)")));
- (UIFont *)testfontfileSize:(double)size __attribute__((swift_name("testfontfile(size:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AtlasImages")))
@interface SharedAtlasImages : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@property (class, readonly, getter=companion) SharedAtlasImagesCompanion *companion __attribute__((swift_name("companion")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AtlasImages.Companion")))
@interface SharedAtlasImagesCompanion : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedAtlasImagesCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) UIImage * _Nullable android_svg __attribute__((swift_name("android_svg")));
@property (readonly) UIImage * _Nullable home_72dp_000000_fill0_wght400_grad0_opsz48 __attribute__((swift_name("home_72dp_000000_fill0_wght400_grad0_opsz48")));
@property (readonly) UIImage * _Nullable home_72dp_000000_fill0_wght400_grad0_opsz49 __attribute__((swift_name("home_72dp_000000_fill0_wght400_grad0_opsz49")));
@property (readonly) UIImage * _Nullable sample_home __attribute__((swift_name("sample_home")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AtlasStrings")))
@interface SharedAtlasStrings : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@property (class, readonly, getter=companion) SharedAtlasStringsCompanion *companion __attribute__((swift_name("companion")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AtlasStrings.Companion")))
@interface SharedAtlasStringsCompanion : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedAtlasStringsCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) NSString *confirm __attribute__((swift_name("confirm")));
@property (readonly) NSString *errorMessage __attribute__((swift_name("errorMessage")));
@property (readonly) NSString *farewell __attribute__((swift_name("farewell")));
@property (readonly) NSString *greeting __attribute__((swift_name("greeting")));
@property (readonly) NSString *helloTest __attribute__((swift_name("helloTest")));
@property (readonly) NSString *welcomeMessage __attribute__((swift_name("welcomeMessage")));
@end

__attribute__((swift_name("BaseComps")))
@interface SharedBaseComps : SharedViewModel
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CompTestStandard")))
@interface SharedCompTestStandard : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@property (class, readonly, getter=companion) SharedCompTestStandardCompanion *companion __attribute__((swift_name("companion")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CompTestStandard.Companion")))
@interface SharedCompTestStandardCompanion : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedCompTestStandardCompanion *shared __attribute__((swift_name("shared")));
- (SharedTestSingle * _Nullable)getTestSingleName:(NSString *)name __attribute__((swift_name("getTestSingle(name:)")));
@property SharedMutableAtlasFlowState<NSString *> *test __attribute__((swift_name("test")));
@end

__attribute__((swift_name("Hello")))
@protocol SharedHello
@required
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ComponentTest")))
@interface SharedComponentTest : SharedBase <SharedHello>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Comps")))
@interface SharedComps : SharedBaseComps
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@end

__attribute__((swift_name("DNSTest")))
@protocol SharedDNSTest
@required
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DNSComps")))
@interface SharedDNSComps : SharedBase <SharedDNSTest>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Greeting")))
@interface SharedGreeting : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (NSString *)greet __attribute__((swift_name("greet()")));
@end

__attribute__((swift_name("Platform")))
@protocol SharedPlatform
@required
@property (readonly) NSString *name __attribute__((swift_name("name")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("IOSPlatform")))
@interface SharedIOSPlatform : SharedBase <SharedPlatform>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MainViewModel")))
@interface SharedMainViewModel : SharedBaseComps
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)test __attribute__((swift_name("test()")));
@property NSString *q __attribute__((swift_name("q")));
@end

__attribute__((swift_name("TestProcess")))
@protocol SharedTestProcess
@required
- (NSString *)test __attribute__((swift_name("test()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReviewApps")))
@interface SharedReviewApps : SharedBase <SharedTestProcess>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (NSString *)test __attribute__((swift_name("test()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReviewProcess")))
@interface SharedReviewProcess : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (NSString *)test __attribute__((swift_name("test()")));
@property id<SharedKotlinLazy> rs __attribute__((swift_name("rs")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReviewProcessTester")))
@interface SharedReviewProcessTester : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (NSString *)test __attribute__((swift_name("test()")));
@property id<SharedKotlinLazy> rs __attribute__((swift_name("rs")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Sample")))
@interface SharedSample : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (NSString *)test __attribute__((swift_name("test()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SampleHandles")))
@interface SharedSampleHandles : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TestIOS")))
@interface SharedTestIOS : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@property (class, readonly, getter=companion) SharedTestIOSCompanion *companion __attribute__((swift_name("companion")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TestIOS.Companion")))
@interface SharedTestIOSCompanion : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedTestIOSCompanion *shared __attribute__((swift_name("shared")));
- (NSString *)registerServices __attribute__((swift_name("registerServices()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TestSampleData")))
@interface SharedTestSampleData : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("HelloThereTest")))
@interface SharedHelloThereTest : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SampleConfig")))
@interface SharedSampleConfig : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TestInter")))
@interface SharedTestInter : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DroidStandard")))
@interface SharedDroidStandard : SharedViewModel <SharedPushable, SharedPoppable>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (NSString *)helloThere __attribute__((swift_name("helloThere()")));
- (void)onAppearing __attribute__((swift_name("onAppearing()")));
- (void)onDestroy __attribute__((swift_name("onDestroy()")));
- (void)onDisappearing __attribute__((swift_name("onDisappearing()")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)onInitializeWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("onInitialize(completionHandler:)")));
- (void)onPopParamsParams:(SharedInt *)params __attribute__((swift_name("onPopParams(params:)")));
- (void)onPushParamsParams:(NSString *)params __attribute__((swift_name("onPushParams(params:)")));
- (void)setHelloTestingS:(NSString *)s __attribute__((swift_name("setHelloTesting(s:)")));
@property (readonly) SharedMutableAtlasFlowState<SharedBoolean *> *isShown __attribute__((swift_name("isShown")));
@property NSString *q __attribute__((swift_name("q")));
@property (readonly) SharedMutableAtlasFlowState<NSArray<SharedSampleProcess *> *> *resultsSamples __attribute__((swift_name("resultsSamples")));
@property (readonly) SharedMutableAtlasFlowState<NSString *> *testText __attribute__((swift_name("testText")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DroidStandardSecond")))
@interface SharedDroidStandardSecond : SharedViewModel <SharedPushable, SharedPoppable>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (NSString *)helloThere __attribute__((swift_name("helloThere()")));
- (void)onAppearing __attribute__((swift_name("onAppearing()")));
- (void)onDestroy __attribute__((swift_name("onDestroy()")));
- (void)onDisappearing __attribute__((swift_name("onDisappearing()")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)onInitializeWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("onInitialize(completionHandler:)")));
- (void)onPopParamsParams:(SharedInt *)params __attribute__((swift_name("onPopParams(params:)")));
- (void)onPushParamsParams:(NSString *)params __attribute__((swift_name("onPushParams(params:)")));
- (void)setHelloTestingS:(NSString *)s __attribute__((swift_name("setHelloTesting(s:)")));
@property NSString *q __attribute__((swift_name("q")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MobileTest")))
@interface SharedMobileTest : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (NSString *)helloThere __attribute__((swift_name("helloThere()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Person")))
@interface SharedPerson : SharedBase
- (instancetype)initWithPerson:(NSString *)person rc:(int32_t)rc __attribute__((swift_name("init(person:rc:)"))) __attribute__((objc_designated_initializer));
- (SharedPerson *)doCopyPerson:(NSString *)person rc:(int32_t)rc __attribute__((swift_name("doCopy(person:rc:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *person __attribute__((swift_name("person")));
@property (readonly) int32_t rc __attribute__((swift_name("rc")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SampleProcess")))
@interface SharedSampleProcess : SharedBase
- (instancetype)initWithRes:(int32_t)res result:(NSString *)result listResult:(NSArray<SharedPerson *> *)listResult __attribute__((swift_name("init(res:result:listResult:)"))) __attribute__((objc_designated_initializer));
- (SharedSampleProcess *)doCopyRes:(int32_t)res result:(NSString *)result listResult:(NSArray<SharedPerson *> *)listResult __attribute__((swift_name("doCopy(res:result:listResult:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<SharedPerson *> *listResult __attribute__((swift_name("listResult")));
@property (readonly) int32_t res __attribute__((swift_name("res")));
@property NSString *result __attribute__((swift_name("result")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TestHelloThere")))
@interface SharedTestHelloThere : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (NSString *)helloThere __attribute__((swift_name("helloThere()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TestSingle")))
@interface SharedTestSingle : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (NSString *)helloThere __attribute__((swift_name("helloThere()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TabParentViewModel")))
@interface SharedTabParentViewModel : SharedViewModel
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)onInitializeWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("onInitialize(completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoreDashboardTabViewModel")))
@interface SharedCoreDashboardTabViewModel : SharedViewModel
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)onAppearing __attribute__((swift_name("onAppearing()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoreSettingsTabViewModel")))
@interface SharedCoreSettingsTabViewModel : SharedViewModel
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)onAppearing __attribute__((swift_name("onAppearing()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FragmentTestFirstViewModel")))
@interface SharedFragmentTestFirstViewModel : SharedViewModel
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)test __attribute__((swift_name("test()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FragmentTestSecondViewModel")))
@interface SharedFragmentTestSecondViewModel : SharedViewModel
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)processResult __attribute__((swift_name("processResult()")));
@property NSString *q __attribute__((swift_name("q")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FragmentTestThirdViewModel")))
@interface SharedFragmentTestThirdViewModel : SharedViewModel
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)processResult __attribute__((swift_name("processResult()")));
@property NSString *q __attribute__((swift_name("q")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.3")
*/
__attribute__((swift_name("KotlinCoroutineContext")))
@protocol SharedKotlinCoroutineContext
@required
- (id _Nullable)foldInitial:(id _Nullable)initial operation:(id _Nullable (^)(id _Nullable, id<SharedKotlinCoroutineContextElement>))operation __attribute__((swift_name("fold(initial:operation:)")));
- (id<SharedKotlinCoroutineContextElement> _Nullable)getKey:(id<SharedKotlinCoroutineContextKey>)key __attribute__((swift_name("get(key:)")));
- (id<SharedKotlinCoroutineContext>)minusKeyKey:(id<SharedKotlinCoroutineContextKey>)key __attribute__((swift_name("minusKey(key:)")));
- (id<SharedKotlinCoroutineContext>)plusContext:(id<SharedKotlinCoroutineContext>)context __attribute__((swift_name("plus(context:)")));
@end

__attribute__((swift_name("KotlinCoroutineContextElement")))
@protocol SharedKotlinCoroutineContextElement <SharedKotlinCoroutineContext>
@required
@property (readonly) id<SharedKotlinCoroutineContextKey> key __attribute__((swift_name("key")));
@end

__attribute__((swift_name("Job")))
@protocol SharedJob <SharedKotlinCoroutineContextElement>
@required

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
- (id<SharedChildHandle>)attachChildChild:(id<SharedChildJob>)child __attribute__((swift_name("attachChild(child:)")));
- (void)cancelCause:(SharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(cause:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
- (SharedKotlinCancellationException *)getCancellationException __attribute__((swift_name("getCancellationException()")));
- (id<SharedDisposableHandle_>)invokeOnCompletionHandler:(void (^)(SharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnCompletion(handler:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
- (id<SharedDisposableHandle_>)invokeOnCompletionOnCancelling:(BOOL)onCancelling invokeImmediately:(BOOL)invokeImmediately handler:(void (^)(SharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnCompletion(onCancelling:invokeImmediately:handler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)joinWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("join(completionHandler:)")));
- (id<SharedJob>)plusOther:(id<SharedJob>)other __attribute__((swift_name("plus(other:)"))) __attribute__((unavailable("Operator '+' on two Job objects is meaningless. Job is a coroutine context element and `+` is a set-sum operator for coroutine contexts. The job to the right of `+` just replaces the job the left of `+`.")));
- (BOOL)start __attribute__((swift_name("start()")));
@property (readonly) id<SharedKotlinSequence> children __attribute__((swift_name("children")));
@property (readonly) BOOL isActive __attribute__((swift_name("isActive")));
@property (readonly) BOOL isCancelled __attribute__((swift_name("isCancelled")));
@property (readonly) BOOL isCompleted __attribute__((swift_name("isCompleted")));
@property (readonly) id<SharedSelectClause0> onJoin __attribute__((swift_name("onJoin")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
@property (readonly) id<SharedJob> _Nullable parent __attribute__((swift_name("parent")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
__attribute__((swift_name("ChildJob")))
@protocol SharedChildJob <SharedJob>
@required

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
- (void)parentCancelledParentJob:(id<SharedParentJob>)parentJob __attribute__((swift_name("parentCancelled(parentJob:)")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
__attribute__((swift_name("ParentJob")))
@protocol SharedParentJob <SharedJob>
@required

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
- (SharedKotlinCancellationException *)getChildJobCancellationCause __attribute__((swift_name("getChildJobCancellationCause()")));
@end

__attribute__((swift_name("JobSupport")))
@interface SharedJobSupport : SharedBase <SharedJob, SharedChildJob, SharedParentJob>
- (instancetype)initWithActive:(BOOL)active __attribute__((swift_name("init(active:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable("This is internal API and may be removed in the future releases")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)afterCompletionState:(id _Nullable)state __attribute__((swift_name("afterCompletion(state:)")));
- (id<SharedChildHandle>)attachChildChild:(id<SharedChildJob>)child __attribute__((swift_name("attachChild(child:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)awaitInternalWithCompletionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("awaitInternal(completionHandler:)")));
- (void)cancelCause:(SharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(cause:)")));
- (BOOL)cancelCoroutineCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("cancelCoroutine(cause:)")));
- (void)cancelInternalCause:(SharedKotlinThrowable *)cause __attribute__((swift_name("cancelInternal(cause:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (NSString *)cancellationExceptionMessage __attribute__((swift_name("cancellationExceptionMessage()")));
- (BOOL)childCancelledCause:(SharedKotlinThrowable *)cause __attribute__((swift_name("childCancelled(cause:)")));
- (SharedKotlinCancellationException *)getCancellationException __attribute__((swift_name("getCancellationException()")));
- (SharedKotlinCancellationException *)getChildJobCancellationCause __attribute__((swift_name("getChildJobCancellationCause()")));
- (SharedKotlinThrowable * _Nullable)getCompletionExceptionOrNull __attribute__((swift_name("getCompletionExceptionOrNull()")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (BOOL)handleJobExceptionException:(SharedKotlinThrowable *)exception __attribute__((swift_name("handleJobException(exception:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)doInitParentJobParent:(id<SharedJob> _Nullable)parent __attribute__((swift_name("doInitParentJob(parent:)")));
- (id<SharedDisposableHandle_>)invokeOnCompletionHandler:(void (^)(SharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnCompletion(handler:)")));
- (id<SharedDisposableHandle_>)invokeOnCompletionOnCancelling:(BOOL)onCancelling invokeImmediately:(BOOL)invokeImmediately handler:(void (^)(SharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnCompletion(onCancelling:invokeImmediately:handler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)joinWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("join(completionHandler:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)onCancellingCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("onCancelling(cause:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)onCompletionInternalState:(id _Nullable)state __attribute__((swift_name("onCompletionInternal(state:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)onStart __attribute__((swift_name("onStart()")));
- (void)parentCancelledParentJob:(id<SharedParentJob>)parentJob __attribute__((swift_name("parentCancelled(parentJob:)")));
- (BOOL)start __attribute__((swift_name("start()")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (SharedKotlinCancellationException *)toCancellationException:(SharedKotlinThrowable *)receiver message:(NSString * _Nullable)message __attribute__((swift_name("toCancellationException(_:message:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
- (NSString *)toDebugString __attribute__((swift_name("toDebugString()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) id<SharedKotlinSequence> children __attribute__((swift_name("children")));

/**
 * @note This property has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
@property (readonly) SharedKotlinThrowable * _Nullable completionCause __attribute__((swift_name("completionCause")));

/**
 * @note This property has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
@property (readonly) BOOL completionCauseHandled __attribute__((swift_name("completionCauseHandled")));
@property (readonly) BOOL isActive __attribute__((swift_name("isActive")));
@property (readonly) BOOL isCancelled __attribute__((swift_name("isCancelled")));
@property (readonly) BOOL isCompleted __attribute__((swift_name("isCompleted")));
@property (readonly) BOOL isCompletedExceptionally __attribute__((swift_name("isCompletedExceptionally")));

/**
 * @note This property has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
@property (readonly) BOOL isScopedCoroutine __attribute__((swift_name("isScopedCoroutine")));
@property (readonly) id<SharedKotlinCoroutineContextKey> key __attribute__((swift_name("key")));

/**
 * @note This property has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
@property (readonly) id<SharedSelectClause1> onAwaitInternal __attribute__((swift_name("onAwaitInternal")));
@property (readonly) id<SharedSelectClause0> onJoin __attribute__((swift_name("onJoin")));
@property (readonly) id<SharedJob> _Nullable parent __attribute__((swift_name("parent")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.3")
*/
__attribute__((swift_name("KotlinContinuation")))
@protocol SharedKotlinContinuation
@required
- (void)resumeWithResult:(id _Nullable)result __attribute__((swift_name("resumeWith(result:)")));
@property (readonly) id<SharedKotlinCoroutineContext> context __attribute__((swift_name("context")));
@end

__attribute__((swift_name("CoroutineScope")))
@protocol SharedCoroutineScope
@required
@property (readonly) id<SharedKotlinCoroutineContext> coroutineContext __attribute__((swift_name("coroutineContext")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
__attribute__((swift_name("AbstractCoroutine")))
@interface SharedAbstractCoroutine<__contravariant T> : SharedJobSupport <SharedJob, SharedKotlinContinuation, SharedCoroutineScope>
- (instancetype)initWithParentContext:(id<SharedKotlinCoroutineContext>)parentContext initParentJob:(BOOL)initParentJob active:(BOOL)active __attribute__((swift_name("init(parentContext:initParentJob:active:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithActive:(BOOL)active __attribute__((swift_name("init(active:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)afterResumeState:(id _Nullable)state __attribute__((swift_name("afterResume(state:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (NSString *)cancellationExceptionMessage __attribute__((swift_name("cancellationExceptionMessage()")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)onCancelledCause:(SharedKotlinThrowable *)cause handled:(BOOL)handled __attribute__((swift_name("onCancelled(cause:handled:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)onCompletedValue:(T _Nullable)value __attribute__((swift_name("onCompleted(value:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)onCompletionInternalState:(id _Nullable)state __attribute__((swift_name("onCompletionInternal(state:)")));
- (void)resumeWithResult:(id _Nullable)result __attribute__((swift_name("resumeWith(result:)")));
- (void)startStart:(SharedCoroutineStart *)start receiver:(id _Nullable)receiver block:(id<SharedKotlinSuspendFunction1>)block __attribute__((swift_name("start(start:receiver:block:)")));
@property (readonly) id<SharedKotlinCoroutineContext> context __attribute__((swift_name("context")));
@property (readonly) id<SharedKotlinCoroutineContext> coroutineContext __attribute__((swift_name("coroutineContext")));
@property (readonly) BOOL isActive __attribute__((swift_name("isActive")));
@end

__attribute__((swift_name("CancellableContinuation")))
@protocol SharedCancellableContinuation <SharedKotlinContinuation>
@required
- (BOOL)cancelCause_:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("cancel(cause_:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
- (void)completeResumeToken:(id)token __attribute__((swift_name("completeResume(token:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
- (void)doInitCancellability __attribute__((swift_name("doInitCancellability()")));
- (void)invokeOnCancellationHandler:(void (^)(SharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnCancellation(handler:)")));
- (void)resumeValue:(id _Nullable)value onCancellation:(void (^ _Nullable)(SharedKotlinThrowable *, id _Nullable, id<SharedKotlinCoroutineContext>))onCancellation __attribute__((swift_name("resume(value:onCancellation:)")));
- (void)resumeValue:(id _Nullable)value onCancellation_:(void (^ _Nullable)(SharedKotlinThrowable *))onCancellation __attribute__((swift_name("resume(value:onCancellation_:)"))) __attribute__((deprecated("Use the overload that also accepts the `value` and the coroutine context in lambda")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
- (void)resumeUndispatched:(SharedCoroutineDispatcher *)receiver value:(id _Nullable)value __attribute__((swift_name("resumeUndispatched(_:value:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
- (void)resumeUndispatchedWithException:(SharedCoroutineDispatcher *)receiver exception:(SharedKotlinThrowable *)exception __attribute__((swift_name("resumeUndispatchedWithException(_:exception:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
- (id _Nullable)tryResumeValue:(id _Nullable)value idempotent:(id _Nullable)idempotent __attribute__((swift_name("tryResume(value:idempotent:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
- (id _Nullable)tryResumeValue:(id _Nullable)value idempotent:(id _Nullable)idempotent onCancellation:(void (^ _Nullable)(SharedKotlinThrowable *, id _Nullable, id<SharedKotlinCoroutineContext>))onCancellation __attribute__((swift_name("tryResume(value:idempotent:onCancellation:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
- (id _Nullable)tryResumeWithExceptionException:(SharedKotlinThrowable *)exception __attribute__((swift_name("tryResumeWithException(exception:)")));
@property (readonly) BOOL isActive __attribute__((swift_name("isActive")));
@property (readonly) BOOL isCancelled __attribute__((swift_name("isCancelled")));
@property (readonly) BOOL isCompleted __attribute__((swift_name("isCompleted")));
@end

__attribute__((swift_name("DisposableHandle_")))
@protocol SharedDisposableHandle_
@required
- (void)dispose __attribute__((swift_name("dispose()")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
__attribute__((swift_name("ChildHandle")))
@protocol SharedChildHandle <SharedDisposableHandle_>
@required

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
- (BOOL)childCancelledCause:(SharedKotlinThrowable *)cause __attribute__((swift_name("childCancelled(cause:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
@property (readonly) id<SharedJob> _Nullable parent __attribute__((swift_name("parent")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.3")
*/
__attribute__((swift_name("KotlinAbstractCoroutineContextElement")))
@interface SharedKotlinAbstractCoroutineContextElement : SharedBase <SharedKotlinCoroutineContextElement>
- (instancetype)initWithKey:(id<SharedKotlinCoroutineContextKey>)key __attribute__((swift_name("init(key:)"))) __attribute__((objc_designated_initializer));
@property (readonly) id<SharedKotlinCoroutineContextKey> key __attribute__((swift_name("key")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.3")
*/
__attribute__((swift_name("KotlinContinuationInterceptor")))
@protocol SharedKotlinContinuationInterceptor <SharedKotlinCoroutineContextElement>
@required
- (id<SharedKotlinContinuation>)interceptContinuationContinuation:(id<SharedKotlinContinuation>)continuation __attribute__((swift_name("interceptContinuation(continuation:)")));
- (void)releaseInterceptedContinuationContinuation:(id<SharedKotlinContinuation>)continuation __attribute__((swift_name("releaseInterceptedContinuation(continuation:)")));
@end

__attribute__((swift_name("CoroutineDispatcher")))
@interface SharedCoroutineDispatcher : SharedKotlinAbstractCoroutineContextElement <SharedKotlinContinuationInterceptor>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithKey:(id<SharedKotlinCoroutineContextKey>)key __attribute__((swift_name("init(key:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) SharedCoroutineDispatcherKey *companion __attribute__((swift_name("companion")));
- (void)dispatchContext:(id<SharedKotlinCoroutineContext>)context block:(id<SharedRunnable>)block __attribute__((swift_name("dispatch(context:block:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
- (void)dispatchYieldContext:(id<SharedKotlinCoroutineContext>)context block:(id<SharedRunnable>)block __attribute__((swift_name("dispatchYield(context:block:)")));
- (id<SharedKotlinContinuation>)interceptContinuationContinuation:(id<SharedKotlinContinuation>)continuation __attribute__((swift_name("interceptContinuation(continuation:)")));
- (BOOL)isDispatchNeededContext:(id<SharedKotlinCoroutineContext>)context __attribute__((swift_name("isDispatchNeeded(context:)")));
- (SharedCoroutineDispatcher *)limitedParallelismParallelism:(int32_t)parallelism name:(NSString * _Nullable)name __attribute__((swift_name("limitedParallelism(parallelism:name:)")));
- (SharedCoroutineDispatcher *)plusOther_:(SharedCoroutineDispatcher *)other __attribute__((swift_name("plus(other_:)"))) __attribute__((unavailable("Operator '+' on two CoroutineDispatcher objects is meaningless. CoroutineDispatcher is a coroutine context element and `+` is a set-sum operator for coroutine contexts. The dispatcher to the right of `+` just replaces the dispatcher to the left.")));
- (void)releaseInterceptedContinuationContinuation:(id<SharedKotlinContinuation>)continuation __attribute__((swift_name("releaseInterceptedContinuation(continuation:)")));
- (NSString *)description __attribute__((swift_name("description()")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="2.0")
*/
__attribute__((swift_name("KotlinAutoCloseable")))
@protocol SharedKotlinAutoCloseable
@required
- (void)close __attribute__((swift_name("close()")));
@end

__attribute__((swift_name("CloseableCoroutineDispatcher")))
@interface SharedCloseableCoroutineDispatcher : SharedCoroutineDispatcher <SharedKotlinAutoCloseable>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)close __attribute__((swift_name("close()")));
@end

__attribute__((swift_name("Deferred")))
@protocol SharedDeferred <SharedJob>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)awaitWithCompletionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("await(completionHandler:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
- (id _Nullable)getCompleted __attribute__((swift_name("getCompleted()")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
- (SharedKotlinThrowable * _Nullable)getCompletionExceptionOrNull __attribute__((swift_name("getCompletionExceptionOrNull()")));
@property (readonly) id<SharedSelectClause1> onAwait __attribute__((swift_name("onAwait")));
@end

__attribute__((swift_name("CompletableDeferred")))
@protocol SharedCompletableDeferred <SharedDeferred>
@required
- (BOOL)completeValue:(id _Nullable)value __attribute__((swift_name("complete(value:)")));
- (BOOL)completeExceptionallyException:(SharedKotlinThrowable *)exception __attribute__((swift_name("completeExceptionally(exception:)")));
@end

__attribute__((swift_name("CompletableJob")))
@protocol SharedCompletableJob <SharedJob>
@required
- (BOOL)complete __attribute__((swift_name("complete()")));
- (BOOL)completeExceptionallyException:(SharedKotlinThrowable *)exception __attribute__((swift_name("completeExceptionally(exception:)")));
@end

__attribute__((swift_name("KotlinThrowable")))
@interface SharedKotlinThrowable : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));

/**
 * @note annotations
 *   kotlin.experimental.ExperimentalNativeApi
*/
- (SharedKotlinArray<NSString *> *)getStackTrace __attribute__((swift_name("getStackTrace()")));
- (void)printStackTrace __attribute__((swift_name("printStackTrace()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) SharedKotlinThrowable * _Nullable cause __attribute__((swift_name("cause")));
@property (readonly) NSString * _Nullable message __attribute__((swift_name("message")));
- (NSError *)asError __attribute__((swift_name("asError()")));
@end

__attribute__((swift_name("KotlinException")))
@interface SharedKotlinException : SharedKotlinThrowable
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
@end

__attribute__((swift_name("KotlinRuntimeException")))
@interface SharedKotlinRuntimeException : SharedKotlinException
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CompletionHandlerException")))
@interface SharedCompletionHandlerException : SharedKotlinRuntimeException
- (instancetype)initWithMessage:(NSString *)message cause:(SharedKotlinThrowable *)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (instancetype)initWithCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
__attribute__((swift_name("CopyableThrowable")))
@protocol SharedCopyableThrowable
@required
- (SharedKotlinThrowable * _Nullable)createCopy __attribute__((swift_name("createCopy()")));
@end

__attribute__((swift_name("KotlinCoroutineContextKey")))
@protocol SharedKotlinCoroutineContextKey
@required
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.3")
 *   kotlin.ExperimentalStdlibApi
*/
__attribute__((swift_name("KotlinAbstractCoroutineContextKey")))
@interface SharedKotlinAbstractCoroutineContextKey<B, E> : SharedBase <SharedKotlinCoroutineContextKey>
- (instancetype)initWithBaseKey:(id<SharedKotlinCoroutineContextKey>)baseKey safeCast:(E _Nullable (^)(id<SharedKotlinCoroutineContextElement>))safeCast __attribute__((swift_name("init(baseKey:safeCast:)"))) __attribute__((objc_designated_initializer));
@end


/**
 * @note annotations
 *   kotlin.ExperimentalStdlibApi
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoroutineDispatcher.Key")))
@interface SharedCoroutineDispatcherKey : SharedKotlinAbstractCoroutineContextKey<id<SharedKotlinContinuationInterceptor>, SharedCoroutineDispatcher *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithBaseKey:(id<SharedKotlinCoroutineContextKey>)baseKey safeCast:(id<SharedKotlinCoroutineContextElement> _Nullable (^)(id<SharedKotlinCoroutineContextElement>))safeCast __attribute__((swift_name("init(baseKey:safeCast:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
+ (instancetype)key __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedCoroutineDispatcherKey *shared __attribute__((swift_name("shared")));
@end

__attribute__((swift_name("CoroutineExceptionHandler")))
@protocol SharedCoroutineExceptionHandler <SharedKotlinCoroutineContextElement>
@required
- (void)handleExceptionContext:(id<SharedKotlinCoroutineContext>)context exception:(SharedKotlinThrowable *)exception __attribute__((swift_name("handleException(context:exception:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoroutineExceptionHandlerKey")))
@interface SharedCoroutineExceptionHandlerKey : SharedBase <SharedKotlinCoroutineContextKey>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)key __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedCoroutineExceptionHandlerKey *shared __attribute__((swift_name("shared")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoroutineName")))
@interface SharedCoroutineName : SharedKotlinAbstractCoroutineContextElement
- (instancetype)initWithName:(NSString *)name __attribute__((swift_name("init(name:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithKey:(id<SharedKotlinCoroutineContextKey>)key __attribute__((swift_name("init(key:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) SharedCoroutineNameKey *companion __attribute__((swift_name("companion")));
- (SharedCoroutineName *)doCopyName:(NSString *)name __attribute__((swift_name("doCopy(name:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoroutineName.Key")))
@interface SharedCoroutineNameKey : SharedBase <SharedKotlinCoroutineContextKey>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)key __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedCoroutineNameKey *shared __attribute__((swift_name("shared")));
@end

__attribute__((swift_name("KotlinComparable")))
@protocol SharedKotlinComparable
@required
- (int32_t)compareToOther:(id _Nullable)other __attribute__((swift_name("compareTo(other:)")));
@end

__attribute__((swift_name("KotlinEnum")))
@interface SharedKotlinEnum<E> : SharedBase <SharedKotlinComparable>
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) SharedKotlinEnumCompanion *companion __attribute__((swift_name("companion")));
- (int32_t)compareToOther:(E)other __attribute__((swift_name("compareTo(other:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@property (readonly) int32_t ordinal __attribute__((swift_name("ordinal")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoroutineStart")))
@interface SharedCoroutineStart : SharedKotlinEnum<SharedCoroutineStart *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) SharedCoroutineStart *default_ __attribute__((swift_name("default_")));
@property (class, readonly) SharedCoroutineStart *lazy __attribute__((swift_name("lazy")));
@property (class, readonly) SharedCoroutineStart *atomic __attribute__((swift_name("atomic")));
@property (class, readonly) SharedCoroutineStart *undispatched __attribute__((swift_name("undispatched")));
+ (SharedKotlinArray<SharedCoroutineStart *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<SharedCoroutineStart *> *entries __attribute__((swift_name("entries")));

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
- (void)invokeBlock:(id<SharedKotlinSuspendFunction1>)block receiver:(id _Nullable)receiver completion:(id<SharedKotlinContinuation>)completion __attribute__((swift_name("invoke(block:receiver:completion:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
@property (readonly) BOOL isLazy __attribute__((swift_name("isLazy")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
__attribute__((swift_name("Delay")))
@protocol SharedDelay
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)delayTime:(int64_t)time completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("delay(time:completionHandler:)"))) __attribute__((unavailable("Deprecated without replacement as an internal method never intended for public use")));
- (id<SharedDisposableHandle_>)invokeOnTimeoutTimeMillis:(int64_t)timeMillis block:(id<SharedRunnable>)block context:(id<SharedKotlinCoroutineContext>)context __attribute__((swift_name("invokeOnTimeout(timeMillis:block:context:)")));
- (void)scheduleResumeAfterDelayTimeMillis:(int64_t)timeMillis continuation:(id<SharedCancellableContinuation>)continuation __attribute__((swift_name("scheduleResumeAfterDelay(timeMillis:continuation:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Dispatchers")))
@interface SharedDispatchers : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)dispatchers __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedDispatchers *shared __attribute__((swift_name("shared")));
@property (readonly) SharedCoroutineDispatcher *Default __attribute__((swift_name("Default")));
@property (readonly) SharedMainCoroutineDispatcher *Main __attribute__((swift_name("Main")));
@property (readonly) SharedCoroutineDispatcher *Unconfined __attribute__((swift_name("Unconfined")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.DelicateCoroutinesApi
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("GlobalScope")))
@interface SharedGlobalScope : SharedBase <SharedCoroutineScope>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)globalScope __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedGlobalScope *shared __attribute__((swift_name("shared")));
@property (readonly) id<SharedKotlinCoroutineContext> coroutineContext __attribute__((swift_name("coroutineContext")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("JobKey")))
@interface SharedJobKey : SharedBase <SharedKotlinCoroutineContextKey>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)key __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedJobKey *shared __attribute__((swift_name("shared")));
@end

__attribute__((swift_name("MainCoroutineDispatcher")))
@interface SharedMainCoroutineDispatcher : SharedCoroutineDispatcher
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (SharedCoroutineDispatcher *)limitedParallelismParallelism:(int32_t)parallelism name:(NSString * _Nullable)name __attribute__((swift_name("limitedParallelism(parallelism:name:)")));
- (NSString *)description __attribute__((swift_name("description()")));

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (NSString * _Nullable)toStringInternalImpl __attribute__((swift_name("toStringInternalImpl()")));
@property (readonly) SharedMainCoroutineDispatcher *immediate __attribute__((swift_name("immediate")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("NonCancellable")))
@interface SharedNonCancellable : SharedKotlinAbstractCoroutineContextElement <SharedJob>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithKey:(id<SharedKotlinCoroutineContextKey>)key __attribute__((swift_name("init(key:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
+ (instancetype)nonCancellable __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedNonCancellable *shared __attribute__((swift_name("shared")));
- (id<SharedChildHandle>)attachChildChild:(id<SharedChildJob>)child __attribute__((swift_name("attachChild(child:)"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
- (void)cancelCause:(SharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(cause:)"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
- (SharedKotlinCancellationException *)getCancellationException __attribute__((swift_name("getCancellationException()"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
- (id<SharedDisposableHandle_>)invokeOnCompletionHandler:(void (^)(SharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnCompletion(handler:)"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
- (id<SharedDisposableHandle_>)invokeOnCompletionOnCancelling:(BOOL)onCancelling invokeImmediately:(BOOL)invokeImmediately handler:(void (^)(SharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnCompletion(onCancelling:invokeImmediately:handler:)"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)joinWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("join(completionHandler:)"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
- (BOOL)start __attribute__((swift_name("start()"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) id<SharedKotlinSequence> children __attribute__((swift_name("children"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
@property (readonly) BOOL isActive __attribute__((swift_name("isActive"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
@property (readonly) BOOL isCancelled __attribute__((swift_name("isCancelled"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
@property (readonly) BOOL isCompleted __attribute__((swift_name("isCompleted"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
@property (readonly) id<SharedSelectClause0> onJoin __attribute__((swift_name("onJoin"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
@property (readonly) id<SharedJob> _Nullable parent __attribute__((swift_name("parent"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("NonDisposableHandle")))
@interface SharedNonDisposableHandle : SharedBase <SharedDisposableHandle_, SharedChildHandle>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)nonDisposableHandle __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedNonDisposableHandle *shared __attribute__((swift_name("shared")));
- (BOOL)childCancelledCause:(SharedKotlinThrowable *)cause __attribute__((swift_name("childCancelled(cause:)")));
- (void)dispose __attribute__((swift_name("dispose()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) id<SharedJob> _Nullable parent __attribute__((swift_name("parent")));
@end

__attribute__((swift_name("Runnable")))
@protocol SharedRunnable
@required
- (void)run __attribute__((swift_name("run()")));
@end

__attribute__((swift_name("KotlinIllegalStateException")))
@interface SharedKotlinIllegalStateException : SharedKotlinRuntimeException
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.4")
*/
__attribute__((swift_name("KotlinCancellationException")))
@interface SharedKotlinCancellationException : SharedKotlinIllegalStateException
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TimeoutCancellationException")))
@interface SharedTimeoutCancellationException : SharedKotlinCancellationException <SharedCopyableThrowable>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (instancetype)initWithCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (SharedTimeoutCancellationException *)createCopy __attribute__((swift_name("createCopy()")));
@end

__attribute__((swift_name("SendChannel")))
@protocol SharedSendChannel
@required
- (BOOL)closeCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("close(cause:)")));
- (void)invokeOnCloseHandler:(void (^)(SharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnClose(handler:)")));
- (BOOL)offerElement:(id _Nullable)element __attribute__((swift_name("offer(element:)"))) __attribute__((unavailable("Deprecated in the favour of 'trySend' method")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)sendElement:(id _Nullable)element completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("send(element:completionHandler:)")));
- (id _Nullable)trySendElement:(id _Nullable)element __attribute__((swift_name("trySend(element:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.DelicateCoroutinesApi
*/
@property (readonly) BOOL isClosedForSend __attribute__((swift_name("isClosedForSend")));
@property (readonly) id<SharedSelectClause2> onSend __attribute__((swift_name("onSend")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.ObsoleteCoroutinesApi
*/
__attribute__((swift_name("BroadcastChannel")))
@protocol SharedBroadcastChannel <SharedSendChannel>
@required
- (void)cancelCause:(SharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(cause:)")));
- (id<SharedReceiveChannel>)openSubscription __attribute__((swift_name("openSubscription()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("BufferOverflow")))
@interface SharedBufferOverflow : SharedKotlinEnum<SharedBufferOverflow *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) SharedBufferOverflow *suspend __attribute__((swift_name("suspend")));
@property (class, readonly) SharedBufferOverflow *dropOldest __attribute__((swift_name("dropOldest")));
@property (class, readonly) SharedBufferOverflow *dropLatest __attribute__((swift_name("dropLatest")));
+ (SharedKotlinArray<SharedBufferOverflow *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<SharedBufferOverflow *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((swift_name("ReceiveChannel")))
@protocol SharedReceiveChannel
@required
- (void)cancelCause:(SharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(cause:)")));
- (id<SharedChannelIterator>)iterator __attribute__((swift_name("iterator()")));
- (id _Nullable)poll __attribute__((swift_name("poll()"))) __attribute__((unavailable("Deprecated in the favour of 'tryReceive'. Please note that the provided replacement does not rethrow channel's close cause as 'poll' did, for the precise replacement please refer to the 'poll' documentation")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)receiveWithCompletionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("receive(completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)receiveCatchingWithCompletionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("receiveCatching(completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)receiveOrNullWithCompletionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("receiveOrNull(completionHandler:)"))) __attribute__((unavailable("Deprecated in favor of 'receiveCatching'. Please note that the provided replacement does not rethrow channel's close cause as 'receiveOrNull' did, for the detailed replacement please refer to the 'receiveOrNull' documentation")));
- (id _Nullable)tryReceive __attribute__((swift_name("tryReceive()")));

/**
 * @note annotations
 *   kotlinx.coroutines.DelicateCoroutinesApi
*/
@property (readonly) BOOL isClosedForReceive __attribute__((swift_name("isClosedForReceive")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
@property (readonly) BOOL isEmpty __attribute__((swift_name("isEmpty")));
@property (readonly) id<SharedSelectClause1> onReceive __attribute__((swift_name("onReceive")));
@property (readonly) id<SharedSelectClause1> onReceiveCatching __attribute__((swift_name("onReceiveCatching")));
@property (readonly) id<SharedSelectClause1> onReceiveOrNull __attribute__((swift_name("onReceiveOrNull"))) __attribute__((unavailable("Deprecated in favor of onReceiveCatching extension")));
@end

__attribute__((swift_name("Channel")))
@protocol SharedChannel <SharedSendChannel, SharedReceiveChannel>
@required
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ChannelFactory")))
@interface SharedChannelFactory : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)factory __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedChannelFactory *shared __attribute__((swift_name("shared")));
@property (readonly) int32_t BUFFERED __attribute__((swift_name("BUFFERED")));
@property (readonly) int32_t CONFLATED __attribute__((swift_name("CONFLATED")));

/**
 * @note annotations
 *   kotlinx.coroutines.DelicateCoroutinesApi
*/
@property (readonly) NSString *DEFAULT_BUFFER_PROPERTY_NAME __attribute__((swift_name("DEFAULT_BUFFER_PROPERTY_NAME")));
@property (readonly) int32_t RENDEZVOUS __attribute__((swift_name("RENDEZVOUS")));
@property (readonly) int32_t UNLIMITED __attribute__((swift_name("UNLIMITED")));
@end

__attribute__((swift_name("ChannelIterator")))
@protocol SharedChannelIterator
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)hasNextWithCompletionHandler:(void (^)(SharedBoolean * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("hasNext(completionHandler:)")));
- (id _Nullable)next __attribute__((swift_name("next()")));
@end

__attribute__((swift_name("KotlinNoSuchElementException")))
@interface SharedKotlinNoSuchElementException : SharedKotlinRuntimeException
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ClosedReceiveChannelException")))
@interface SharedClosedReceiveChannelException : SharedKotlinNoSuchElementException
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ClosedSendChannelException")))
@interface SharedClosedSendChannelException : SharedKotlinIllegalStateException
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
- (instancetype)initWithCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.ObsoleteCoroutinesApi
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ConflatedBroadcastChannel")))
@interface SharedConflatedBroadcastChannel<E> : SharedBase <SharedBroadcastChannel>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable("ConflatedBroadcastChannel is deprecated in the favour of SharedFlow and is no longer supported")));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithValue:(E _Nullable)value __attribute__((swift_name("init(value:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable("ConflatedBroadcastChannel is deprecated in the favour of SharedFlow and is no longer supported")));
- (void)cancelCause:(SharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(cause:)")));
- (BOOL)closeCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("close(cause:)")));
- (void)invokeOnCloseHandler:(void (^)(SharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnClose(handler:)")));
- (BOOL)offerElement:(E _Nullable)element __attribute__((swift_name("offer(element:)"))) __attribute__((unavailable("Deprecated in the favour of 'trySend' method")));
- (id<SharedReceiveChannel>)openSubscription __attribute__((swift_name("openSubscription()")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)sendElement:(E _Nullable)element completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("send(element:completionHandler:)")));
- (id _Nullable)trySendElement:(E _Nullable)element __attribute__((swift_name("trySend(element:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.DelicateCoroutinesApi
*/
@property (readonly) BOOL isClosedForSend __attribute__((swift_name("isClosedForSend")));
@property (readonly) id<SharedSelectClause2> onSend __attribute__((swift_name("onSend")));
@property (readonly) E _Nullable value __attribute__((swift_name("value")));
@property (readonly) E _Nullable valueOrNull __attribute__((swift_name("valueOrNull")));
@end

__attribute__((swift_name("ProducerScope")))
@protocol SharedProducerScope <SharedCoroutineScope, SharedSendChannel>
@required
@property (readonly) id<SharedSendChannel> channel __attribute__((swift_name("channel")));
@end

__attribute__((swift_name("Flow")))
@protocol SharedFlow
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)collectCollector:(id<SharedFlowCollector>)collector completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("collect(collector:completionHandler:)")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
__attribute__((swift_name("AbstractFlow")))
@interface SharedAbstractFlow<T> : SharedBase <SharedFlow>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)collectCollector:(id<SharedFlowCollector>)collector completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("collect(collector:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)collectSafelyCollector:(id<SharedFlowCollector>)collector completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("collectSafely(collector:completionHandler:)")));
@end

__attribute__((swift_name("FlowCollector")))
@protocol SharedFlowCollector
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)emitValue:(id _Nullable)value completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("emit(value:completionHandler:)")));
@end

__attribute__((swift_name("SharedFlow")))
@protocol SharedSharedFlow <SharedFlow>
@required
@property (readonly) NSArray<id> *replayCache __attribute__((swift_name("replayCache")));
@end

__attribute__((swift_name("MutableSharedFlow")))
@protocol SharedMutableSharedFlow <SharedSharedFlow, SharedFlowCollector>
@required

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
- (void)resetReplayCache __attribute__((swift_name("resetReplayCache()")));
- (BOOL)tryEmitValue:(id _Nullable)value __attribute__((swift_name("tryEmit(value:)")));
@property (readonly) id<SharedStateFlow> subscriptionCount __attribute__((swift_name("subscriptionCount")));
@end

__attribute__((swift_name("StateFlow")))
@protocol SharedStateFlow <SharedSharedFlow>
@required
@property (readonly) id _Nullable value __attribute__((swift_name("value")));
@end

__attribute__((swift_name("MutableStateFlow")))
@protocol SharedMutableStateFlow <SharedStateFlow, SharedMutableSharedFlow>
@required
- (void)setValue:(id _Nullable)value __attribute__((swift_name("setValue(_:)")));
- (BOOL)compareAndSetExpect:(id _Nullable)expect update:(id _Nullable)update __attribute__((swift_name("compareAndSet(expect:update:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SharingCommand")))
@interface SharedSharingCommand : SharedKotlinEnum<SharedSharingCommand *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) SharedSharingCommand *start __attribute__((swift_name("start")));
@property (class, readonly) SharedSharingCommand *stop __attribute__((swift_name("stop")));
@property (class, readonly) SharedSharingCommand *stopAndResetReplayCache __attribute__((swift_name("stopAndResetReplayCache")));
+ (SharedKotlinArray<SharedSharingCommand *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<SharedSharingCommand *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((swift_name("SharingStarted")))
@protocol SharedSharingStarted
@required
- (id<SharedFlow>)commandSubscriptionCount:(id<SharedStateFlow>)subscriptionCount __attribute__((swift_name("command(subscriptionCount:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SharingStartedCompanion")))
@interface SharedSharingStartedCompanion : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedSharingStartedCompanion *shared __attribute__((swift_name("shared")));
- (id<SharedSharingStarted>)WhileSubscribedStopTimeoutMillis:(int64_t)stopTimeoutMillis replayExpirationMillis:(int64_t)replayExpirationMillis __attribute__((swift_name("WhileSubscribed(stopTimeoutMillis:replayExpirationMillis:)")));
@property (readonly) id<SharedSharingStarted> Eagerly __attribute__((swift_name("Eagerly")));
@property (readonly) id<SharedSharingStarted> Lazily __attribute__((swift_name("Lazily")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
__attribute__((swift_name("FusibleFlow")))
@protocol SharedFusibleFlow <SharedFlow>
@required
- (id<SharedFlow>)fuseContext:(id<SharedKotlinCoroutineContext>)context capacity:(int32_t)capacity onBufferOverflow:(SharedBufferOverflow *)onBufferOverflow __attribute__((swift_name("fuse(context:capacity:onBufferOverflow:)")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
__attribute__((swift_name("ChannelFlow")))
@interface SharedChannelFlow<T> : SharedBase <SharedFusibleFlow>
- (instancetype)initWithContext:(id<SharedKotlinCoroutineContext>)context capacity:(int32_t)capacity onBufferOverflow:(SharedBufferOverflow *)onBufferOverflow __attribute__((swift_name("init(context:capacity:onBufferOverflow:)"))) __attribute__((objc_designated_initializer));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (NSString * _Nullable)additionalToStringProps __attribute__((swift_name("additionalToStringProps()")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)collectCollector:(id<SharedFlowCollector>)collector completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("collect(collector:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)collectToScope:(id<SharedProducerScope>)scope completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("collectTo(scope:completionHandler:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (SharedChannelFlow<T> *)createContext:(id<SharedKotlinCoroutineContext>)context capacity:(int32_t)capacity onBufferOverflow:(SharedBufferOverflow *)onBufferOverflow __attribute__((swift_name("create(context:capacity:onBufferOverflow:)")));
- (id<SharedFlow> _Nullable)dropChannelOperators __attribute__((swift_name("dropChannelOperators()")));
- (id<SharedFlow>)fuseContext:(id<SharedKotlinCoroutineContext>)context capacity:(int32_t)capacity onBufferOverflow:(SharedBufferOverflow *)onBufferOverflow __attribute__((swift_name("fuse(context:capacity:onBufferOverflow:)")));
- (id<SharedReceiveChannel>)produceImplScope:(id<SharedCoroutineScope>)scope __attribute__((swift_name("produceImpl(scope:)")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t capacity __attribute__((swift_name("capacity")));
@property (readonly) id<SharedKotlinCoroutineContext> context __attribute__((swift_name("context")));
@property (readonly) SharedBufferOverflow *onBufferOverflow __attribute__((swift_name("onBufferOverflow")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SendingCollector")))
@interface SharedSendingCollector<T> : SharedBase <SharedFlowCollector>
- (instancetype)initWithChannel:(id<SharedSendChannel>)channel __attribute__((swift_name("init(channel:)"))) __attribute__((objc_designated_initializer));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)emitValue:(T _Nullable)value completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("emit(value:completionHandler:)")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
__attribute__((swift_name("LockFreeLinkedListNode")))
@interface SharedLockFreeLinkedListNode : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (BOOL)addLastNode:(SharedLockFreeLinkedListNode *)node permissionsBitmask:(int32_t)permissionsBitmask __attribute__((swift_name("addLast(node:permissionsBitmask:)")));
- (BOOL)addOneIfEmptyNode:(SharedLockFreeLinkedListNode *)node __attribute__((swift_name("addOneIfEmpty(node:)")));
- (void)closeForbiddenElementsBit:(int32_t)forbiddenElementsBit __attribute__((swift_name("close(forbiddenElementsBit:)")));
- (BOOL)remove __attribute__((swift_name("remove()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL isRemoved __attribute__((swift_name("isRemoved")));
@property (readonly, getter=next_) id next __attribute__((swift_name("next")));
@property (readonly) SharedLockFreeLinkedListNode *nextNode __attribute__((swift_name("nextNode")));
@property (readonly) SharedLockFreeLinkedListNode *prevNode __attribute__((swift_name("prevNode")));
@end

__attribute__((swift_name("LockFreeLinkedListHead")))
@interface SharedLockFreeLinkedListHead : SharedLockFreeLinkedListNode
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)forEachBlock:(void (^)(SharedLockFreeLinkedListNode *))block __attribute__((swift_name("forEach(block:)")));
- (BOOL)remove __attribute__((swift_name("remove()")));
@property (readonly) BOOL isRemoved __attribute__((swift_name("isRemoved")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
__attribute__((swift_name("MainDispatcherFactory")))
@protocol SharedMainDispatcherFactory
@required
- (SharedMainCoroutineDispatcher *)createDispatcherAllFactories:(NSArray<id<SharedMainDispatcherFactory>> *)allFactories __attribute__((swift_name("createDispatcher(allFactories:)")));
- (NSString * _Nullable)hintOnError __attribute__((swift_name("hintOnError()")));
@property (readonly) int32_t loadPriority __attribute__((swift_name("loadPriority")));
@end

__attribute__((swift_name("AtomicfuSynchronizedObject")))
@interface SharedAtomicfuSynchronizedObject : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)lock __attribute__((swift_name("lock()")));
- (BOOL)tryLock __attribute__((swift_name("tryLock()")));
- (void)unlock __attribute__((swift_name("unlock()")));

/**
 * @note This property has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
@property (readonly, getter=lock_) SharedKotlinAtomicReference<SharedAtomicfuSynchronizedObjectLockState *> *lock __attribute__((swift_name("lock")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
__attribute__((swift_name("ThreadSafeHeap")))
@interface SharedThreadSafeHeap<T> : SharedAtomicfuSynchronizedObject
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)addLastNode:(T)node __attribute__((swift_name("addLast(node:)")));
- (BOOL)addLastIfNode:(T)node cond:(SharedBoolean *(^)(T _Nullable))cond __attribute__((swift_name("addLastIf(node:cond:)")));
- (T _Nullable)findPredicate:(SharedBoolean *(^)(T))predicate __attribute__((swift_name("find(predicate:)")));
- (T _Nullable)peek __attribute__((swift_name("peek()")));
- (BOOL)removeNode:(T)node __attribute__((swift_name("remove(node:)")));
- (T _Nullable)removeFirstIfPredicate:(SharedBoolean *(^)(T))predicate __attribute__((swift_name("removeFirstIf(predicate:)")));
- (T _Nullable)removeFirstOrNull __attribute__((swift_name("removeFirstOrNull()")));
@property (readonly) BOOL isEmpty __attribute__((swift_name("isEmpty")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
__attribute__((swift_name("ThreadSafeHeapNode")))
@protocol SharedThreadSafeHeapNode
@required
@property SharedThreadSafeHeap<id> * _Nullable heap __attribute__((swift_name("heap")));
@property int32_t index __attribute__((swift_name("index")));
@end

__attribute__((swift_name("SelectBuilder")))
@protocol SharedSelectBuilder
@required
- (void)invoke:(id<SharedSelectClause0>)receiver block:(id<SharedKotlinSuspendFunction0>)block __attribute__((swift_name("invoke(_:block:)")));
- (void)invoke:(id<SharedSelectClause1>)receiver block_:(id<SharedKotlinSuspendFunction1>)block __attribute__((swift_name("invoke(_:block_:)")));
- (void)invoke:(id<SharedSelectClause2>)receiver block__:(id<SharedKotlinSuspendFunction1>)block __attribute__((swift_name("invoke(_:block__:)")));
- (void)invoke:(id<SharedSelectClause2>)receiver param:(id _Nullable)param block:(id<SharedKotlinSuspendFunction1>)block __attribute__((swift_name("invoke(_:param:block:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
- (void)onTimeoutTimeMillis:(int64_t)timeMillis block:(id<SharedKotlinSuspendFunction0>)block __attribute__((swift_name("onTimeout(timeMillis:block:)"))) __attribute__((unavailable("Replaced with the same extension function")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
__attribute__((swift_name("SelectClause")))
@protocol SharedSelectClause
@required
@property (readonly) id clauseObject __attribute__((swift_name("clauseObject")));
@property (readonly) SharedKotlinUnit *(^(^ _Nullable onCancellationConstructor)(id<SharedSelectInstance>, id _Nullable, id _Nullable))(SharedKotlinThrowable *, id _Nullable, id<SharedKotlinCoroutineContext>) __attribute__((swift_name("onCancellationConstructor")));
@property (readonly) id _Nullable (^processResFunc)(id, id _Nullable, id _Nullable) __attribute__((swift_name("processResFunc")));
@property (readonly) void (^regFunc)(id, id<SharedSelectInstance>, id _Nullable) __attribute__((swift_name("regFunc")));
@end

__attribute__((swift_name("SelectClause0")))
@protocol SharedSelectClause0 <SharedSelectClause>
@required
@end

__attribute__((swift_name("SelectClause1")))
@protocol SharedSelectClause1 <SharedSelectClause>
@required
@end

__attribute__((swift_name("SelectClause2")))
@protocol SharedSelectClause2 <SharedSelectClause>
@required
@end


/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
__attribute__((swift_name("SelectInstance")))
@protocol SharedSelectInstance
@required
- (void)disposeOnCompletionDisposableHandle:(id<SharedDisposableHandle_>)disposableHandle __attribute__((swift_name("disposeOnCompletion(disposableHandle:)")));
- (void)selectInRegistrationPhaseInternalResult:(id _Nullable)internalResult __attribute__((swift_name("selectInRegistrationPhase(internalResult:)")));
- (BOOL)trySelectClauseObject:(id)clauseObject result:(id _Nullable)result __attribute__((swift_name("trySelect(clauseObject:result:)")));
@property (readonly) id<SharedKotlinCoroutineContext> context __attribute__((swift_name("context")));
@end

__attribute__((swift_name("Mutex")))
@protocol SharedMutex
@required
- (BOOL)holdsLockOwner:(id)owner __attribute__((swift_name("holdsLock(owner:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)lockOwner:(id _Nullable)owner completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("lock(owner:completionHandler:)")));
- (BOOL)tryLockOwner:(id _Nullable)owner __attribute__((swift_name("tryLock(owner:)")));
- (void)unlockOwner:(id _Nullable)owner __attribute__((swift_name("unlock(owner:)")));
@property (readonly) BOOL isLocked __attribute__((swift_name("isLocked")));
@property (readonly) id<SharedSelectClause2> onLock __attribute__((swift_name("onLock"))) __attribute__((deprecated("Mutex.onLock deprecated without replacement. For additional details please refer to #2794")));
@end

__attribute__((swift_name("Semaphore")))
@protocol SharedSemaphore
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)acquireWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("acquire(completionHandler:)")));
- (void)release_ __attribute__((swift_name("release()")));
- (BOOL)tryAcquire __attribute__((swift_name("tryAcquire()")));
@property (readonly) int32_t availablePermits __attribute__((swift_name("availablePermits")));
@end

@interface SharedViewModel (Extensions)
- (void)tryHandlePopParams:(id)params __attribute__((swift_name("tryHandlePop(params:)")));
- (void)tryHandlePushParams:(id)params __attribute__((swift_name("tryHandlePush(params:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinArray")))
@interface SharedKotlinArray<T> : SharedBase
+ (instancetype)arrayWithSize:(int32_t)size init:(T _Nullable (^)(SharedInt *))init __attribute__((swift_name("init(size:init:)")));
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (T _Nullable)getIndex:(int32_t)index __attribute__((swift_name("get(index:)")));
- (id<SharedKotlinIterator>)iterator __attribute__((swift_name("iterator()")));
- (void)setIndex:(int32_t)index value:(T _Nullable)value __attribute__((swift_name("set(index:value:)")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end

@interface SharedKotlinArray (Extensions)
- (id<SharedFlow>)asFlow __attribute__((swift_name("asFlow()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinIntArray")))
@interface SharedKotlinIntArray : SharedBase
+ (instancetype)arrayWithSize:(int32_t)size __attribute__((swift_name("init(size:)")));
+ (instancetype)arrayWithSize:(int32_t)size init:(SharedInt *(^)(SharedInt *))init __attribute__((swift_name("init(size:init:)")));
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (int32_t)getIndex:(int32_t)index __attribute__((swift_name("get(index:)")));
- (SharedKotlinIntIterator *)iterator __attribute__((swift_name("iterator()")));
- (void)setIndex:(int32_t)index value:(int32_t)value __attribute__((swift_name("set(index:value:)")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end

@interface SharedKotlinIntArray (Extensions)
- (id<SharedFlow>)asFlow __attribute__((swift_name("asFlow()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinLongArray")))
@interface SharedKotlinLongArray : SharedBase
+ (instancetype)arrayWithSize:(int32_t)size __attribute__((swift_name("init(size:)")));
+ (instancetype)arrayWithSize:(int32_t)size init:(SharedLong *(^)(SharedInt *))init __attribute__((swift_name("init(size:init:)")));
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (int64_t)getIndex:(int32_t)index __attribute__((swift_name("get(index:)")));
- (SharedKotlinLongIterator *)iterator __attribute__((swift_name("iterator()")));
- (void)setIndex:(int32_t)index value:(int64_t)value __attribute__((swift_name("set(index:value:)")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end

@interface SharedKotlinLongArray (Extensions)
- (id<SharedFlow>)asFlow __attribute__((swift_name("asFlow()")));
@end

__attribute__((swift_name("KotlinIterable")))
@protocol SharedKotlinIterable
@required
- (id<SharedKotlinIterator>)iterator __attribute__((swift_name("iterator()")));
@end

__attribute__((swift_name("KotlinIntProgression")))
@interface SharedKotlinIntProgression : SharedBase <SharedKotlinIterable>
@property (class, readonly, getter=companion) SharedKotlinIntProgressionCompanion *companion __attribute__((swift_name("companion")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (BOOL)isEmpty_ __attribute__((swift_name("isEmpty()")));
- (SharedKotlinIntIterator *)iterator __attribute__((swift_name("iterator()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t first __attribute__((swift_name("first")));
@property (readonly) int32_t last __attribute__((swift_name("last")));
@property (readonly) int32_t step __attribute__((swift_name("step")));
@end

__attribute__((swift_name("KotlinClosedRange")))
@protocol SharedKotlinClosedRange
@required
- (BOOL)containsValue:(id)value __attribute__((swift_name("contains(value:)")));
- (BOOL)isEmpty_ __attribute__((swift_name("isEmpty()")));
@property (readonly) id endInclusive __attribute__((swift_name("endInclusive")));
@property (readonly, getter=start_) id start __attribute__((swift_name("start")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.9")
*/
__attribute__((swift_name("KotlinOpenEndRange")))
@protocol SharedKotlinOpenEndRange
@required
- (BOOL)containsValue_:(id)value __attribute__((swift_name("contains(value_:)")));
- (BOOL)isEmpty_ __attribute__((swift_name("isEmpty()")));
@property (readonly) id endExclusive __attribute__((swift_name("endExclusive")));
@property (readonly, getter=start_) id start __attribute__((swift_name("start")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinIntRange")))
@interface SharedKotlinIntRange : SharedKotlinIntProgression <SharedKotlinClosedRange, SharedKotlinOpenEndRange>
- (instancetype)initWithStart:(int32_t)start endInclusive:(int32_t)endInclusive __attribute__((swift_name("init(start:endInclusive:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) SharedKotlinIntRangeCompanion *companion __attribute__((swift_name("companion")));
- (BOOL)containsValue:(SharedInt *)value __attribute__((swift_name("contains(value:)")));
- (BOOL)containsValue_:(SharedInt *)value __attribute__((swift_name("contains(value_:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (BOOL)isEmpty_ __attribute__((swift_name("isEmpty()")));
- (NSString *)description __attribute__((swift_name("description()")));

/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.9")
*/
@property (readonly) SharedInt *endExclusive __attribute__((swift_name("endExclusive"))) __attribute__((deprecated("Can throw an exception when it's impossible to represent the value with Int type, for example, when the range includes MAX_VALUE. It's recommended to use 'endInclusive' property that doesn't throw.")));
@property (readonly) SharedInt *endInclusive __attribute__((swift_name("endInclusive")));
@property (readonly, getter=start_) SharedInt *start __attribute__((swift_name("start")));
@end

@interface SharedKotlinIntRange (Extensions)
- (id<SharedFlow>)asFlow __attribute__((swift_name("asFlow()")));
@end

__attribute__((swift_name("KotlinLongProgression")))
@interface SharedKotlinLongProgression : SharedBase <SharedKotlinIterable>
@property (class, readonly, getter=companion) SharedKotlinLongProgressionCompanion *companion __attribute__((swift_name("companion")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (BOOL)isEmpty_ __attribute__((swift_name("isEmpty()")));
- (SharedKotlinLongIterator *)iterator __attribute__((swift_name("iterator()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int64_t first __attribute__((swift_name("first")));
@property (readonly) int64_t last __attribute__((swift_name("last")));
@property (readonly) int64_t step __attribute__((swift_name("step")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinLongRange")))
@interface SharedKotlinLongRange : SharedKotlinLongProgression <SharedKotlinClosedRange, SharedKotlinOpenEndRange>
- (instancetype)initWithStart:(int64_t)start endInclusive:(int64_t)endInclusive __attribute__((swift_name("init(start:endInclusive:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) SharedKotlinLongRangeCompanion *companion __attribute__((swift_name("companion")));
- (BOOL)containsValue:(SharedLong *)value __attribute__((swift_name("contains(value:)")));
- (BOOL)containsValue_:(SharedLong *)value __attribute__((swift_name("contains(value_:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (BOOL)isEmpty_ __attribute__((swift_name("isEmpty()")));
- (NSString *)description __attribute__((swift_name("description()")));

/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.9")
*/
@property (readonly) SharedLong *endExclusive __attribute__((swift_name("endExclusive"))) __attribute__((deprecated("Can throw an exception when it's impossible to represent the value with Long type, for example, when the range includes MAX_VALUE. It's recommended to use 'endInclusive' property that doesn't throw.")));
@property (readonly) SharedLong *endInclusive __attribute__((swift_name("endInclusive")));
@property (readonly, getter=start_) SharedLong *start __attribute__((swift_name("start")));
@end

@interface SharedKotlinLongRange (Extensions)
- (id<SharedFlow>)asFlow __attribute__((swift_name("asFlow()")));
@end

@interface SharedCoroutineDispatcher (Extensions)

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeBlock:(id<SharedKotlinSuspendFunction1>)block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(block:completionHandler:)")));
@end

@interface SharedDispatchers (Extensions)
@property (readonly) SharedCoroutineDispatcher *IO __attribute__((swift_name("IO")));
@end

@interface SharedSharingStartedCompanion (Extensions)
- (id<SharedSharingStarted>)WhileSubscribedStopTimeout:(int64_t)stopTimeout replayExpiration:(int64_t)replayExpiration __attribute__((swift_name("WhileSubscribed(stopTimeout:replayExpiration:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AwaitKt")))
@interface SharedAwaitKt : SharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)awaitAll:(id)receiver completionHandler:(void (^)(NSArray<id> * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("awaitAll(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)awaitAllDeferreds:(SharedKotlinArray<id<SharedDeferred>> *)deferreds completionHandler:(void (^)(NSArray<id> * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("awaitAll(deferreds:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)joinAll:(id)receiver completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("joinAll(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)joinAllJobs:(SharedKotlinArray<id<SharedJob>> *)jobs completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("joinAll(jobs:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("BroadcastKt")))
@interface SharedBroadcastKt : SharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.ObsoleteCoroutinesApi
*/
+ (id<SharedBroadcastChannel>)broadcast:(id<SharedReceiveChannel>)receiver capacity:(int32_t)capacity start:(SharedCoroutineStart *)start __attribute__((swift_name("broadcast(_:capacity:start:)"))) __attribute__((unavailable("BroadcastChannel is deprecated in the favour of SharedFlow and is no longer supported")));

/**
 * @note annotations
 *   kotlinx.coroutines.ObsoleteCoroutinesApi
*/
+ (id<SharedBroadcastChannel>)broadcast:(id<SharedCoroutineScope>)receiver context:(id<SharedKotlinCoroutineContext>)context capacity:(int32_t)capacity start:(SharedCoroutineStart *)start onCompletion:(void (^ _Nullable)(SharedKotlinThrowable * _Nullable))onCompletion block:(id<SharedKotlinSuspendFunction1>)block __attribute__((swift_name("broadcast(_:context:capacity:start:onCompletion:block:)"))) __attribute__((unavailable("BroadcastChannel is deprecated in the favour of SharedFlow and is no longer supported")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("BroadcastChannelKt")))
@interface SharedBroadcastChannelKt : SharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.ObsoleteCoroutinesApi
*/
+ (id<SharedBroadcastChannel>)BroadcastChannelCapacity:(int32_t)capacity __attribute__((swift_name("BroadcastChannel(capacity:)"))) __attribute__((unavailable("BroadcastChannel is deprecated in the favour of SharedFlow and StateFlow, and is no longer supported")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Builders_commonKt")))
@interface SharedBuilders_commonKt : SharedBase
+ (id<SharedDeferred>)async:(id<SharedCoroutineScope>)receiver context:(id<SharedKotlinCoroutineContext>)context start:(SharedCoroutineStart *)start block:(id<SharedKotlinSuspendFunction1>)block __attribute__((swift_name("async(_:context:start:block:)")));
+ (id<SharedJob>)launch:(id<SharedCoroutineScope>)receiver context:(id<SharedKotlinCoroutineContext>)context start:(SharedCoroutineStart *)start block:(id<SharedKotlinSuspendFunction1>)block __attribute__((swift_name("launch(_:context:start:block:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)withContextContext:(id<SharedKotlinCoroutineContext>)context block:(id<SharedKotlinSuspendFunction1>)block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("withContext(context:block:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("BuildersKt")))
@interface SharedBuildersKt : SharedBase
+ (id<SharedFlow>)asFlow:(id _Nullable (^)(void))receiver __attribute__((swift_name("asFlow(_:)")));
+ (id<SharedFlow>)asFlow_:(id)receiver __attribute__((swift_name("asFlow(__:)")));
+ (id<SharedFlow>)asFlow__:(id<SharedKotlinIterator>)receiver __attribute__((swift_name("asFlow(___:)")));
+ (id<SharedFlow>)asFlow___:(id<SharedKotlinSuspendFunction0>)receiver __attribute__((swift_name("asFlow(____:)")));
+ (id<SharedFlow>)asFlow____:(id<SharedKotlinSequence>)receiver __attribute__((swift_name("asFlow(_____:)")));
+ (id<SharedFlow>)callbackFlowBlock:(id<SharedKotlinSuspendFunction1>)block __attribute__((swift_name("callbackFlow(block:)")));
+ (id<SharedFlow>)channelFlowBlock:(id<SharedKotlinSuspendFunction1>)block __attribute__((swift_name("channelFlow(block:)")));
+ (id<SharedFlow>)emptyFlow __attribute__((swift_name("emptyFlow()")));
+ (id<SharedFlow>)flowBlock:(id<SharedKotlinSuspendFunction1>)block __attribute__((swift_name("flow(block:)")));
+ (id<SharedFlow>)flowOfValue:(id _Nullable)value __attribute__((swift_name("flowOf(value:)")));
+ (id<SharedFlow>)flowOfElements:(SharedKotlinArray<id> *)elements __attribute__((swift_name("flowOf(elements:)")));
+ (id _Nullable)runBlockingContext:(id<SharedKotlinCoroutineContext>)context block:(id<SharedKotlinSuspendFunction1>)block __attribute__((swift_name("runBlocking(context:block:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CFlowKt")))
@interface SharedCFlowKt : SharedBase
+ (SharedCFlow<id> *)asCFlow:(id<SharedStateFlow>)receiver __attribute__((swift_name("asCFlow(_:)")));
+ (SharedMutableCFlow<id> *)asMutableCFlow:(id<SharedMutableStateFlow>)receiver __attribute__((swift_name("asMutableCFlow(_:)")));
+ (SharedAnyKmpObjectFlow *)asSwiftFlow:(id<SharedMutableStateFlow>)receiver __attribute__((swift_name("asSwiftFlow(_:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CancellableKt")))
@interface SharedCancellableKt : SharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
+ (void)startCoroutineCancellable:(id<SharedKotlinSuspendFunction0>)receiver completion:(id<SharedKotlinContinuation>)completion __attribute__((swift_name("startCoroutineCancellable(_:completion:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CancellableContinuationKt")))
@interface SharedCancellableContinuationKt : SharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
+ (void)disposeOnCancellation:(id<SharedCancellableContinuation>)receiver handle:(id<SharedDisposableHandle_>)handle __attribute__((swift_name("disposeOnCancellation(_:handle:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)suspendCancellableCoroutineBlock:(void (^)(id<SharedCancellableContinuation>))block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("suspendCancellableCoroutine(block:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ChannelKt")))
@interface SharedChannelKt : SharedBase
+ (id<SharedChannel>)ChannelCapacity:(int32_t)capacity onBufferOverflow:(SharedBufferOverflow *)onBufferOverflow onUndeliveredElement:(void (^ _Nullable)(id _Nullable))onUndeliveredElement __attribute__((swift_name("Channel(capacity:onBufferOverflow:onUndeliveredElement:)")));
+ (id _Nullable)getOrElse:(id _Nullable)receiver onFailure:(id _Nullable (^)(SharedKotlinThrowable * _Nullable))onFailure __attribute__((swift_name("getOrElse(_:onFailure:)")));
+ (id _Nullable)onClosed:(id _Nullable)receiver action:(void (^)(SharedKotlinThrowable * _Nullable))action __attribute__((swift_name("onClosed(_:action:)")));
+ (id _Nullable)onFailure:(id _Nullable)receiver action:(void (^)(SharedKotlinThrowable * _Nullable))action __attribute__((swift_name("onFailure(_:action:)")));
+ (id _Nullable)onSuccess:(id _Nullable)receiver action:(void (^)(id _Nullable))action __attribute__((swift_name("onSuccess(_:action:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Channels_commonKt")))
@interface SharedChannels_commonKt : SharedBase
+ (id _Nullable)consume:(id<SharedReceiveChannel>)receiver block:(id _Nullable (^)(id<SharedReceiveChannel>))block __attribute__((swift_name("consume(_:block:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)consumeEach:(id<SharedReceiveChannel>)receiver action:(void (^)(id _Nullable))action completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("consumeEach(_:action:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)toList:(id<SharedReceiveChannel>)receiver completionHandler:(void (^)(NSArray<id> * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("toList(_:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ChannelsKt")))
@interface SharedChannelsKt : SharedBase
+ (id<SharedFlow>)consumeAsFlow:(id<SharedReceiveChannel>)receiver __attribute__((swift_name("consumeAsFlow(_:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)emitAll:(id<SharedFlowCollector>)receiver channel:(id<SharedReceiveChannel>)channel completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("emitAll(_:channel:completionHandler:)")));
+ (id<SharedReceiveChannel>)produceIn:(id<SharedFlow>)receiver scope:(id<SharedCoroutineScope>)scope __attribute__((swift_name("produceIn(_:scope:)")));
+ (id<SharedFlow>)receiveAsFlow:(id<SharedReceiveChannel>)receiver __attribute__((swift_name("receiveAsFlow(_:)")));
+ (id _Nullable)trySendBlocking:(id<SharedSendChannel>)receiver element:(id _Nullable)element __attribute__((swift_name("trySendBlocking(_:element:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CollectKt")))
@interface SharedCollectKt : SharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)collect:(id<SharedFlow>)receiver completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("collect(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)collectIndexed:(id<SharedFlow>)receiver action:(id<SharedKotlinSuspendFunction2>)action completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("collectIndexed(_:action:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)collectLatest:(id<SharedFlow>)receiver action:(id<SharedKotlinSuspendFunction1>)action completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("collectLatest(_:action:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)emitAll:(id<SharedFlowCollector>)receiver flow:(id<SharedFlow>)flow completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("emitAll(_:flow:completionHandler:)")));
+ (id<SharedJob>)launchIn:(id<SharedFlow>)receiver scope:(id<SharedCoroutineScope>)scope __attribute__((swift_name("launchIn(_:scope:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CollectionKt")))
@interface SharedCollectionKt : SharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)toCollection:(id<SharedFlow>)receiver destination:(id)destination completionHandler:(void (^)(id _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("toCollection(_:destination:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)toList:(id<SharedFlow>)receiver destination:(NSMutableArray<id> *)destination completionHandler:(void (^)(NSArray<id> * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("toList(_:destination:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)toSet:(id<SharedFlow>)receiver destination:(SharedMutableSet<id> *)destination completionHandler:(void (^)(NSSet<id> * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("toSet(_:destination:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CompletableDeferredKt")))
@interface SharedCompletableDeferredKt : SharedBase
+ (id<SharedCompletableDeferred>)CompletableDeferredValue:(id _Nullable)value __attribute__((swift_name("CompletableDeferred(value:)")));
+ (id<SharedCompletableDeferred>)CompletableDeferredParent:(id<SharedJob> _Nullable)parent __attribute__((swift_name("CompletableDeferred(parent:)")));
+ (BOOL)completeWith:(id<SharedCompletableDeferred>)receiver result:(id _Nullable)result __attribute__((swift_name("completeWith(_:result:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ContextKt")))
@interface SharedContextKt : SharedBase
+ (id<SharedFlow>)buffer:(id<SharedFlow>)receiver capacity:(int32_t)capacity onBufferOverflow:(SharedBufferOverflow *)onBufferOverflow __attribute__((swift_name("buffer(_:capacity:onBufferOverflow:)")));
+ (id<SharedFlow>)cancellable:(id<SharedFlow>)receiver __attribute__((swift_name("cancellable(_:)")));
+ (id<SharedFlow>)conflate:(id<SharedFlow>)receiver __attribute__((swift_name("conflate(_:)")));
+ (id<SharedFlow>)flowOn:(id<SharedFlow>)receiver context:(id<SharedKotlinCoroutineContext>)context __attribute__((swift_name("flowOn(_:context:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoroutineContextKt")))
@interface SharedCoroutineContextKt : SharedBase
+ (id<SharedKotlinCoroutineContext>)doNewCoroutineContext:(id<SharedKotlinCoroutineContext>)receiver addedContext:(id<SharedKotlinCoroutineContext>)addedContext __attribute__((swift_name("doNewCoroutineContext(_:addedContext:)")));
+ (id<SharedKotlinCoroutineContext>)doNewCoroutineContext:(id<SharedCoroutineScope>)receiver context:(id<SharedKotlinCoroutineContext>)context __attribute__((swift_name("doNewCoroutineContext(_:context:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoroutineExceptionHandlerKt")))
@interface SharedCoroutineExceptionHandlerKt : SharedBase
+ (id<SharedCoroutineExceptionHandler>)CoroutineExceptionHandlerHandler:(void (^)(id<SharedKotlinCoroutineContext>, SharedKotlinThrowable *))handler __attribute__((swift_name("CoroutineExceptionHandler(handler:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
+ (void)handleCoroutineExceptionContext:(id<SharedKotlinCoroutineContext>)context exception:(SharedKotlinThrowable *)exception __attribute__((swift_name("handleCoroutineException(context:exception:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoroutineScopeKt")))
@interface SharedCoroutineScopeKt : SharedBase
+ (BOOL)isActive:(id<SharedCoroutineScope>)receiver __attribute__((swift_name("isActive(_:)")));
+ (id<SharedCoroutineScope>)CoroutineScopeContext:(id<SharedKotlinCoroutineContext>)context __attribute__((swift_name("CoroutineScope(context:)")));
+ (id<SharedCoroutineScope>)MainScope __attribute__((swift_name("MainScope()")));
+ (void)cancel:(id<SharedCoroutineScope>)receiver cause:(SharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(_:cause:)")));
+ (void)cancel:(id<SharedCoroutineScope>)receiver message:(NSString *)message cause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("cancel(_:message:cause:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)coroutineScopeBlock:(id<SharedKotlinSuspendFunction1>)block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("coroutineScope(block:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)currentCoroutineContextWithCompletionHandler:(void (^)(id<SharedKotlinCoroutineContext> _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("currentCoroutineContext(completionHandler:)")));
+ (void)ensureActive:(id<SharedCoroutineScope>)receiver __attribute__((swift_name("ensureActive(_:)")));
+ (id<SharedCoroutineScope>)plus:(id<SharedCoroutineScope>)receiver context:(id<SharedKotlinCoroutineContext>)context __attribute__((swift_name("plus(_:context:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CountKt")))
@interface SharedCountKt : SharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)count:(id<SharedFlow>)receiver completionHandler:(void (^)(SharedInt * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("count(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)count:(id<SharedFlow>)receiver predicate:(id<SharedKotlinSuspendFunction1>)predicate completionHandler:(void (^)(SharedInt * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("count(_:predicate:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DelayKt")))
@interface SharedDelayKt : SharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)awaitCancellationWithCompletionHandler:(void (^)(SharedKotlinNothing * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("awaitCancellation(completionHandler:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
*/
+ (id<SharedFlow>)debounce:(id<SharedFlow>)receiver timeoutMillis:(SharedLong *(^)(id _Nullable))timeoutMillis __attribute__((swift_name("debounce(_:timeoutMillis:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
 *   kotlin.jvm.JvmName(name="debounceDuration")
*/
+ (id<SharedFlow>)debounce:(id<SharedFlow>)receiver timeout:(id (^)(id _Nullable))timeout __attribute__((swift_name("debounce(_:timeout:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
*/
+ (id<SharedFlow>)debounce:(id<SharedFlow>)receiver timeoutMillis_:(int64_t)timeoutMillis __attribute__((swift_name("debounce(_:timeoutMillis_:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
*/
+ (id<SharedFlow>)debounce:(id<SharedFlow>)receiver timeout_:(int64_t)timeout __attribute__((swift_name("debounce(_:timeout_:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)delayTimeMillis:(int64_t)timeMillis completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("delay(timeMillis:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)delayDuration:(int64_t)duration completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("delay(duration:completionHandler:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
*/
+ (id<SharedFlow>)sample:(id<SharedFlow>)receiver periodMillis:(int64_t)periodMillis __attribute__((swift_name("sample(_:periodMillis:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
*/
+ (id<SharedFlow>)sample:(id<SharedFlow>)receiver period:(int64_t)period __attribute__((swift_name("sample(_:period:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
*/
+ (id<SharedFlow>)timeout:(id<SharedFlow>)receiver timeout:(int64_t)timeout __attribute__((swift_name("timeout(_:timeout:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DeprecatedKt")))
@interface SharedDeprecatedKt : SharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.ObsoleteCoroutinesApi
*/
+ (id _Nullable)consume:(id<SharedBroadcastChannel>)receiver block:(id _Nullable (^)(id<SharedReceiveChannel>))block __attribute__((swift_name("consume(_:block:)"))) __attribute__((unavailable("BroadcastChannel is deprecated in the favour of SharedFlow and is no longer supported")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)consumeEach:(id<SharedBroadcastChannel>)receiver action:(void (^)(id _Nullable))action completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("consumeEach(_:action:completionHandler:)"))) __attribute__((unavailable("BroadcastChannel is deprecated in the favour of SharedFlow and is no longer supported")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DispatchedContinuationKt")))
@interface SharedDispatchedContinuationKt : SharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
+ (void)resumeCancellableWith:(id<SharedKotlinContinuation>)receiver result:(id _Nullable)result __attribute__((swift_name("resumeCancellableWith(_:result:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DistinctKt")))
@interface SharedDistinctKt : SharedBase
+ (id<SharedFlow>)distinctUntilChanged:(id<SharedFlow>)receiver __attribute__((swift_name("distinctUntilChanged(_:)")));
+ (id<SharedFlow>)distinctUntilChanged:(id<SharedFlow>)receiver areEquivalent:(SharedBoolean *(^)(id _Nullable, id _Nullable))areEquivalent __attribute__((swift_name("distinctUntilChanged(_:areEquivalent:)")));
+ (id<SharedFlow>)distinctUntilChangedBy:(id<SharedFlow>)receiver keySelector:(id _Nullable (^)(id _Nullable))keySelector __attribute__((swift_name("distinctUntilChangedBy(_:keySelector:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("EmittersKt")))
@interface SharedEmittersKt : SharedBase
+ (id<SharedFlow>)onCompletion:(id<SharedFlow>)receiver action:(id<SharedKotlinSuspendFunction2>)action __attribute__((swift_name("onCompletion(_:action:)")));
+ (id<SharedFlow>)onEmpty:(id<SharedFlow>)receiver action:(id<SharedKotlinSuspendFunction1>)action __attribute__((swift_name("onEmpty(_:action:)")));
+ (id<SharedFlow>)onStart:(id<SharedFlow>)receiver action:(id<SharedKotlinSuspendFunction1>)action __attribute__((swift_name("onStart(_:action:)")));
+ (id<SharedFlow>)transform:(id<SharedFlow>)receiver transform:(id<SharedKotlinSuspendFunction2>)transform __attribute__((swift_name("transform(_:transform:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ErrorsKt")))
@interface SharedErrorsKt : SharedBase
+ (id<SharedFlow>)catch:(id<SharedFlow>)receiver action:(id<SharedKotlinSuspendFunction2>)action __attribute__((swift_name("catch(_:action:)")));
+ (id<SharedFlow>)retry:(id<SharedFlow>)receiver retries:(int64_t)retries predicate:(id<SharedKotlinSuspendFunction1>)predicate __attribute__((swift_name("retry(_:retries:predicate:)")));
+ (id<SharedFlow>)retryWhen:(id<SharedFlow>)receiver predicate:(id<SharedKotlinSuspendFunction3>)predicate __attribute__((swift_name("retryWhen(_:predicate:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ExceptionsKt")))
@interface SharedExceptionsKt : SharedBase
+ (SharedKotlinCancellationException *)CancellationExceptionMessage:(NSString * _Nullable)message cause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("CancellationException(message:cause:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("JobKt")))
@interface SharedJobKt : SharedBase
+ (BOOL)isActive:(id<SharedKotlinCoroutineContext>)receiver __attribute__((swift_name("isActive(_:)")));
+ (id<SharedJob>)job:(id<SharedKotlinCoroutineContext>)receiver __attribute__((swift_name("job(_:)")));
+ (id<SharedCompletableJob>)JobParent:(id<SharedJob> _Nullable)parent __attribute__((swift_name("Job(parent:)")));
+ (void)cancel:(id<SharedKotlinCoroutineContext>)receiver cause:(SharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(_:cause:)")));
+ (void)cancel:(id<SharedJob>)receiver message:(NSString *)message cause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("cancel(_:message:cause:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)cancelAndJoin:(id<SharedJob>)receiver completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("cancelAndJoin(_:completionHandler:)")));
+ (void)cancelChildren:(id<SharedKotlinCoroutineContext>)receiver cause:(SharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancelChildren(_:cause:)")));
+ (void)cancelChildren:(id<SharedJob>)receiver cause_:(SharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancelChildren(_:cause_:)")));
+ (void)ensureActive:(id<SharedKotlinCoroutineContext>)receiver __attribute__((swift_name("ensureActive(_:)")));
+ (void)ensureActive_:(id<SharedJob>)receiver __attribute__((swift_name("ensureActive(__:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("LabelExtensionsKt")))
@interface SharedLabelExtensionsKt : SharedBase
+ (id<SharedDisposableHandle_>)bindText:(UILabel *)receiver scope:(id<SharedCoroutineScope>)scope flow:(id<SharedStateFlow>)flow __attribute__((swift_name("bindText(_:scope:flow:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("LimitKt")))
@interface SharedLimitKt : SharedBase
+ (id<SharedFlow>)drop:(id<SharedFlow>)receiver count:(int32_t)count __attribute__((swift_name("drop(_:count:)")));
+ (id<SharedFlow>)dropWhile:(id<SharedFlow>)receiver predicate:(id<SharedKotlinSuspendFunction1>)predicate __attribute__((swift_name("dropWhile(_:predicate:)")));
+ (id<SharedFlow>)take:(id<SharedFlow>)receiver count:(int32_t)count __attribute__((swift_name("take(_:count:)")));
+ (id<SharedFlow>)takeWhile:(id<SharedFlow>)receiver predicate:(id<SharedKotlinSuspendFunction1>)predicate __attribute__((swift_name("takeWhile(_:predicate:)")));
+ (id<SharedFlow>)transformWhile:(id<SharedFlow>)receiver transform:(id<SharedKotlinSuspendFunction2>)transform __attribute__((swift_name("transformWhile(_:transform:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("LintKt")))
@interface SharedLintKt : SharedBase
+ (id<SharedKotlinCoroutineContext>)coroutineContext:(id<SharedFlowCollector>)receiver __attribute__((swift_name("coroutineContext(_:)"))) __attribute__((unavailable("coroutineContext is resolved into the property of outer CoroutineScope which is likely to be an error. Use currentCoroutineContext() instead or specify the receiver of coroutineContext explicitly")));
+ (BOOL)isActive:(id<SharedFlowCollector>)receiver __attribute__((swift_name("isActive(_:)"))) __attribute__((unavailable("isActive is resolved into the extension of outer CoroutineScope which is likely to be an error. Use currentCoroutineContext().isActive or cancellable() operator instead or specify the receiver of isActive explicitly. Additionally, flow {} builder emissions are cancellable by default.")));
+ (void)cancel:(id<SharedFlowCollector>)receiver cause:(SharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(_:cause:)"))) __attribute__((unavailable("cancel() is resolved into the extension of outer CoroutineScope which is likely to be an error. Use currentCoroutineContext().cancel() instead or specify the receiver of cancel() explicitly")));
+ (id<SharedFlow>)cancellable:(id<SharedSharedFlow>)receiver __attribute__((swift_name("cancellable(_:)"))) __attribute__((unavailable("Applying 'cancellable' to a SharedFlow has no effect. See the SharedFlow documentation on Operator Fusion.")));
+ (id<SharedFlow>)catch:(id<SharedSharedFlow>)receiver action:(id<SharedKotlinSuspendFunction2>)action __attribute__((swift_name("catch(_:action:)"))) __attribute__((deprecated("SharedFlow never completes, so this operator typically has not effect, it can only catch exceptions from 'onSubscribe' operator")));
+ (id<SharedFlow>)conflate:(id<SharedStateFlow>)receiver __attribute__((swift_name("conflate(_:)"))) __attribute__((unavailable("Applying 'conflate' to StateFlow has no effect. See the StateFlow documentation on Operator Fusion.")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)count:(id<SharedSharedFlow>)receiver completionHandler:(void (^)(SharedInt * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("count(_:completionHandler:)"))) __attribute__((deprecated("SharedFlow never completes, so this terminal operation never completes.")));
+ (id<SharedFlow>)distinctUntilChanged:(id<SharedStateFlow>)receiver __attribute__((swift_name("distinctUntilChanged(_:)"))) __attribute__((unavailable("Applying 'distinctUntilChanged' to StateFlow has no effect. See the StateFlow documentation on Operator Fusion.")));
+ (id<SharedFlow>)flowOn:(id<SharedSharedFlow>)receiver context:(id<SharedKotlinCoroutineContext>)context __attribute__((swift_name("flowOn(_:context:)"))) __attribute__((unavailable("Applying 'flowOn' to SharedFlow has no effect. See the SharedFlow documentation on Operator Fusion.")));
+ (id<SharedFlow>)retry:(id<SharedSharedFlow>)receiver retries:(int64_t)retries predicate:(id<SharedKotlinSuspendFunction1>)predicate __attribute__((swift_name("retry(_:retries:predicate:)"))) __attribute__((deprecated("SharedFlow never completes, so this operator has no effect.")));
+ (id<SharedFlow>)retryWhen:(id<SharedSharedFlow>)receiver predicate:(id<SharedKotlinSuspendFunction3>)predicate __attribute__((swift_name("retryWhen(_:predicate:)"))) __attribute__((deprecated("SharedFlow never completes, so this operator has no effect.")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)toList:(id<SharedSharedFlow>)receiver completionHandler:(void (^)(NSArray<id> * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("toList(_:completionHandler:)"))) __attribute__((deprecated("SharedFlow never completes, so this terminal operation never completes.")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)toList:(id<SharedSharedFlow>)receiver destination:(NSMutableArray<id> *)destination completionHandler:(void (^)(SharedKotlinNothing * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("toList(_:destination:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)toSet:(id<SharedSharedFlow>)receiver completionHandler:(void (^)(NSSet<id> * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("toSet(_:completionHandler:)"))) __attribute__((deprecated("SharedFlow never completes, so this terminal operation never completes.")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)toSet:(id<SharedSharedFlow>)receiver destination:(SharedMutableSet<id> *)destination completionHandler:(void (^)(SharedKotlinNothing * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("toSet(_:destination:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("LogicKt")))
@interface SharedLogicKt : SharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)all:(id<SharedFlow>)receiver predicate:(id<SharedKotlinSuspendFunction1>)predicate completionHandler:(void (^)(SharedBoolean * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("all(_:predicate:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)any:(id<SharedFlow>)receiver predicate:(id<SharedKotlinSuspendFunction1>)predicate completionHandler:(void (^)(SharedBoolean * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("any(_:predicate:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)none:(id<SharedFlow>)receiver predicate:(id<SharedKotlinSuspendFunction1>)predicate completionHandler:(void (^)(SharedBoolean * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("none(_:predicate:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MergeKt")))
@interface SharedMergeKt : SharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (id<SharedFlow>)flatMapConcat:(id<SharedFlow>)receiver transform:(id<SharedKotlinSuspendFunction1>)transform __attribute__((swift_name("flatMapConcat(_:transform:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (id<SharedFlow>)flatMapLatest:(id<SharedFlow>)receiver transform:(id<SharedKotlinSuspendFunction1>)transform __attribute__((swift_name("flatMapLatest(_:transform:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (id<SharedFlow>)flatMapMerge:(id<SharedFlow>)receiver concurrency:(int32_t)concurrency transform:(id<SharedKotlinSuspendFunction1>)transform __attribute__((swift_name("flatMapMerge(_:concurrency:transform:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (id<SharedFlow>)flattenConcat:(id<SharedFlow>)receiver __attribute__((swift_name("flattenConcat(_:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (id<SharedFlow>)flattenMerge:(id<SharedFlow>)receiver concurrency:(int32_t)concurrency __attribute__((swift_name("flattenMerge(_:concurrency:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (id<SharedFlow>)mapLatest:(id<SharedFlow>)receiver transform:(id<SharedKotlinSuspendFunction1>)transform __attribute__((swift_name("mapLatest(_:transform:)")));
+ (id<SharedFlow>)merge:(id)receiver __attribute__((swift_name("merge(_:)")));
+ (id<SharedFlow>)mergeFlows:(SharedKotlinArray<id<SharedFlow>> *)flows __attribute__((swift_name("merge(flows:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (id<SharedFlow>)transformLatest:(id<SharedFlow>)receiver transform:(id<SharedKotlinSuspendFunction2>)transform __attribute__((swift_name("transformLatest(_:transform:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
*/
@property (class, readonly) int32_t DEFAULT_CONCURRENCY __attribute__((swift_name("DEFAULT_CONCURRENCY")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
*/
@property (class, readonly) NSString *DEFAULT_CONCURRENCY_PROPERTY_NAME __attribute__((swift_name("DEFAULT_CONCURRENCY_PROPERTY_NAME")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MigrationKt")))
@interface SharedMigrationKt : SharedBase
+ (id<SharedFlow>)cache:(id<SharedFlow>)receiver __attribute__((swift_name("cache(_:)"))) __attribute__((unavailable("Flow analogue of 'cache()' is 'shareIn' with unlimited replay and 'started = SharingStarted.Lazily' argument'")));
+ (id<SharedFlow>)combineLatest:(id<SharedFlow>)receiver other:(id<SharedFlow>)other transform:(id<SharedKotlinSuspendFunction2>)transform __attribute__((swift_name("combineLatest(_:other:transform:)"))) __attribute__((unavailable("Flow analogue of 'combineLatest' is 'combine'")));
+ (id<SharedFlow>)combineLatest:(id<SharedFlow>)receiver other:(id<SharedFlow>)other other2:(id<SharedFlow>)other2 transform:(id<SharedKotlinSuspendFunction3>)transform __attribute__((swift_name("combineLatest(_:other:other2:transform:)"))) __attribute__((unavailable("Flow analogue of 'combineLatest' is 'combine'")));
+ (id<SharedFlow>)combineLatest:(id<SharedFlow>)receiver other:(id<SharedFlow>)other other2:(id<SharedFlow>)other2 other3:(id<SharedFlow>)other3 transform:(id<SharedKotlinSuspendFunction4>)transform __attribute__((swift_name("combineLatest(_:other:other2:other3:transform:)"))) __attribute__((unavailable("Flow analogue of 'combineLatest' is 'combine'")));
+ (id<SharedFlow>)combineLatest:(id<SharedFlow>)receiver other:(id<SharedFlow>)other other2:(id<SharedFlow>)other2 other3:(id<SharedFlow>)other3 other4:(id<SharedFlow>)other4 transform:(id<SharedKotlinSuspendFunction5>)transform __attribute__((swift_name("combineLatest(_:other:other2:other3:other4:transform:)"))) __attribute__((unavailable("Flow analogue of 'combineLatest' is 'combine'")));
+ (id<SharedFlow>)compose:(id<SharedFlow>)receiver transformer:(id<SharedFlow> (^)(id<SharedFlow>))transformer __attribute__((swift_name("compose(_:transformer:)"))) __attribute__((unavailable("Flow analogue of 'compose' is 'let'")));
+ (id<SharedFlow>)concatMap:(id<SharedFlow>)receiver mapper:(id<SharedFlow> (^)(id _Nullable))mapper __attribute__((swift_name("concatMap(_:mapper:)"))) __attribute__((unavailable("Flow analogue of 'concatMap' is 'flatMapConcat'")));
+ (id<SharedFlow>)concatWith:(id<SharedFlow>)receiver value:(id _Nullable)value __attribute__((swift_name("concatWith(_:value:)"))) __attribute__((unavailable("Flow analogue of 'concatWith' is 'onCompletion'. Use 'onCompletion { emit(value) }'")));
+ (id<SharedFlow>)concatWith:(id<SharedFlow>)receiver other:(id<SharedFlow>)other __attribute__((swift_name("concatWith(_:other:)"))) __attribute__((unavailable("Flow analogue of 'concatWith' is 'onCompletion'. Use 'onCompletion { if (it == null) emitAll(other) }'")));
+ (id<SharedFlow>)delayEach:(id<SharedFlow>)receiver timeMillis:(int64_t)timeMillis __attribute__((swift_name("delayEach(_:timeMillis:)"))) __attribute__((unavailable("Use 'onEach { delay(timeMillis) }'")));
+ (id<SharedFlow>)delayFlow:(id<SharedFlow>)receiver timeMillis:(int64_t)timeMillis __attribute__((swift_name("delayFlow(_:timeMillis:)"))) __attribute__((unavailable("Use 'onStart { delay(timeMillis) }'")));
+ (id<SharedFlow>)flatMap:(id<SharedFlow>)receiver mapper:(id<SharedKotlinSuspendFunction1>)mapper __attribute__((swift_name("flatMap(_:mapper:)"))) __attribute__((unavailable("Flow analogue is 'flatMapConcat'")));
+ (id<SharedFlow>)flatten:(id<SharedFlow>)receiver __attribute__((swift_name("flatten(_:)"))) __attribute__((unavailable("Flow analogue of 'flatten' is 'flattenConcat'")));
+ (void)forEach:(id<SharedFlow>)receiver action:(id<SharedKotlinSuspendFunction1>)action __attribute__((swift_name("forEach(_:action:)"))) __attribute__((unavailable("Flow analogue of 'forEach' is 'collect'")));
+ (id<SharedFlow>)merge:(id<SharedFlow>)receiver __attribute__((swift_name("merge(_:)"))) __attribute__((unavailable("Flow analogue of 'merge' is 'flattenConcat'")));
+ (id<SharedFlow>)observeOn:(id<SharedFlow>)receiver context:(id<SharedKotlinCoroutineContext>)context __attribute__((swift_name("observeOn(_:context:)"))) __attribute__((unavailable("Collect flow in the desired context instead")));
+ (id<SharedFlow>)onErrorResume:(id<SharedFlow>)receiver fallback:(id<SharedFlow>)fallback __attribute__((swift_name("onErrorResume(_:fallback:)"))) __attribute__((unavailable("Flow analogue of 'onErrorXxx' is 'catch'. Use 'catch { emitAll(fallback) }'")));
+ (id<SharedFlow>)onErrorResumeNext:(id<SharedFlow>)receiver fallback:(id<SharedFlow>)fallback __attribute__((swift_name("onErrorResumeNext(_:fallback:)"))) __attribute__((unavailable("Flow analogue of 'onErrorXxx' is 'catch'. Use 'catch { emitAll(fallback) }'")));
+ (id<SharedFlow>)onErrorReturn:(id<SharedFlow>)receiver fallback:(id _Nullable)fallback __attribute__((swift_name("onErrorReturn(_:fallback:)"))) __attribute__((unavailable("Flow analogue of 'onErrorXxx' is 'catch'. Use 'catch { emit(fallback) }'")));
+ (id<SharedFlow>)onErrorReturn:(id<SharedFlow>)receiver fallback:(id _Nullable)fallback predicate:(SharedBoolean *(^)(SharedKotlinThrowable *))predicate __attribute__((swift_name("onErrorReturn(_:fallback:predicate:)"))) __attribute__((unavailable("Flow analogue of 'onErrorXxx' is 'catch'. Use 'catch { e -> if (predicate(e)) emit(fallback) else throw e }'")));
+ (id<SharedFlow>)publish:(id<SharedFlow>)receiver __attribute__((swift_name("publish(_:)"))) __attribute__((unavailable("Flow analogue of 'publish()' is 'shareIn'. \npublish().connect() is the default strategy (no extra call is needed), \npublish().autoConnect() translates to 'started = SharingStarted.Lazily' argument, \npublish().refCount() translates to 'started = SharingStarted.WhileSubscribed()' argument.")));
+ (id<SharedFlow>)publish:(id<SharedFlow>)receiver bufferSize:(int32_t)bufferSize __attribute__((swift_name("publish(_:bufferSize:)"))) __attribute__((unavailable("Flow analogue of 'publish(bufferSize)' is 'buffer' followed by 'shareIn'. \npublish().connect() is the default strategy (no extra call is needed), \npublish().autoConnect() translates to 'started = SharingStarted.Lazily' argument, \npublish().refCount() translates to 'started = SharingStarted.WhileSubscribed()' argument.")));
+ (id<SharedFlow>)publishOn:(id<SharedFlow>)receiver context:(id<SharedKotlinCoroutineContext>)context __attribute__((swift_name("publishOn(_:context:)"))) __attribute__((unavailable("Collect flow in the desired context instead")));
+ (id<SharedFlow>)replay:(id<SharedFlow>)receiver __attribute__((swift_name("replay(_:)"))) __attribute__((unavailable("Flow analogue of 'replay()' is 'shareIn' with unlimited replay. \nreplay().connect() is the default strategy (no extra call is needed), \nreplay().autoConnect() translates to 'started = SharingStarted.Lazily' argument, \nreplay().refCount() translates to 'started = SharingStarted.WhileSubscribed()' argument.")));
+ (id<SharedFlow>)replay:(id<SharedFlow>)receiver bufferSize:(int32_t)bufferSize __attribute__((swift_name("replay(_:bufferSize:)"))) __attribute__((unavailable("Flow analogue of 'replay(bufferSize)' is 'shareIn' with the specified replay parameter. \nreplay().connect() is the default strategy (no extra call is needed), \nreplay().autoConnect() translates to 'started = SharingStarted.Lazily' argument, \nreplay().refCount() translates to 'started = SharingStarted.WhileSubscribed()' argument.")));
+ (id<SharedFlow>)scanFold:(id<SharedFlow>)receiver initial:(id _Nullable)initial operation:(id<SharedKotlinSuspendFunction2>)operation __attribute__((swift_name("scanFold(_:initial:operation:)"))) __attribute__((unavailable("Flow has less verbose 'scan' shortcut")));
+ (id<SharedFlow>)scanReduce:(id<SharedFlow>)receiver operation:(id<SharedKotlinSuspendFunction2>)operation __attribute__((swift_name("scanReduce(_:operation:)"))) __attribute__((unavailable("'scanReduce' was renamed to 'runningReduce' to be consistent with Kotlin standard library")));
+ (id<SharedFlow>)skip:(id<SharedFlow>)receiver count:(int32_t)count __attribute__((swift_name("skip(_:count:)"))) __attribute__((unavailable("Flow analogue of 'skip' is 'drop'")));
+ (id<SharedFlow>)startWith:(id<SharedFlow>)receiver value:(id _Nullable)value __attribute__((swift_name("startWith(_:value:)"))) __attribute__((unavailable("Flow analogue of 'startWith' is 'onStart'. Use 'onStart { emit(value) }'")));
+ (id<SharedFlow>)startWith:(id<SharedFlow>)receiver other:(id<SharedFlow>)other __attribute__((swift_name("startWith(_:other:)"))) __attribute__((unavailable("Flow analogue of 'startWith' is 'onStart'. Use 'onStart { emitAll(other) }'")));
+ (void)subscribe:(id<SharedFlow>)receiver __attribute__((swift_name("subscribe(_:)"))) __attribute__((unavailable("Use 'launchIn' with 'onEach', 'onCompletion' and 'catch' instead")));
+ (void)subscribe:(id<SharedFlow>)receiver onEach:(id<SharedKotlinSuspendFunction1>)onEach __attribute__((swift_name("subscribe(_:onEach:)"))) __attribute__((unavailable("Use 'launchIn' with 'onEach', 'onCompletion' and 'catch' instead")));
+ (void)subscribe:(id<SharedFlow>)receiver onEach:(id<SharedKotlinSuspendFunction1>)onEach onError:(id<SharedKotlinSuspendFunction1>)onError __attribute__((swift_name("subscribe(_:onEach:onError:)"))) __attribute__((unavailable("Use 'launchIn' with 'onEach', 'onCompletion' and 'catch' instead")));
+ (id<SharedFlow>)subscribeOn:(id<SharedFlow>)receiver context:(id<SharedKotlinCoroutineContext>)context __attribute__((swift_name("subscribeOn(_:context:)"))) __attribute__((unavailable("Use 'flowOn' instead")));
+ (id<SharedFlow>)switchMap:(id<SharedFlow>)receiver transform:(id<SharedKotlinSuspendFunction1>)transform __attribute__((swift_name("switchMap(_:transform:)"))) __attribute__((unavailable("Flow analogues of 'switchMap' are 'transformLatest', 'flatMapLatest' and 'mapLatest'")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MultithreadedDispatchers_commonKt")))
@interface SharedMultithreadedDispatchers_commonKt : SharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
 *   kotlinx.coroutines.DelicateCoroutinesApi
*/
+ (SharedCloseableCoroutineDispatcher *)doNewSingleThreadContextName:(NSString *)name __attribute__((swift_name("doNewSingleThreadContext(name:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MultithreadedDispatchersKt")))
@interface SharedMultithreadedDispatchersKt : SharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.DelicateCoroutinesApi
*/
+ (SharedCloseableCoroutineDispatcher *)doNewFixedThreadPoolContextNThreads:(int32_t)nThreads name:(NSString *)name __attribute__((swift_name("doNewFixedThreadPoolContext(nThreads:name:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MutexKt")))
@interface SharedMutexKt : SharedBase
+ (id<SharedMutex>)MutexLocked:(BOOL)locked __attribute__((swift_name("Mutex(locked:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)withLock:(id<SharedMutex>)receiver owner:(id _Nullable)owner action:(id _Nullable (^)(void))action completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("withLock(_:owner:action:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("OnTimeoutKt")))
@interface SharedOnTimeoutKt : SharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (void)onTimeout:(id<SharedSelectBuilder>)receiver timeMillis:(int64_t)timeMillis block:(id<SharedKotlinSuspendFunction0>)block __attribute__((swift_name("onTimeout(_:timeMillis:block:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (void)onTimeout:(id<SharedSelectBuilder>)receiver timeout:(int64_t)timeout block:(id<SharedKotlinSuspendFunction0>)block __attribute__((swift_name("onTimeout(_:timeout:block:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Platform_iosKt")))
@interface SharedPlatform_iosKt : SharedBase
+ (id<SharedPlatform>)getPlatform __attribute__((swift_name("getPlatform()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ProduceKt")))
@interface SharedProduceKt : SharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)awaitClose:(id<SharedProducerScope>)receiver block:(void (^)(void))block completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("awaitClose(_:block:completionHandler:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (id<SharedReceiveChannel>)produce:(id<SharedCoroutineScope>)receiver context:(id<SharedKotlinCoroutineContext>)context capacity:(int32_t)capacity block:(id<SharedKotlinSuspendFunction1>)block __attribute__((swift_name("produce(_:context:capacity:block:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
+ (id<SharedReceiveChannel>)produce:(id<SharedCoroutineScope>)receiver context:(id<SharedKotlinCoroutineContext>)context capacity:(int32_t)capacity start:(SharedCoroutineStart *)start onCompletion:(void (^ _Nullable)(SharedKotlinThrowable * _Nullable))onCompletion block:(id<SharedKotlinSuspendFunction1>)block __attribute__((swift_name("produce(_:context:capacity:start:onCompletion:block:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReduceKt")))
@interface SharedReduceKt : SharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)first:(id<SharedFlow>)receiver completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("first(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)first:(id<SharedFlow>)receiver predicate:(id<SharedKotlinSuspendFunction1>)predicate completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("first(_:predicate:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)firstOrNull:(id<SharedFlow>)receiver completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("firstOrNull(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)firstOrNull:(id<SharedFlow>)receiver predicate:(id<SharedKotlinSuspendFunction1>)predicate completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("firstOrNull(_:predicate:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)fold:(id<SharedFlow>)receiver initial:(id _Nullable)initial operation:(id<SharedKotlinSuspendFunction2>)operation completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("fold(_:initial:operation:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)last:(id<SharedFlow>)receiver completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("last(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)lastOrNull:(id<SharedFlow>)receiver completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("lastOrNull(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)reduce:(id<SharedFlow>)receiver operation:(id<SharedKotlinSuspendFunction2>)operation completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("reduce(_:operation:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)single:(id<SharedFlow>)receiver completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("single(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)singleOrNull:(id<SharedFlow>)receiver completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("singleOrNull(_:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SelectKt")))
@interface SharedSelectKt : SharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)selectBuilder:(void (^)(id<SharedSelectBuilder>))builder completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("select(builder:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SelectUnbiasedKt")))
@interface SharedSelectUnbiasedKt : SharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)selectUnbiasedBuilder:(void (^)(id<SharedSelectBuilder>))builder completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("selectUnbiased(builder:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SemaphoreKt")))
@interface SharedSemaphoreKt : SharedBase
+ (id<SharedSemaphore>)SemaphorePermits:(int32_t)permits acquiredPermits:(int32_t)acquiredPermits __attribute__((swift_name("Semaphore(permits:acquiredPermits:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)withPermit:(id<SharedSemaphore>)receiver action:(id _Nullable (^)(void))action completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("withPermit(_:action:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ShareKt")))
@interface SharedShareKt : SharedBase
+ (id<SharedSharedFlow>)asSharedFlow:(id<SharedMutableSharedFlow>)receiver __attribute__((swift_name("asSharedFlow(_:)")));
+ (id<SharedStateFlow>)asStateFlow:(id<SharedMutableStateFlow>)receiver __attribute__((swift_name("asStateFlow(_:)")));
+ (id<SharedSharedFlow>)onSubscription:(id<SharedSharedFlow>)receiver action:(id<SharedKotlinSuspendFunction1>)action __attribute__((swift_name("onSubscription(_:action:)")));
+ (id<SharedSharedFlow>)shareIn:(id<SharedFlow>)receiver scope:(id<SharedCoroutineScope>)scope started:(id<SharedSharingStarted>)started replay:(int32_t)replay __attribute__((swift_name("shareIn(_:scope:started:replay:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)stateIn:(id<SharedFlow>)receiver scope:(id<SharedCoroutineScope>)scope completionHandler:(void (^)(id<SharedStateFlow> _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("stateIn(_:scope:completionHandler:)")));
+ (id<SharedStateFlow>)stateIn:(id<SharedFlow>)receiver scope:(id<SharedCoroutineScope>)scope started:(id<SharedSharingStarted>)started initialValue:(id _Nullable)initialValue __attribute__((swift_name("stateIn(_:scope:started:initialValue:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SharedFlowKt")))
@interface SharedSharedFlowKt : SharedBase
+ (id<SharedMutableSharedFlow>)MutableSharedFlowReplay:(int32_t)replay extraBufferCapacity:(int32_t)extraBufferCapacity onBufferOverflow:(SharedBufferOverflow *)onBufferOverflow __attribute__((swift_name("MutableSharedFlow(replay:extraBufferCapacity:onBufferOverflow:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SliderExtensionsKt")))
@interface SharedSliderExtensionsKt : SharedBase
+ (id<SharedDisposableHandle_>)bindSlider:(UISlider *)receiver scope:(id<SharedCoroutineScope>)scope state:(id<SharedMutableStateFlow>)state __attribute__((swift_name("bindSlider(_:scope:state:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("StateFlowKt")))
@interface SharedStateFlowKt : SharedBase
+ (id<SharedDisposableHandle_>)bind:(id<SharedStateFlow>)receiver coroutineScope:(id<SharedCoroutineScope>)coroutineScope block:(void (^)(id _Nullable))block __attribute__((swift_name("bind(_:coroutineScope:block:)")));
+ (id<SharedDisposableHandle_>)bindTwoWay:(id<SharedMutableStateFlow>)receiver coroutineScope:(id<SharedCoroutineScope>)coroutineScope onUiChange:(void (^)(id _Nullable))onUiChange __attribute__((swift_name("bindTwoWay(_:coroutineScope:onUiChange:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("StateFlowKt_")))
@interface SharedStateFlowKt_ : SharedBase
+ (id<SharedMutableStateFlow>)MutableStateFlowValue:(id _Nullable)value __attribute__((swift_name("MutableStateFlow(value:)")));
+ (id _Nullable)getAndUpdate:(id<SharedMutableStateFlow>)receiver function:(id _Nullable (^)(id _Nullable))function __attribute__((swift_name("getAndUpdate(_:function:)")));
+ (void)update:(id<SharedMutableStateFlow>)receiver function:(id _Nullable (^)(id _Nullable))function __attribute__((swift_name("update(_:function:)")));
+ (id _Nullable)updateAndGet:(id<SharedMutableStateFlow>)receiver function:(id _Nullable (^)(id _Nullable))function __attribute__((swift_name("updateAndGet(_:function:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SupervisorKt")))
@interface SharedSupervisorKt : SharedBase
+ (id<SharedCompletableJob>)SupervisorJobParent:(id<SharedJob> _Nullable)parent __attribute__((swift_name("SupervisorJob(parent:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)supervisorScopeBlock:(id<SharedKotlinSuspendFunction1>)block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("supervisorScope(block:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SwitchExtensionsKt")))
@interface SharedSwitchExtensionsKt : SharedBase
+ (id<SharedDisposableHandle_>)bindChecked:(UISwitch *)receiver scope:(id<SharedCoroutineScope>)scope state:(id<SharedMutableStateFlow>)state __attribute__((swift_name("bindChecked(_:scope:state:)")));
+ (NSString * _Nullable)toNSString:(NSString *)receiver __attribute__((swift_name("toNSString(_:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Synchronized_commonKt")))
@interface SharedSynchronized_commonKt : SharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
+ (id _Nullable)synchronizedLock:(SharedAtomicfuSynchronizedObject *)lock block:(id _Nullable (^)(void))block __attribute__((swift_name("synchronized(lock:block:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SynchronizedKt")))
@interface SharedSynchronizedKt : SharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.InternalCoroutinesApi
*/
+ (id _Nullable)synchronizedImplLock:(SharedAtomicfuSynchronizedObject *)lock block:(id _Nullable (^)(void))block __attribute__((swift_name("synchronizedImpl(lock:block:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TextFieldExtensionsKt")))
@interface SharedTextFieldExtensionsKt : SharedBase
+ (id<SharedDisposableHandle_>)bindTwoWayText:(UITextField *)receiver scope:(id<SharedCoroutineScope>)scope state:(id<SharedMutableStateFlow>)state __attribute__((swift_name("bindTwoWayText(_:scope:state:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TimeoutKt")))
@interface SharedTimeoutKt : SharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)withTimeoutTimeMillis:(int64_t)timeMillis block:(id<SharedKotlinSuspendFunction1>)block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("withTimeout(timeMillis:block:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)withTimeoutTimeout:(int64_t)timeout block:(id<SharedKotlinSuspendFunction1>)block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("withTimeout(timeout:block:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)withTimeoutOrNullTimeMillis:(int64_t)timeMillis block:(id<SharedKotlinSuspendFunction1>)block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("withTimeoutOrNull(timeMillis:block:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)withTimeoutOrNullTimeout:(int64_t)timeout block:(id<SharedKotlinSuspendFunction1>)block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("withTimeoutOrNull(timeout:block:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TransformKt")))
@interface SharedTransformKt : SharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (id<SharedFlow>)chunked:(id<SharedFlow>)receiver size:(int32_t)size __attribute__((swift_name("chunked(_:size:)")));
+ (id<SharedFlow>)filter:(id<SharedFlow>)receiver predicate:(id<SharedKotlinSuspendFunction1>)predicate __attribute__((swift_name("filter(_:predicate:)")));
+ (id<SharedFlow>)filterIsInstance:(id<SharedFlow>)receiver __attribute__((swift_name("filterIsInstance(_:)")));
+ (id<SharedFlow>)filterIsInstance:(id<SharedFlow>)receiver klass:(id<SharedKotlinKClass>)klass __attribute__((swift_name("filterIsInstance(_:klass:)")));
+ (id<SharedFlow>)filterNot:(id<SharedFlow>)receiver predicate:(id<SharedKotlinSuspendFunction1>)predicate __attribute__((swift_name("filterNot(_:predicate:)")));
+ (id<SharedFlow>)filterNotNull:(id<SharedFlow>)receiver __attribute__((swift_name("filterNotNull(_:)")));
+ (id<SharedFlow>)map:(id<SharedFlow>)receiver transform:(id<SharedKotlinSuspendFunction1>)transform __attribute__((swift_name("map(_:transform:)")));
+ (id<SharedFlow>)mapNotNull:(id<SharedFlow>)receiver transform:(id<SharedKotlinSuspendFunction1>)transform __attribute__((swift_name("mapNotNull(_:transform:)")));
+ (id<SharedFlow>)onEach:(id<SharedFlow>)receiver action:(id<SharedKotlinSuspendFunction1>)action __attribute__((swift_name("onEach(_:action:)")));
+ (id<SharedFlow>)runningFold:(id<SharedFlow>)receiver initial:(id _Nullable)initial operation:(id<SharedKotlinSuspendFunction2>)operation __attribute__((swift_name("runningFold(_:initial:operation:)")));
+ (id<SharedFlow>)runningReduce:(id<SharedFlow>)receiver operation:(id<SharedKotlinSuspendFunction2>)operation __attribute__((swift_name("runningReduce(_:operation:)")));
+ (id<SharedFlow>)scan:(id<SharedFlow>)receiver initial:(id _Nullable)initial operation:(id<SharedKotlinSuspendFunction2>)operation __attribute__((swift_name("scan(_:initial:operation:)")));
+ (id<SharedFlow>)withIndex:(id<SharedFlow>)receiver __attribute__((swift_name("withIndex(_:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ViewExtensionsKt")))
@interface SharedViewExtensionsKt : SharedBase
+ (id<SharedDisposableHandle_>)bindGone:(UIView *)receiver scope:(id<SharedCoroutineScope>)scope flow:(id<SharedStateFlow>)flow __attribute__((swift_name("bindGone(_:scope:flow:)")));
+ (id<SharedDisposableHandle_>)bindVisible:(UIView *)receiver scope:(id<SharedCoroutineScope>)scope flow:(id<SharedStateFlow>)flow __attribute__((swift_name("bindVisible(_:scope:flow:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("WhileSelectKt")))
@interface SharedWhileSelectKt : SharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)whileSelectBuilder:(void (^)(id<SharedSelectBuilder>))builder completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("whileSelect(builder:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("YieldKt")))
@interface SharedYieldKt : SharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)yieldWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("yield(completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ZipKt")))
@interface SharedZipKt : SharedBase
+ (id<SharedFlow>)combineFlows:(SharedKotlinArray<id<SharedFlow>> *)flows transform:(id<SharedKotlinSuspendFunction1>)transform __attribute__((swift_name("combine(flows:transform:)")));
+ (id<SharedFlow>)combineFlows:(id)flows transform_:(id<SharedKotlinSuspendFunction1>)transform __attribute__((swift_name("combine(flows:transform_:)")));

/**
 * @note annotations
 *   kotlin.jvm.JvmName(name="flowCombine")
*/
+ (id<SharedFlow>)combine:(id<SharedFlow>)receiver flow:(id<SharedFlow>)flow transform:(id<SharedKotlinSuspendFunction2>)transform __attribute__((swift_name("combine(_:flow:transform:)")));
+ (id<SharedFlow>)combineFlow:(id<SharedFlow>)flow flow2:(id<SharedFlow>)flow2 transform:(id<SharedKotlinSuspendFunction2>)transform __attribute__((swift_name("combine(flow:flow2:transform:)")));
+ (id<SharedFlow>)combineFlow:(id<SharedFlow>)flow flow2:(id<SharedFlow>)flow2 flow3:(id<SharedFlow>)flow3 transform:(id<SharedKotlinSuspendFunction3>)transform __attribute__((swift_name("combine(flow:flow2:flow3:transform:)")));
+ (id<SharedFlow>)combineFlow:(id<SharedFlow>)flow flow2:(id<SharedFlow>)flow2 flow3:(id<SharedFlow>)flow3 flow4:(id<SharedFlow>)flow4 transform:(id<SharedKotlinSuspendFunction4>)transform __attribute__((swift_name("combine(flow:flow2:flow3:flow4:transform:)")));
+ (id<SharedFlow>)combineFlow:(id<SharedFlow>)flow flow2:(id<SharedFlow>)flow2 flow3:(id<SharedFlow>)flow3 flow4:(id<SharedFlow>)flow4 flow5:(id<SharedFlow>)flow5 transform:(id<SharedKotlinSuspendFunction5>)transform __attribute__((swift_name("combine(flow:flow2:flow3:flow4:flow5:transform:)")));
+ (id<SharedFlow>)combineTransformFlows:(SharedKotlinArray<id<SharedFlow>> *)flows transform:(id<SharedKotlinSuspendFunction2>)transform __attribute__((swift_name("combineTransform(flows:transform:)")));
+ (id<SharedFlow>)combineTransformFlows:(id)flows transform_:(id<SharedKotlinSuspendFunction2>)transform __attribute__((swift_name("combineTransform(flows:transform_:)")));

/**
 * @note annotations
 *   kotlin.jvm.JvmName(name="flowCombineTransform")
*/
+ (id<SharedFlow>)combineTransform:(id<SharedFlow>)receiver flow:(id<SharedFlow>)flow transform:(id<SharedKotlinSuspendFunction3>)transform __attribute__((swift_name("combineTransform(_:flow:transform:)")));
+ (id<SharedFlow>)combineTransformFlow:(id<SharedFlow>)flow flow2:(id<SharedFlow>)flow2 transform:(id<SharedKotlinSuspendFunction3>)transform __attribute__((swift_name("combineTransform(flow:flow2:transform:)")));
+ (id<SharedFlow>)combineTransformFlow:(id<SharedFlow>)flow flow2:(id<SharedFlow>)flow2 flow3:(id<SharedFlow>)flow3 transform:(id<SharedKotlinSuspendFunction4>)transform __attribute__((swift_name("combineTransform(flow:flow2:flow3:transform:)")));
+ (id<SharedFlow>)combineTransformFlow:(id<SharedFlow>)flow flow2:(id<SharedFlow>)flow2 flow3:(id<SharedFlow>)flow3 flow4:(id<SharedFlow>)flow4 transform:(id<SharedKotlinSuspendFunction5>)transform __attribute__((swift_name("combineTransform(flow:flow2:flow3:flow4:transform:)")));
+ (id<SharedFlow>)combineTransformFlow:(id<SharedFlow>)flow flow2:(id<SharedFlow>)flow2 flow3:(id<SharedFlow>)flow3 flow4:(id<SharedFlow>)flow4 flow5:(id<SharedFlow>)flow5 transform:(id<SharedKotlinSuspendFunction6>)transform __attribute__((swift_name("combineTransform(flow:flow2:flow3:flow4:flow5:transform:)")));
+ (id<SharedFlow>)zip:(id<SharedFlow>)receiver other:(id<SharedFlow>)other transform:(id<SharedKotlinSuspendFunction2>)transform __attribute__((swift_name("zip(_:other:transform:)")));
@end

__attribute__((swift_name("KotlinKDeclarationContainer")))
@protocol SharedKotlinKDeclarationContainer
@required
@end

__attribute__((swift_name("KotlinKAnnotatedElement")))
@protocol SharedKotlinKAnnotatedElement
@required
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.1")
*/
__attribute__((swift_name("KotlinKClassifier")))
@protocol SharedKotlinKClassifier
@required
@end

__attribute__((swift_name("KotlinKClass")))
@protocol SharedKotlinKClass <SharedKotlinKDeclarationContainer, SharedKotlinKAnnotatedElement, SharedKotlinKClassifier>
@required

/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.1")
*/
- (BOOL)isInstanceValue:(id _Nullable)value __attribute__((swift_name("isInstance(value:)")));
@property (readonly) NSString * _Nullable qualifiedName __attribute__((swift_name("qualifiedName")));
@property (readonly) NSString * _Nullable simpleName __attribute__((swift_name("simpleName")));
@end

__attribute__((swift_name("KotlinFunction")))
@protocol SharedKotlinFunction
@required
@end

__attribute__((swift_name("KotlinSuspendFunction1")))
@protocol SharedKotlinSuspendFunction1 <SharedKotlinFunction>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeP1:(id _Nullable)p1 completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(p1:completionHandler:)")));
@end

__attribute__((swift_name("KotlinLazy")))
@protocol SharedKotlinLazy
@required
- (BOOL)isInitialized __attribute__((swift_name("isInitialized()")));
@property (readonly) id _Nullable value __attribute__((swift_name("value")));
@end

__attribute__((swift_name("KotlinSequence")))
@protocol SharedKotlinSequence
@required
- (id<SharedKotlinIterator>)iterator __attribute__((swift_name("iterator()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinEnumCompanion")))
@interface SharedKotlinEnumCompanion : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedKotlinEnumCompanion *shared __attribute__((swift_name("shared")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AtomicfuSynchronizedObject.LockState")))
@interface SharedAtomicfuSynchronizedObjectLockState : SharedBase
- (instancetype)initWithStatus:(SharedAtomicfuSynchronizedObjectStatus *)status nestedLocks:(int32_t)nestedLocks waiters:(int32_t)waiters ownerThreadId:(void * _Nullable)ownerThreadId mutex:(SharedAtomicfuNativeMutexNode * _Nullable)mutex __attribute__((swift_name("init(status:nestedLocks:waiters:ownerThreadId:mutex:)"))) __attribute__((objc_designated_initializer));
@property (readonly) SharedAtomicfuNativeMutexNode * _Nullable mutex __attribute__((swift_name("mutex")));
@property (readonly) int32_t nestedLocks __attribute__((swift_name("nestedLocks")));
@property (readonly) void * _Nullable ownerThreadId __attribute__((swift_name("ownerThreadId")));
@property (readonly) SharedAtomicfuSynchronizedObjectStatus *status __attribute__((swift_name("status")));
@property (readonly) int32_t waiters __attribute__((swift_name("waiters")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.9")
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinAtomicReference")))
@interface SharedKotlinAtomicReference<T> : SharedBase
- (instancetype)initWithValue:(T _Nullable)value __attribute__((swift_name("init(value:)"))) __attribute__((objc_designated_initializer));
- (T _Nullable)compareAndExchangeExpected:(T _Nullable)expected newValue:(T _Nullable)newValue __attribute__((swift_name("compareAndExchange(expected:newValue:)")));
- (BOOL)compareAndSetExpected:(T _Nullable)expected newValue:(T _Nullable)newValue __attribute__((swift_name("compareAndSet(expected:newValue:)")));
- (T _Nullable)getAndSetNewValue:(T _Nullable)newValue __attribute__((swift_name("getAndSet(newValue:)")));
- (NSString *)description __attribute__((swift_name("description()")));
@property T _Nullable value __attribute__((swift_name("value")));
@end

__attribute__((swift_name("KotlinSuspendFunction0")))
@protocol SharedKotlinSuspendFunction0 <SharedKotlinFunction>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeWithCompletionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinUnit")))
@interface SharedKotlinUnit : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)unit __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedKotlinUnit *shared __attribute__((swift_name("shared")));
- (NSString *)description __attribute__((swift_name("description()")));
@end

__attribute__((swift_name("KotlinIterator")))
@protocol SharedKotlinIterator
@required
- (BOOL)hasNext __attribute__((swift_name("hasNext()")));
- (id _Nullable)next __attribute__((swift_name("next()")));
@end

__attribute__((swift_name("KotlinIntIterator")))
@interface SharedKotlinIntIterator : SharedBase <SharedKotlinIterator>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (SharedInt *)next __attribute__((swift_name("next()")));
- (int32_t)nextInt __attribute__((swift_name("nextInt()")));
@end

__attribute__((swift_name("KotlinLongIterator")))
@interface SharedKotlinLongIterator : SharedBase <SharedKotlinIterator>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (SharedLong *)next __attribute__((swift_name("next()")));
- (int64_t)nextLong __attribute__((swift_name("nextLong()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinIntProgression.Companion")))
@interface SharedKotlinIntProgressionCompanion : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedKotlinIntProgressionCompanion *shared __attribute__((swift_name("shared")));
- (SharedKotlinIntProgression *)fromClosedRangeRangeStart:(int32_t)rangeStart rangeEnd:(int32_t)rangeEnd step:(int32_t)step __attribute__((swift_name("fromClosedRange(rangeStart:rangeEnd:step:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinIntRange.Companion")))
@interface SharedKotlinIntRangeCompanion : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedKotlinIntRangeCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) SharedKotlinIntRange *EMPTY __attribute__((swift_name("EMPTY")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinLongProgression.Companion")))
@interface SharedKotlinLongProgressionCompanion : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedKotlinLongProgressionCompanion *shared __attribute__((swift_name("shared")));
- (SharedKotlinLongProgression *)fromClosedRangeRangeStart:(int64_t)rangeStart rangeEnd:(int64_t)rangeEnd step:(int64_t)step __attribute__((swift_name("fromClosedRange(rangeStart:rangeEnd:step:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinLongRange.Companion")))
@interface SharedKotlinLongRangeCompanion : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedKotlinLongRangeCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) SharedKotlinLongRange *EMPTY __attribute__((swift_name("EMPTY")));
@end

__attribute__((swift_name("KotlinSuspendFunction2")))
@protocol SharedKotlinSuspendFunction2 <SharedKotlinFunction>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeP1:(id _Nullable)p1 p2:(id _Nullable)p2 completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(p1:p2:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinNothing")))
@interface SharedKotlinNothing : SharedBase
@end

__attribute__((swift_name("KotlinSuspendFunction3")))
@protocol SharedKotlinSuspendFunction3 <SharedKotlinFunction>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeP1:(id _Nullable)p1 p2:(id _Nullable)p2 p3:(id _Nullable)p3 completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(p1:p2:p3:completionHandler:)")));
@end

__attribute__((swift_name("KotlinSuspendFunction4")))
@protocol SharedKotlinSuspendFunction4 <SharedKotlinFunction>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeP1:(id _Nullable)p1 p2:(id _Nullable)p2 p3:(id _Nullable)p3 p4:(id _Nullable)p4 completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(p1:p2:p3:p4:completionHandler:)")));
@end

__attribute__((swift_name("KotlinSuspendFunction5")))
@protocol SharedKotlinSuspendFunction5 <SharedKotlinFunction>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeP1:(id _Nullable)p1 p2:(id _Nullable)p2 p3:(id _Nullable)p3 p4:(id _Nullable)p4 p5:(id _Nullable)p5 completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(p1:p2:p3:p4:p5:completionHandler:)")));
@end

__attribute__((swift_name("KotlinSuspendFunction6")))
@protocol SharedKotlinSuspendFunction6 <SharedKotlinFunction>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeP1:(id _Nullable)p1 p2:(id _Nullable)p2 p3:(id _Nullable)p3 p4:(id _Nullable)p4 p5:(id _Nullable)p5 p6:(id _Nullable)p6 completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(p1:p2:p3:p4:p5:p6:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AtomicfuSynchronizedObject.Status")))
@interface SharedAtomicfuSynchronizedObjectStatus : SharedKotlinEnum<SharedAtomicfuSynchronizedObjectStatus *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) SharedAtomicfuSynchronizedObjectStatus *unlocked __attribute__((swift_name("unlocked")));
@property (class, readonly) SharedAtomicfuSynchronizedObjectStatus *thin __attribute__((swift_name("thin")));
@property (class, readonly) SharedAtomicfuSynchronizedObjectStatus *fat __attribute__((swift_name("fat")));
+ (SharedKotlinArray<SharedAtomicfuSynchronizedObjectStatus *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<SharedAtomicfuSynchronizedObjectStatus *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AtomicfuNativeMutexNode")))
@interface SharedAtomicfuNativeMutexNode : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)lock __attribute__((swift_name("lock()")));
- (void)unlock __attribute__((swift_name("unlock()")));
@end

#pragma pop_macro("_Nullable_result")
#pragma clang diagnostic pop
NS_ASSUME_NONNULL_END
