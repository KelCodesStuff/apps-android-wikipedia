package org.wikipedia.compose.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wikipedia.R
import org.wikipedia.compose.theme.WikipediaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WikiTopAppBar(
    title: String,
    onNavigationClick: (() -> Unit),
    titleStyle: TextStyle = WikipediaTheme.typography.h1.copy(lineHeight = 24.sp),
    elevation: Dp = 0.dp,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val navigationIcon = Icons.AutoMirrored.Filled.ArrowBack
    val backgroundColor = WikipediaTheme.colors.paperColor

    TopAppBar(
        title = {
            Text(
                text = title,
                style = titleStyle,
                color = WikipediaTheme.colors.primaryColor
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    imageVector = navigationIcon,
                    tint = WikipediaTheme.colors.primaryColor,
                    contentDescription = stringResource(R.string.search_back_button_content_description)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = WikipediaTheme.colors.primaryColor
        ),
        actions = actions,
        modifier = modifier.shadow(elevation = elevation)
    )
}
