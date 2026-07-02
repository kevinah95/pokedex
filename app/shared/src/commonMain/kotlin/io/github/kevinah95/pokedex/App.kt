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
package io.github.kevinah95.pokedex

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.kevinah95.pokedex.presentation.pokemon.PokemonViewModel
import io.github.kevinah95.pokedex.presentation.pokemon.PokemonDetailViewModel
import io.github.kevinah95.pokedex.presentation.ui.PokemonDetailScreen
import org.koin.compose.viewmodel.koinViewModel

sealed interface Screen {
    data object List : Screen
    data class Detail(val pokemonNumber: Int) : Screen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    viewModel: PokemonViewModel = koinViewModel(),
    detailViewModel: PokemonDetailViewModel = koinViewModel()
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }

    MaterialTheme {
        when (val screen = currentScreen) {
            is Screen.List -> {
                val state by viewModel.uiState.collectAsState()

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "First Generation Pokedex",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator()
                        } else if (state.error != null) {
                            Text(
                                text = "Error: ${state.error}",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 160.dp),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(state.pokemonList) { pokemon ->
                                    PokemonCard(
                                        name = pokemon.name,
                                        number = pokemon.number,
                                        modifier = Modifier.clickable {
                                            currentScreen = Screen.Detail(pokemon.number)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            is Screen.Detail -> {
                PokemonDetailScreen(
                    number = screen.pokemonNumber,
                    viewModel = detailViewModel,
                    onBack = { currentScreen = Screen.List },
                    onNavigateTo = { num -> currentScreen = Screen.Detail(num) }
                )
            }
        }
    }
}

@Composable
fun PokemonCard(name: String, number: Int, modifier: Modifier = Modifier) {
    val formattedNumber = "#" + number.toString().padStart(3, '0')

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formattedNumber,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}