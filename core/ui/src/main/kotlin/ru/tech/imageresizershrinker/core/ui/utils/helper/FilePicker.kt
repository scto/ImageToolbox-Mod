/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package ru.tech.imageresizershrinker.core.ui.utils.helper

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ru.tech.imageresizershrinker.core.ui.utils.provider.rememberLocalEssentials

private data class FilePickerImpl(
    val context: Context,
    val type: FileType,
    val mimeTypes: List<String>,
    val openDocument: ManagedActivityResultLauncher<Array<String>, Uri?>,
    val openDocumentMultiple: ManagedActivityResultLauncher<Array<String>, List<Uri>>,
    val onFailure: (Throwable) -> Unit
) : FilePicker {

    override fun pickFile() {
        runCatching {
            when (type) {
                FileType.Single -> openDocument.launch(mimeTypes.toTypedArray())
                FileType.Multiple -> openDocumentMultiple.launch(mimeTypes.toTypedArray())
            }
        }.onFailure(onFailure)
    }

}

@Stable
@Immutable
interface FilePicker {
    fun pickFile()
}

enum class FileType {
    Single, Multiple
}

@Composable
fun rememberFilePicker(
    type: FileType,
    mimeTypes: List<String> = DefaultMimeTypes,
    onFailure: () -> Unit = {},
    onSuccess: (List<Uri>) -> Unit,
): FilePicker {
    val context = LocalContext.current

    val openDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.takeIf {
                it != Uri.EMPTY
            }?.let {
                onSuccess(listOf(it))
            } ?: onFailure()
        }
    )
    val openDocumentMultiple = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            uris.takeIf { it.isNotEmpty() }?.let(onSuccess) ?: onFailure()
        }
    )

    val essentials = rememberLocalEssentials()

    return remember(
        type,
        mimeTypes,
        openDocument,
        openDocumentMultiple
    ) {
        derivedStateOf {
            FilePickerImpl(
                context = context,
                type = type,
                mimeTypes = mimeTypes,
                openDocument = openDocument,
                openDocumentMultiple = openDocumentMultiple,
                onFailure = {
                    onFailure()

                    when (it) {
                        is ActivityNotFoundException -> essentials.showActivateFilesToast()
                        else -> essentials.showFailureToast(it)
                    }
                }
            )
        }
    }.value
}

private val DefaultMimeTypes = listOf("*/*")