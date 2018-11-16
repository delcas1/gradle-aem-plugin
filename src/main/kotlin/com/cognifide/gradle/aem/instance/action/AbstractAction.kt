package com.cognifide.gradle.aem.instance.action

import com.cognifide.gradle.aem.base.BaseExtension
import com.cognifide.gradle.aem.instance.Instance
import com.cognifide.gradle.aem.instance.InstanceAction
import com.cognifide.gradle.aem.instance.toLocalHandles
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

abstract class AbstractAction(
        @Internal
        @Transient
        val project: Project
) : InstanceAction {

    @Internal
    @Transient
    val aem = BaseExtension.of(project)

    @Input
    var instances: List<Instance> = Instance.filter(project)

    @Internal
    var notify = true

    @get:Internal
    val handles = instances.toLocalHandles(project)

    fun notify(title: String, text: String, enabled: Boolean = this.notify) {
        if (enabled) {
            aem.notifier.notify(title, text)
        } else {
            aem.notifier.log(title, text)
        }
    }

}