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
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import coil3.compose.AsyncImage
import io.github.kevinah95.pokedex.presentation.navigation.Route
import io.github.kevinah95.pokedex.presentation.navigation.navigationConfig
import io.github.kevinah95.pokedex.presentation.pokemon.PokemonDetailViewModel
import io.github.kevinah95.pokedex.presentation.pokemon.PokemonViewModel
import io.github.kevinah95.pokedex.presentation.ui.PokedexTheme
import io.github.kevinah95.pokedex.presentation.ui.PokemonDetailScreen
import io.github.kevinah95.pokedex.presentation.ui.getPokemonTypeColor
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    viewModel: PokemonViewModel = koinViewModel(),
    detailViewModel: PokemonDetailViewModel = koinViewModel()
) {
    val backStack = rememberNavBackStack(navigationConfig, Route.PokemonList)

    PokedexTheme {
        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeAt(backStack.lastIndex)
                }
            },
            entryProvider = { key ->
                when (key) {
                    is Route.PokemonList -> NavEntry(key) {
                        val state by viewModel.uiState.collectAsState()
                        var searchQuery by remember { mutableStateOf("") }
                        val filteredList = remember(state.pokemonList, searchQuery) {
                            state.pokemonList.filter { pokemon ->
                                pokemon.name.contains(searchQuery, ignoreCase = true) ||
                                        pokemon.number.toString().contains(searchQuery)
                            }
                        }

                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = "Pokédex",
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
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search Pokémon...") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search"
                                        )
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Clear"
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    )
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Powered by ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                    AsyncImage(
                                        model = "https://raw.githubusercontent.com/PokeAPI/media/master/logo/pokeapi_256.png",
                                        contentDescription = "PokéAPI Logo",
                                        modifier = Modifier.height(24.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f),
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
                                    } else if (filteredList.isEmpty()) {
                                        Text(
                                            text = "No Pokémon found",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                        )
                                    } else {
                                        LazyVerticalGrid(
                                            columns = GridCells.Adaptive(minSize = 160.dp),
                                            contentPadding = PaddingValues(16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            items(filteredList) { pokemon ->
                                                PokemonCard(
                                                    name = pokemon.name,
                                                    number = pokemon.number,
                                                    imageUrl = pokemon.imageUrl,
                                                    types = pokemon.types,
                                                    modifier = Modifier.clickable {
                                                        backStack.add(Route.PokemonDetail(pokemon.number))
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is Route.PokemonDetail -> NavEntry(key) {
                        PokemonDetailScreen(
                            number = key.number,
                            viewModel = detailViewModel,
                            onBack = {
                                if (backStack.size > 1) {
                                    backStack.removeAt(backStack.lastIndex)
                                }
                            },
                            onNavigateTo = { num ->
                                backStack.add(Route.PokemonDetail(num))
                            }
                        )
                    }
                    else -> throw IllegalArgumentException("Unexpected key $key")
                }
            }
        )
    }
}

@Composable
fun PokemonCard(
    name: String,
    number: Int,
    imageUrl: String,
    types: List<String>,
    modifier: Modifier = Modifier
) {
    val formattedNumber = "#" + number.toString().padStart(3, '0')
    val firstType = types.firstOrNull() ?: "normal"
    val typeColor = getPokemonTypeColor(firstType)

    val isDark = isSystemInDarkTheme()
    val baseColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val cardBg = Color(
        red = typeColor.red * 0.12f + baseColor.red * 0.88f,
        green = typeColor.green * 0.12f + baseColor.green * 0.88f,
        blue = typeColor.blue * 0.12f + baseColor.blue * 0.88f,
        alpha = 1f
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formattedNumber,
                style = MaterialTheme.typography.labelMedium,
                color = typeColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}