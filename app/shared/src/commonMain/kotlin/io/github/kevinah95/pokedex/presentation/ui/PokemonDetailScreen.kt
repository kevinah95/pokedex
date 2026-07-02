/*
 * Copyright 2026 kevinah95 (Kevin A. Hernández Rostrán)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.kevinah95.pokedex.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.kevinah95.pokedex.domain.entity.PokemonDetail
import io.github.kevinah95.pokedex.presentation.pokemon.PokemonDetailViewModel

@Composable
fun PokemonDetailScreen(
    number: Int,
    viewModel: PokemonDetailViewModel,
    onBack: () -> Unit,
    onNavigateTo: (Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(number) {
        viewModel.loadPokemonDetail(number)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else if (state.error != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Error loading Pokemon: ${state.error}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Back to list", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onBack() })
                }
            } else {
                state.pokemonDetail?.let { detail ->
                    PokemonDetailContent(
                        detail = detail,
                        onBack = onBack,
                        onNavigateTo = onNavigateTo
                    )
                }
            }
        }
    }
}

@Composable
fun PokemonDetailContent(
    detail: PokemonDetail,
    onBack: () -> Unit,
    onNavigateTo: (Int) -> Unit
) {
    val firstType = detail.types.firstOrNull() ?: ""
    val themeColor = getPokemonTypeColor(firstType)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColor)
            .verticalScroll(rememberScrollState())
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Pokédex",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = "Favorite",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Pokemon Name and Number Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = detail.name,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = "#" + detail.number.toString().padStart(3, '0'),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
        }

        // Pokemon Sprite Image Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = detail.imageUrl,
                contentDescription = detail.name,
                modifier = Modifier.size(190.dp)
            )
        }

        // Detailed Stats Curved Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Type capsules
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    detail.types.forEach { type ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .background(getPokemonTypeColor(type), shape = RoundedCornerShape(16.dp))
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Row with 3 columns (Species, Height, Weight)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DetailStatItem(
                        value = detail.species.removeSuffix(" Pokémon"),
                        label = "Species"
                    )

                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.LightGray))

                    DetailStatItem(
                        value = formatHeight(detail.height),
                        label = "Height"
                    )

                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.LightGray))

                    DetailStatItem(
                        value = formatWeight(detail.weight),
                        label = "Weight"
                    )
                }

                // Candy and Location Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DetailStatItem(
                        value = "${detail.name} Candy",
                        label = "Candy"
                    )

                    DetailStatItem(
                        value = if (detail.evolutionChain.size > 1) "Evolution Only" else "Wild / Hatch",
                        label = "Location"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Evolution Section
                Text(
                    text = "Evolution",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = themeColor
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    detail.evolutionChain.forEachIndexed { index, stage ->
                        if (index > 0) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stage.evolutionTrigger ?: "Evolve",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "➔",
                                    color = themeColor,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onNavigateTo(stage.number) }
                                .padding(4.dp)
                        ) {
                            AsyncImage(
                                model = stage.imageUrl,
                                contentDescription = stage.name,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = stage.name,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF2C2C2C)
                            )
                            Text(
                                text = "#" + stage.number.toString().padStart(3, '0'),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Base Stats Section
                Text(
                    text = "Base Stats",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = themeColor
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    detail.stats.forEach { stat ->
                        val isHp = stat.name.uppercase() == "HP"
                        val minVal = if (isHp) stat.value * 2 + 110 else ((stat.value * 2 + 5) * 0.9).toInt()
                        val maxVal = if (isHp) stat.value * 2 + 204 else ((stat.value * 2 + 99) * 1.1).toInt()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stat.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.Gray,
                                modifier = Modifier.width(48.dp)
                            )
                            Text(
                                text = stat.value.toString(),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF2C2C2C),
                                modifier = Modifier.width(36.dp)
                            )

                            val progress = (stat.value / 255f).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progress },
                                color = themeColor,
                                trackColor = Color.LightGray.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )

                            Text(
                                text = "min.$minVal max.$maxVal",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .width(90.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailStatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF2C2C2C)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

fun formatHeight(heightDm: Double): String {
    // PokeAPI returns height in decimeters. E.g. 15 dm = 1.5 m
    val meters = heightDm / 10.0
    val totalInches = meters * 39.3701
    val feet = (totalInches / 12).toInt()
    val inches = kotlin.math.round(totalInches % 12).toInt()
    return "${feet}' ${inches}\""
}

fun formatWeight(weightHg: Double): String {
    // PokeAPI returns weight in hectograms. E.g. 405 hg = 40.5 kg
    val kg = weightHg / 10.0
    val lbs = kg * 2.20462
    val roundedLbs = kotlin.math.round(lbs * 10) / 10.0
    return "$roundedLbs lbs"
}

