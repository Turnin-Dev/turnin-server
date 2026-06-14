package com.turnin.domain.user.infrastructure.provider

import com.turnin.domain.file.application.provider.FileProviderApi
import com.turnin.domain.file.domain.model.FileCategory
import com.turnin.domain.user.domain.provider.FileProvider

class FileProviderImpl(private val fileProviderApi: FileProviderApi) : FileProvider {
    override fun deleteFile(
        fileName: String,
        fileCategory: FileCategory,
    ) =
        fileProviderApi.deleteFile(fileName, fileCategory)
}
