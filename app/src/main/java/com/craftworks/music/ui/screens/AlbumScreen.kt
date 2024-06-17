package com.craftworks.music.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.session.MediaController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.craftworks.music.R
import com.craftworks.music.data.Screen
import com.craftworks.music.data.albumList
import com.craftworks.music.data.useNavidromeServer
import com.craftworks.music.providers.local.getSongsOnDevice
import com.craftworks.music.providers.navidrome.getNavidromeAlbums
import com.craftworks.music.ui.elements.AlbumGrid
import com.craftworks.music.ui.elements.HorizontalLineWithNavidromeCheck
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@Preview(showBackground = true, showSystemUi = false)
@Composable
fun AlbumScreen(
    navHostController: NavHostController = rememberNavController(),
    mediaController: MediaController? = null
) {

    val leftPadding = if (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE) 0.dp else 80.dp
    val context = LocalContext.current

    var isSearchFieldOpen by remember { mutableStateOf(false) }
    var searchFilter by remember { mutableStateOf("") }

    var sortedAlbumList = albumList.sortedBy { it.name }.toMutableList()

    val state = rememberPullToRefreshState()
    if (state.isRefreshing) {
        LaunchedEffect(true) {
            albumList.clear()
            if (useNavidromeServer.value){
                getNavidromeAlbums()
            }
            else{
                getSongsOnDevice(context)
            }
            delay(1500)
            state.endRefresh()
        }
    }

    Box(modifier = Modifier.nestedScroll(state.nestedScrollConnection)){

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = leftPadding,
                top = WindowInsets.statusBars
                    .asPaddingValues()
                    .calculateTopPadding())) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.placeholder),
                    contentDescription = "Songs Icon",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(48.dp))
                Text(
                    text = stringResource(R.string.Albums),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { isSearchFieldOpen = !isSearchFieldOpen },
                    modifier = Modifier
                        .size(48.dp)) {
                    Icon(Icons.Rounded.Search, contentDescription = "Search all songs")
                }
            }

            HorizontalLineWithNavidromeCheck()

            AnimatedVisibility(
                visible = isSearchFieldOpen,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                ) {
                    val focusRequester = remember { FocusRequester() }
                    TextField(
                        value = searchFilter,
                        onValueChange = { searchFilter = it },
                        label = { Text(stringResource(R.string.Action_Search)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(0.dp, 0.dp, 12.dp, 12.dp))
                            .focusRequester(focusRequester))

                    LaunchedEffect(isSearchFieldOpen) {
                        if (isSearchFieldOpen)
                            focusRequester.requestFocus()
                        else
                            focusRequester.freeFocus()
                    }
                }
            }

            if (searchFilter.isNotBlank()){
                sortedAlbumList = sortedAlbumList.filter { it.name!!.contains(searchFilter, true)  ||
                        it.artist.contains(searchFilter, true) }.toMutableList()
            }

            AlbumGrid(sortedAlbumList, mediaController, onAlbumSelected = { album ->
                navHostController.navigate(Screen.AlbumDetails.route) {
                    launchSingleTop = true
                }
                selectedAlbum = album})
        }

        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = state,
        )
    }
}