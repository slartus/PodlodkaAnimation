package ru.slartus.podlodkaanimation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@ExperimentalMotionApi
@Composable
fun MainScreen() {
    val screenHeight = LocalConfiguration.current.screenHeightDp.toFloat()
    val swipingState = rememberSwipeableState(initialValue = SwipeStates.Expanded)
    val animateToEnd by remember { mutableStateOf(true) }
    val motionLayoutProgress by animateFloatAsState(
        targetValue = if (swipingState.progress.to == SwipeStates.Expanded) {
            1f - swipingState.progress.fraction
        } else {
            swipingState.progress.fraction
        },
    )
    MotionLayout(
        start = startConstrainSet(),
        end = endConstrainSet(),
        progress = motionLayoutProgress,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFF500089))
            .swipeable(
                state = swipingState,
                thresholds = { _, _ -> FractionalThreshold(0.5f) },
                orientation = Orientation.Vertical,
                anchors = mapOf(
                    0f to SwipeStates.Expanded,
                    screenHeight to SwipeStates.Collapsed
                )
            )
    ) {
        Taxi(motionLayoutProgress)
        Glasses(motionLayoutProgress)
        BottomContent(motionLayoutProgress)
    }
}

@Composable
private fun Taxi(motionLayoutProgress: Float, modifier: Modifier = Modifier) {
    val screenWidth = (LocalConfiguration.current.screenWidthDp + 100).toFloat()

    val offsetAnimation = remember { Animatable(0f, Float.VectorConverter) }
    var startAnimation by remember { mutableStateOf(false) }
    startAnimation = motionLayoutProgress == 1f

    LaunchedEffect(startAnimation) {
        if (startAnimation) {
            launch { offsetAnimation.animateTo(screenWidth, tween(2000)) }
        } else {
            offsetAnimation.snapTo(0f)
        }
    }

    Image(
        modifier = modifier
            .layoutId(ViewIds.Taxi)
            .alpha(alpha = motionLayoutProgress)
            .scale(motionLayoutProgress)
            .offset(x = offsetAnimation.value.dp)
            .size(40.dp),
        painter = painterResource(id = R.drawable.taxi), contentDescription = null
    )
}

@Composable
private fun Glasses(motionLayoutProgress: Float, modifier: Modifier = Modifier) {

    val rotateAnimation = remember { Animatable(0f, Float.VectorConverter) }
    var startAnimation by remember { mutableStateOf(false) }
    startAnimation = motionLayoutProgress == 1f

    LaunchedEffect(startAnimation) {
        if (startAnimation) {
            launch { rotateAnimation.animateTo(360f, tween(2000)) }
        } else {
            rotateAnimation.snapTo(0f)
        }
    }

    Image(
        modifier = modifier
            .layoutId(ViewIds.Glasses)
            .alpha(alpha = motionLayoutProgress)
            .scale(motionLayoutProgress)
            .rotate(rotateAnimation.value)
            .size(40.dp),
        painter = painterResource(id = R.drawable.glasses), contentDescription = null
    )
}

@Composable
private fun BottomContent(motionLayoutProgress: Float) {
    Box(
        modifier = Modifier
            .layoutId(ViewIds.MainBox)
            .background(color = Color(0xFF6A00B9)),
    ) {
        StartContent(motionLayoutProgress)
        CardContent(motionLayoutProgress)
    }
}

@Composable
private fun BoxScope.CardContent(motionLayoutProgress: Float) {
    val transition = rememberInfiniteTransition()
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = InfiniteRepeatableSpec(
            tween(
                durationMillis = 1500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    Image(
        modifier = Modifier
            .padding(top = 20.dp)
            .align(Center)
            .size(100.dp)
            .offset(10.dp * offset, 10.dp * offset)
            .alpha(alpha = motionLayoutProgress),
        painter = painterResource(id = R.drawable.card),
        contentDescription = "Card"
    )
}

@Composable
private fun BoxScope.StartContent(motionLayoutProgress: Float) {
    Column(
        modifier = Modifier.Companion
            .align(Center)
            .alpha(alpha = 1f - motionLayoutProgress)
    ) {
        Text(
            text = "Поехали!?",
            color = Color.White,
            fontSize = 40.sp,
            modifier = Modifier
                .align(CenterHorizontally),
        )
        Image(
            modifier = Modifier
                .padding(top = 20.dp)
                .align(CenterHorizontally)
                .size(40.dp)
                .rotate(-1 * motionLayoutProgress * 100),
            painter = painterResource(id = R.drawable.hand),
            contentDescription = "Hand"
        )
    }
}


private fun startConstrainSet() = ConstraintSet {
    val taxi = createRefFor(ViewIds.Taxi)
    val mainBox = createRefFor(ViewIds.MainBox)
    val glasses = createRefFor(ViewIds.Glasses)

    constrain(mainBox) {
        width = Dimension.fillToConstraints
        height = Dimension.fillToConstraints
        top.linkTo(parent.top)
        bottom.linkTo(parent.bottom)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
    }

    constrain(taxi) {
        bottom.linkTo(mainBox.top, margin = 20.dp)
        start.linkTo(parent.start)
    }

    constrain(glasses) {
        top.linkTo(parent.top)
        bottom.linkTo(mainBox.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
    }
}

private fun endConstrainSet() = ConstraintSet {
    val taxi = createRefFor(ViewIds.Taxi)
    val mainBox = createRefFor(ViewIds.MainBox)
    val glasses = createRefFor(ViewIds.Glasses)

    constrain(mainBox) {
        width = Dimension.fillToConstraints
        height = Dimension.percent(0.5f)
        bottom.linkTo(parent.bottom)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
    }

    constrain(taxi) {
        bottom.linkTo(mainBox.top, margin = 20.dp)
        start.linkTo(parent.start)
    }

    constrain(glasses) {
        top.linkTo(parent.top)
        bottom.linkTo(mainBox.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
    }
}

private enum class SwipeStates {
    Expanded, Collapsed
}

private enum class ViewIds {
    MainBox, Taxi, Glasses
}
