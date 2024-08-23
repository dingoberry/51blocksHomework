package com.block57.homework

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apollographql.apollo.api.Optional
import com.block57.homework.generate.PeopleQuery
import com.block57.homework.generate.PeopleQuery.Person
import com.block57.homework.generate.PersonQuery
import com.block57.homework.net.apolloSwapiClient
import com.block57.homework.ui.theme.GraphQLHwDemoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val KEY_PERSON_ID = "personId"
private const val PAGE_LIST = "page.list"
private const val PAGE_DETAIL = "page.detail"
private const val TRANSITION_ANIM_DURATION = 500


class MainActivity : ComponentActivity() {

    private var mCanPageChanged = true

    override fun onBackPressed() {
        if (mCanPageChanged) {
            super.onBackPressed()
        }
    }

    private fun antiShakeAnim() {
        lifecycle.coroutineScope.launch(Dispatchers.Main) {
            mCanPageChanged = false
            delay(TRANSITION_ANIM_DURATION.toLong())
            mCanPageChanged = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GraphQLHwDemoTheme {
                val navControl = rememberNavController().apply {
                    addOnDestinationChangedListener { _, _, _ ->
                        antiShakeAnim()
                    }
                }

                val list = remember {
                    mutableStateListOf<Person>()
                }

                var errMessage by remember {
                    mutableStateOf<String?>(null)
                }

                var showLoading by remember {
                    mutableStateOf(true)
                }

                if (showLoading) {
                    Loading()
                }

                var current by remember {
                    mutableLongStateOf(SystemClock.elapsedRealtime())
                }

                LaunchedEffect(current) {
                    list.addAll(withContext(Dispatchers.IO) {
                        mutableListOf<Person>().apply {
                            runCatching {
                                apolloSwapiClient.query(
                                    PeopleQuery()
                                ).execute().also {
                                    var e: String? = null
                                    if (it.hasErrors()) {
                                        e = it.errors.toString()
                                    }

                                    it.exception?.run {
                                        e = if (null == e) this.toString() else "$e\n${this}"
                                    }

                                    e?.run {
                                        this@LaunchedEffect.launch(Dispatchers.Main) {
                                            errMessage = this@run
                                        }
                                    }
                                }.data?.allPeople?.people?.forEach {
                                    it?.run {
                                        this@apply.add(this)
                                    }
                                }
                            }.onFailure {
                                errMessage = it.toString()
                            }
                        }
                    })
                    showLoading = false
                }

                NavHost(navControl, startDestination = PAGE_LIST) {
                    (slideIn(tween(durationMillis = TRANSITION_ANIM_DURATION)) {
                        IntOffset(
                            -it.width,
                            0
                        )
                    } to slideOut(tween(durationMillis = TRANSITION_ANIM_DURATION)) {
                        IntOffset(
                            -it.width,
                            0
                        )
                    }).apply {
                        composable(PAGE_LIST,
                            popEnterTransition = {
                                first
                            },
                            enterTransition = {
                                first
                            },
                            exitTransition = {
                                second
                            },
                            popExitTransition = {
                                second
                            }
                        ) {
                            ListBoard(list = list, errMessage = errMessage, retryCallback = {
                                current = SystemClock.elapsedRealtime()
                            }) {
                                if (mCanPageChanged) {
                                    navControl.navigate(
                                        "$PAGE_DETAIL/$it",
                                        navOptions = NavOptions.Builder().setLaunchSingleTop(true)
                                            .build()
                                    )
                                }
                            }
                        }
                    }

                    (slideIn(tween(durationMillis = TRANSITION_ANIM_DURATION)) {
                        IntOffset(
                            it.width,
                            0
                        )
                    } to slideOut(tween(durationMillis = TRANSITION_ANIM_DURATION)) {
                        IntOffset(
                            it.width,
                            0
                        )
                    }).apply {
                        composable("$PAGE_DETAIL/{$KEY_PERSON_ID}",
                            enterTransition = {
                                first
                            },
                            popEnterTransition = {
                                first
                            },
                            popExitTransition = {
                                second
                            }, exitTransition = {
                                second
                            }) {
                            DetailBoard(id = it.arguments?.getString(KEY_PERSON_ID)) {
                                if (mCanPageChanged) {
                                    navControl.popBackStack()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


private fun Modifier.commonBackground(innerPadding: PaddingValues) = this
    .fillMaxSize()
    .padding(innerPadding)
    .background(
        brush = Brush.linearGradient(
            listOf(Color(0xFF1E1266), Color.Black)
        )
    )

@Composable
private fun Loading() {
    Dialog(onDismissRequest = { }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.requiredHeight(10.dp))
            Text(text = "Loading")
        }
    }
}

@Composable
private fun ListBoard(
    modifier: Modifier = Modifier,
    list: List<Person>? = null,
    errMessage: String? = null,
    retryCallback: (() -> Unit)? = null,
    onNavi2Detail: (String) -> Unit
) {
    Column(
        modifier = modifier
            .commonBackground(PaddingValues(0.dp))
            .navigationBarsPadding()
    ) {
        Text(text = "People", fontSize = 48.sp, modifier = Modifier.padding(22.dp))

        VerticalDivider(
            modifier
                .background(Color.LightGray)
                .fillMaxWidth()
                .height(1.dp)
        )

        if (list.isNullOrEmpty() && errMessage != null) {
            Box(
                modifier = Modifier
                    .padding(13.dp)
                    .fillMaxSize()
            ) {
                Text(text = errMessage, fontSize = 16.sp)

                Button(
                    onClick = { retryCallback?.invoke() },
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(text = "Retry Request")
                }
            }
            return
        }

        LazyColumn {
            var first = true
            list?.forEach {
                item {
                    if (first) {
                        first = false
                    } else {
                        VerticalDivider(
                            modifier
                                .background(Color.White)
                                .fillMaxWidth()
                                .height(1.dp)
                        )
                    }
                    Row(modifier = Modifier
                        .heightIn(80.dp)
                        .padding(18.dp)
                        .clickable {
                            onNavi2Detail(it.id)
                        }) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .wrapContentHeight()
                                .align(Alignment.CenterVertically),
                        ) {
                            Text(text = it.name ?: "")
                            Text(text = "Height:${it.height ?: ""}")
                            Text(text = "Mass:${it.mass ?: ""}")
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "",
                            modifier = Modifier.size(33.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}


@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailBoard(
    modifier: Modifier = Modifier,
    id: String? = null,
    onBack2List: () -> Unit
) {
    var person by remember {
        mutableStateOf<PersonQuery.Person?>(null)
    }
    var defaultContent by remember {
        mutableStateOf("")
    }

    id?.run {
        LaunchedEffect(UInt) {
            person = withContext(Dispatchers.IO) {
                runCatching {
                    apolloSwapiClient.query(PersonQuery(Optional.present(this@run)))
                        .execute().data?.person
                }.onFailure {
                    this@LaunchedEffect.launch(Dispatchers.Main) {
                        defaultContent = it.message.toString()
                    }
                }.getOrNull()
            }
            if (person == null && defaultContent.isEmpty()) {
                defaultContent = "No such person data found!"
            }
        }
    } ?: run {
        defaultContent = "Empty invalid Person ID"
    }

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .commonBackground(PaddingValues(0.dp))
    ) {
        var v by remember {
            mutableStateOf(false)
        }

        if (person != null) {
            AnimatedVisibility(
                visible = v,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                enter = slideIn(
                    tween(durationMillis = 800)

                ) { IntOffset(0, it.height) },
                exit = slideOut(
                    tween(durationMillis = 800)

                ) { IntOffset(it.height, 0) }
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
                    color = Color.White
                ) {
                    Box(
                        modifier = Modifier
                            .padding(18.dp)
                            .sizeIn(minHeight = 120.dp)
                    ) {
                        LazyColumn {
                            item {
                                Text(
                                    text = person?.name ?: "",
                                    color = Color.Black,
                                    fontSize = 27.sp
                                )
                            }
                            item {
                                Text(
                                    text = """homeworld : ${person?.homeworld?.name}
id : ${person?.homeworld?.id}
created : ${person?.homeworld?.created}
edited : ${person?.homeworld?.edited}
gravity : ${person?.homeworld?.gravity}
diameter : ${person?.homeworld?.diameter}
orbitalPeriod : ${person?.homeworld?.orbitalPeriod}
population : ${person?.homeworld?.population}
rotationPeriod : ${person?.homeworld?.rotationPeriod}
surfaceWater : ${person?.homeworld?.surfaceWater}
climates : ${person?.homeworld?.climates}
terrains : ${person?.homeworld?.terrains}
                                    """.trimIndent(),
                                    color = Color.Black,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Column {
            TopAppBar(
                title = { Text(text = "People", fontSize = 32.sp) },

                colors = TopAppBarColors(
                    Color.Transparent,
                    Color.Transparent,
                    Color.White,
                    Color.White,
                    Color.Transparent
                ),
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "",
                        modifier = Modifier
                            .graphicsLayer {
                                this.rotationY = 180f
                            }
                            .size(44.dp)
                            .clickable { onBack2List() }
                    )
                }
            )

            if (person == null) {
                Text(text = defaultContent, fontSize = 26.sp)
            } else {
                ClickableText(
                    text = buildAnnotatedString {
                        this.append("Click here to view homeworld data for ${person?.name ?: ""}")
                        this.addStyle(
                            SpanStyle(
                                color = Color.Blue,
                                textDecoration = TextDecoration.Underline
                            ), 6, 10
                        )
                    },
                    modifier = Modifier.padding(40.dp),
                    style = TextStyle(fontSize = 20.sp, color = Color.White)
                ) {
                    if (it in 6..9) {
                        v = true
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 1080, heightDp = 1920)
@Composable
private fun ListBoardPreview() {
    GraphQLHwDemoTheme {
        ListBoard {}
    }
}

@Preview(showBackground = true, widthDp = 1080, heightDp = 1920)
@Composable
private fun DetailBoardPreview() {
    GraphQLHwDemoTheme {
        DetailBoard {}
    }
}