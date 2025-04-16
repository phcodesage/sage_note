package com.example.sagenote.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.sagenote.data.NoteType

data class FabItem(
    val icon: ImageVector,
    val label: String,
    val noteType: NoteType
)

@Composable
fun MultiFloatingActionButton(
    onFabItemClicked: (NoteType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Animate rotation when expanded with spring animation for more natural feel
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "FAB Rotation"
    )
    
    // Use different color when expanded
    val backgroundColor = if (expanded) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    
    val fabItems = listOf(
        FabItem(Icons.Default.Note, "Text Note", NoteType.TEXT),
        FabItem(Icons.Default.CheckBox, "List Note", NoteType.LIST),
        FabItem(Icons.Default.Brush, "Drawing Note", NoteType.DRAWING),
        FabItem(Icons.Default.Mic, "Audio Note", NoteType.AUDIO)
    )
    
    Box(modifier = modifier) {
        // Semi-transparent background overlay when menu is expanded
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { expanded = false } // Close menu when clicking outside
            )
        }
        
        // Menu items column positioned above the FAB
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(durationMillis = 400, easing = EaseInOut)) + 
                       expandVertically(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = tween(durationMillis = 300, easing = EaseInOut)) + 
                       shrinkVertically(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.padding(bottom = 72.dp)
                ) {
                    fabItems.forEachIndexed { index, item ->
                        FabItemRow(
                            item = item,
                            onFabItemClicked = {
                                expanded = false
                                onFabItemClicked(item.noteType)
                            },
                            index = index
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
        
        // Main FAB positioned at the bottom end
        Box(modifier = Modifier.align(Alignment.BottomEnd)) {
            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = backgroundColor
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Note",
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}

@Composable
fun FabItemRow(
    item: FabItem,
    onFabItemClicked: () -> Unit,
    index: Int = 0
) {
    // Animate the scale of the FAB item with staggered delay based on index
    val scale = remember { Animatable(0.8f) }
    
    LaunchedEffect(key1 = Unit) {
        // Add staggered delay based on index
        kotlinx.coroutines.delay(index * 50L)
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        
        SmallFloatingActionButton(
            onClick = onFabItemClicked,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label
            )
        }
    }
}
