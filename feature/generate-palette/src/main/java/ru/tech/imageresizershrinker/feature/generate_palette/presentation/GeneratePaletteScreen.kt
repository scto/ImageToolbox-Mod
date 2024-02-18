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

package ru.tech.imageresizershrinker.feature.generate_palette.presentation

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colordetector.ImageColorPalette
import com.t8rin.dynamic.theme.LocalDynamicThemeState
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.pop
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.settings.presentation.LocalSettingsState
import ru.tech.imageresizershrinker.core.ui.icons.material.PaletteSwatch
import ru.tech.imageresizershrinker.core.ui.utils.helper.ContextUtils.copyToClipboard
import ru.tech.imageresizershrinker.core.ui.utils.helper.Picker
import ru.tech.imageresizershrinker.core.ui.utils.helper.localImagePickerMode
import ru.tech.imageresizershrinker.core.ui.utils.helper.rememberImagePicker
import ru.tech.imageresizershrinker.core.ui.utils.helper.toHex
import ru.tech.imageresizershrinker.core.ui.utils.navigation.LocalNavController
import ru.tech.imageresizershrinker.core.ui.utils.navigation.Screen
import ru.tech.imageresizershrinker.core.ui.widget.buttons.EnhancedFloatingActionButton
import ru.tech.imageresizershrinker.core.ui.widget.buttons.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.buttons.ZoomButton
import ru.tech.imageresizershrinker.core.ui.widget.image.ImageNotPickedWidget
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.modifier.drawHorizontalStroke
import ru.tech.imageresizershrinker.core.ui.widget.modifier.navBarsPaddingOnlyIfTheyAtTheBottom
import ru.tech.imageresizershrinker.core.ui.widget.modifier.transparencyChecker
import ru.tech.imageresizershrinker.core.ui.widget.other.LoadingDialog
import ru.tech.imageresizershrinker.core.ui.widget.other.LocalToastHostState
import ru.tech.imageresizershrinker.core.ui.widget.other.TopAppBarEmoji
import ru.tech.imageresizershrinker.core.ui.widget.other.showError
import ru.tech.imageresizershrinker.core.ui.widget.sheets.ZoomModalSheet
import ru.tech.imageresizershrinker.core.ui.widget.text.Marquee
import ru.tech.imageresizershrinker.core.ui.widget.utils.LocalWindowSizeClass
import ru.tech.imageresizershrinker.core.ui.widget.utils.isScrollingUp
import ru.tech.imageresizershrinker.feature.generate_palette.presentation.components.PaletteColorsCountSelector
import ru.tech.imageresizershrinker.feature.generate_palette.presentation.viewModel.GeneratePaletteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratePaletteScreen(
    uriState: Uri?,
    onGoBack: () -> Unit,
    viewModel: GeneratePaletteViewModel = hiltViewModel()
) {
    val settingsState = LocalSettingsState.current
    val context = LocalContext.current
    val toastHostState = LocalToastHostState.current
    val themeState = LocalDynamicThemeState.current
    val allowChangeColor = settingsState.allowChangeColorByImage
    val navController = LocalNavController.current

    val scope = rememberCoroutineScope()

    var color by rememberSaveable(
        saver = Saver(
            save = { it.value.toArgb() },
            restore = { mutableStateOf(Color(it)) }
        )
    ) { mutableStateOf(Color.Unspecified) }

    LaunchedEffect(uriState) {
        uriState?.let {
            color = Color.Unspecified
            viewModel.setUri(it)
            viewModel.decodeBitmapByUri(
                uri = it,
                originalSize = false,
                onGetMimeType = {},
                onGetExif = {},
                onGetBitmap = viewModel::updateBitmap,
                onError = {
                    scope.launch {
                        toastHostState.showError(context, it)
                    }
                }
            )
        }
    }

    LaunchedEffect(viewModel.bitmap, color) {
        viewModel.bitmap?.let {
            if (allowChangeColor) {
                if (color == Color.Unspecified) {
                    themeState.updateColorByImage(it)
                } else {
                    themeState.updateColor(color)
                }
            }
        }
    }

    val pickImageLauncher =
        rememberImagePicker(
            mode = localImagePickerMode(Picker.Single)
        ) { uris ->
            uris.takeIf { it.isNotEmpty() }?.firstOrNull()?.let {
                color = Color.Unspecified
                viewModel.setUri(it)
                viewModel.decodeBitmapByUri(
                    uri = it,
                    originalSize = false,
                    onGetMimeType = {},
                    onGetExif = {},
                    onGetBitmap = viewModel::updateBitmap,
                    onError = {
                        scope.launch {
                            toastHostState.showError(context, it)
                        }
                    }
                )
            }
        }

    val pickImage = {
        pickImageLauncher.pickImage()
    }
    val scrollState = rememberScrollState()

    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val noPalette: @Composable ColumnScope.() -> Unit = {
        Column(
            Modifier.container(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            FilledIconButton(
                enabled = false,
                onClick = {},
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(16.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface,
                )
            ) {
                Icon(
                    Icons.Rounded.PaletteSwatch,
                    null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                stringResource(R.string.no_palette),
                Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    val landscape =
        LocalWindowSizeClass.current.widthSizeClass != WindowWidthSizeClass.Compact || LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val showZoomSheet = rememberSaveable { mutableStateOf(false) }

    ZoomModalSheet(
        data = viewModel.bitmap,
        visible = showZoomSheet
    )

    var count by rememberSaveable { mutableIntStateOf(32) }

    Box(
        Modifier
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LargeTopAppBar(
                scrollBehavior = topAppBarScrollBehavior,
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),
                modifier = Modifier.drawHorizontalStroke(),
                title = {
                    Marquee(
                        edgeColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    ) {
                        if (viewModel.bitmap == null) Text(stringResource(R.string.generate_palette))
                        else Text(stringResource(R.string.palette))
                    }
                },
                navigationIcon = {
                    EnhancedIconButton(
                        containerColor = Color.Transparent,
                        contentColor = LocalContentColor.current,
                        enableAutoShadowAndBorder = false,
                        onClick = {
                            onGoBack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                    }
                },
                actions = {
                    if (viewModel.uri == null) {
                        TopAppBarEmoji()
                    }
                    ZoomButton(
                        onClick = { showZoomSheet.value = true },
                        visible = viewModel.bitmap != null,
                    )
                    if (viewModel.uri != null) {
                        EnhancedIconButton(
                            containerColor = Color.Transparent,
                            contentColor = LocalContentColor.current,
                            enableAutoShadowAndBorder = false,
                            onClick = {
                                if (navController.backstack.entries.isNotEmpty()) navController.pop()
                                navController.navigate(Screen.PickColorFromImage(viewModel.uri))
                            }
                        ) {
                            Icon(Icons.Rounded.Colorize, null)
                        }
                    }
                }
            )

            AnimatedContent(targetState = viewModel.bitmap) { bitmap ->
                bitmap?.let { b ->
                    val bmp = remember(b) { b.asImageBitmap() }

                    if (landscape) {
                        val direction = LocalLayoutDirection.current
                        Row {
                            Image(
                                bitmap = bmp,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(16.dp)
                                    .navBarsPaddingOnlyIfTheyAtTheBottom()
                                    .padding(
                                        start = WindowInsets
                                            .displayCutout
                                            .asPaddingValues()
                                            .calculateStartPadding(direction)
                                    )
                                    .container()
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .transparencyChecker(),
                                contentDescription = null,
                                contentScale = ContentScale.FillHeight
                            )
                            Column(
                                Modifier
                                    .weight(1f)
                                    .verticalScroll(scrollState)
                                    .padding(
                                        end = WindowInsets.displayCutout
                                            .asPaddingValues()
                                            .calculateEndPadding(direction)
                                    )
                            ) {
                                PaletteColorsCountSelector(
                                    modifier = Modifier.padding(top = 16.dp),
                                    value = count,
                                    onValueChange = { count = it }
                                )
                                ImageColorPalette(
                                    borderWidth = settingsState.borderWidth,
                                    imageBitmap = bmp,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 72.dp)
                                        .padding(16.dp)
                                        .navigationBarsPadding()
                                        .container(RoundedCornerShape(24.dp))
                                        .padding(4.dp),
                                    style = LocalTextStyle.current,
                                    onEmpty = { noPalette() },
                                    maximumColorCount = count,
                                    onColorChange = {
                                        context.copyToClipboard(
                                            context.getString(R.string.color),
                                            it.color.toHex()
                                        )
                                        scope.launch {
                                            color = it.color
                                            toastHostState.showToast(
                                                icon = Icons.Rounded.ContentPaste,
                                                message = context.getString(R.string.color_copied)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        Column(
                            Modifier
                                .verticalScroll(scrollState),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                bitmap = bmp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .container()
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .transparencyChecker(),
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth
                            )
                            PaletteColorsCountSelector(
                                value = count,
                                onValueChange = { count = it }
                            )
                            ImageColorPalette(
                                borderWidth = settingsState.borderWidth,
                                imageBitmap = bmp,
                                modifier = Modifier
                                    .padding(bottom = 72.dp)
                                    .padding(16.dp)
                                    .navigationBarsPadding()
                                    .container(RoundedCornerShape(24.dp))
                                    .padding(4.dp),
                                onEmpty = { noPalette() },
                                style = LocalTextStyle.current,
                                maximumColorCount = count,
                                onColorChange = {
                                    context.copyToClipboard(
                                        context.getString(R.string.color),
                                        it.color.toHex()
                                    )
                                    scope.launch {
                                        color = it.color
                                        toastHostState.showToast(
                                            icon = Icons.Rounded.ContentPaste,
                                            message = context.getString(R.string.color_copied)
                                        )
                                    }
                                }
                            )
                        }
                    }
                } ?: Column(Modifier.verticalScroll(scrollState)) {
                    ImageNotPickedWidget(
                        onPickImage = pickImage,
                        modifier = Modifier
                            .padding(bottom = 88.dp, top = 20.dp, start = 20.dp, end = 20.dp)
                            .navigationBarsPadding()
                    )
                }
            }
        }

        EnhancedFloatingActionButton(
            onClick = pickImage,
            modifier = Modifier
                .navigationBarsPadding()
                .then(
                    if (viewModel.bitmap != null) {
                        Modifier.displayCutoutPadding()
                    } else Modifier
                )
                .padding(12.dp)
                .align(if (!landscape || viewModel.bitmap == null) settingsState.fabAlignment else Alignment.BottomEnd)
        ) {
            val expanded =
                scrollState.isScrollingUp(settingsState.fabAlignment != Alignment.BottomCenter || landscape)
            val horizontalPadding by animateDpAsState(targetValue = if (expanded) 16.dp else 0.dp)
            Row(
                modifier = Modifier.padding(horizontal = horizontalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Rounded.AddPhotoAlternate, contentDescription = null)
                AnimatedVisibility(visible = expanded) {
                    Row {
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.pick_image_alt))
                    }
                }
            }
        }
    }

    if (viewModel.isImageLoading) LoadingDialog(false) {}

    BackHandler {
        onGoBack()
    }
}