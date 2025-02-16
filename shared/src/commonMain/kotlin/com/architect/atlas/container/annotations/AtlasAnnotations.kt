package com.architect.atlas.container.annotations

@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Singleton

@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Factory

@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Scoped

@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class ViewModels

@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Module

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Provides
