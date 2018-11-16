package com.cognifide.gradle.aem.base

import com.cognifide.gradle.aem.api.AemPlugin
import com.cognifide.gradle.aem.base.tasks.Debug
import com.cognifide.gradle.aem.base.tasks.*
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.language.base.plugins.LifecycleBasePlugin

/**
 * Provides configuration used by both package and instance plugins.
 */
class BasePlugin : AemPlugin() {

    override fun Project.configure() {
        setupGreet()
        setupDependentPlugins()
        setupExtensions()
        setupTasks()
    }

    private fun Project.setupGreet() {
        AemPlugin.once { logger.info("Using: ${AemPlugin.NAME_WITH_VERSION}") }
    }

    private fun Project.setupDependentPlugins() {
        plugins.apply(BasePlugin::class.java)
    }

    private fun Project.setupExtensions() {
        extensions.create(BaseExtension.NAME, BaseExtension::class.java, this)
    }

    private fun Project.setupTasks() {
        registerTask(Debug.NAME, Debug::class.java) {
            it.dependsOn(LifecycleBasePlugin.BUILD_TASK_NAME)
        }
        registerTask(Rcp.NAME, Rcp::class.java)
        registerTask(Clean.NAME, Clean::class.java) {
            it.mustRunAfter(LifecycleBasePlugin.CLEAN_TASK_NAME, Checkout.NAME)
        }
        registerTask(Vlt.NAME, Vlt::class.java) {
            it.mustRunAfter(LifecycleBasePlugin.CLEAN_TASK_NAME)
        }
        registerTask(Checkout.NAME, Checkout::class.java) {
            it.mustRunAfter(LifecycleBasePlugin.CLEAN_TASK_NAME)
        }
        registerTask(Sync.NAME, Sync::class.java) {
            it.mustRunAfter(LifecycleBasePlugin.CLEAN_TASK_NAME)
            it.dependsOn(Clean.NAME, Checkout.NAME)
        }
    }

    companion object {
        const val PKG = "com.cognifide.gradle.aem"

        const val ID = "com.cognifide.aem.base"
    }

}