package de.lukasneugebauer.nextcloudcookbook.tasks.domain.repository

import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.tasks.domain.model.TaskList

interface TasksRepository {
    suspend fun getTaskLists(): Resource<List<TaskList>>

    suspend fun createTasks(
        listUrl: String,
        summaries: List<String>,
    ): Resource<Int>
}
