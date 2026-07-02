package com.turnin.domain.user.infrastructure.provider

import com.turnin.domain.file.application.provider.FileProviderApi
import com.turnin.domain.user.domain.provider.FileProvider

class FileProviderImpl(private val fileProviderApi: FileProviderApi) : FileProvider {
    override suspend fun deleteFile(fileUrl: String) =
        fileProviderApi.deleteFile(fileUrl)
}
