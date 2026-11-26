package com.sippy.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sippy.app.R
import com.sippy.app.data.FriendEntity
import com.sippy.app.domain.GrowthStage
import com.sippy.app.domain.Mood
import com.sippy.app.domain.Rules
import com.sippy.app.domain.Species
import com.sippy.app.domain.stageFor
import com.sippy.app.ui.ConfirmDialog
import com.sippy.app.ui.FriendCanvas
import com.sippy.app.ui.LocalAnimationsEnabled
import com.sippy.app.ui.UiAction
import com.sippy.app.ui.UiState
import com.sippy.app.ui.theme.GlassCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendsScreen(state: UiState, onAction: (UiAction) -> Unit) {
    var showAdd by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newSpecies by remember { mutableStateOf(Species.DROPLET) }
    var menuFor by remember { mutableStateOf<Long?>(null) }
    var renameFor by remember { mutableStateOf<FriendEntity?>(null) }
    var renameText by remember { mutableStateOf("") }
    var deleteFor by remember { mutableStateOf<FriendEntity?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Your friends", style = MaterialTheme.typography.headlineSmall)
            Button(
                onClick = { showAdd = true },
                enabled = Rules.canAddFriend(state.friends.size)
            ) { Text("Add friend") }
        }
        Text("${state.friends.size}/${Rules.MAX_FRIENDS} friends · tap to activate, long-press for options")
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.friends, key = { it.id }) { friend ->
                val species = runCatching { Species.valueOf(friend.species) }.getOrDefault(Species.DROPLET)
                val stage = stageFor(friend.growthPoints)
                GlassCard(
                    modifier = Modifier.combinedClickable(
                        onClick = { onAction(UiAction.SelectFriend(friend.id)) },
                        onLongClick = { menuFor = friend.id }
                    ),
                    alpha = if (friend.id == state.activeFriend?.id) 0.3f else 0.12f
                ) {
                    Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box {
                            FriendCanvas(
                                species = species, mood = Mood.CONTENT, stage = stage,
                                phase = 0.1f, size = 92.dp,
                                animationsEnabled = LocalAnimationsEnabled.current,
                                description = "${friend.name}, ${species.label}, ${stage.label}"
                            )
                            IconButton(
                                onClick = { menuFor = friend.id },
                                modifier = Modifier.align(Alignment.TopEnd).size(44.dp)
                            ) {
                                Icon(painterResource(R.drawable.ic_more), contentDescription = "Options for ${friend.name}", tint = MaterialTheme.colorScheme.onSurface)
                            }
                            DropdownMenu(expanded = menuFor == friend.id, onDismissRequest = { menuFor = null }) {
                                DropdownMenuItem(text = { Text("Make active") }, onClick = {
                                    onAction(UiAction.SelectFriend(friend.id)); menuFor = null
                                })
                                DropdownMenuItem(text = { Text("Rename") }, onClick = {
                                    renameFor = friend; renameText = friend.name; menuFor = null
                                })
                                DropdownMenuItem(text = { Text("Delete") }, enabled = Rules.canDeleteFriend(state.friends.size),
                                    onClick = { deleteFor = friend; menuFor = null })
                            }
                        }
                        Text(friend.name, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center,
                            maxLines = 1)
                        Text("${species.label} · ${stage.label}", style = MaterialTheme.typography.labelSmall)
                        Text("${friend.growthPoints} growth pts", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }

    if (showAdd) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add a friend") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName, onValueChange = { newName = it.take(24) },
                        label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    SpeciesPicker(newSpecies) { newSpecies = it }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onAction(UiAction.AddFriend(newName, newSpecies)); showAdd = false; newName = ""
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }

    renameFor?.let { friend ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { renameFor = null },
            title = { Text("Rename ${friend.name}") },
            text = {
                OutlinedTextField(
                    value = renameText, onValueChange = { renameText = it.take(24) },
                    label = { Text("New name") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onAction(UiAction.RenameFriend(friend.id, renameText)); renameFor = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { renameFor = null }) { Text("Cancel") } }
        )
    }

    deleteFor?.let { friend ->
        ConfirmDialog(
            title = "Delete ${friend.name}?",
            body = "This removes the friend and its growth. Drink entries stay in history. You cannot delete your last friend.",
            onDismiss = { deleteFor = null },
            onConfirm = { onAction(UiAction.DeleteFriend(friend.id)); deleteFor = null }
        )
    }
}
