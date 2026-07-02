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
package io.github.kevinah95.pokedex.data.remote

import io.github.kevinah95.pokedex.data.remote.dto.PokemonListResponse
import io.github.kevinah95.pokedex.domain.entity.Pokemon
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

private const val POKEMON_URL = "https://pokeapi.co/api/v2/pokemon?limit=151"

class PokemonRemoteDataSource(
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher
) : IPokemonRemoteDataSource {

    override fun fetchPokemonList(): Flow<List<Pokemon>> = flow {
        val response = httpClient.get(POKEMON_URL).body<PokemonListResponse>()
        val pokemonList = response.results.mapIndexed { index, dto ->
            val number = dto.url.split("/").filter { it.isNotEmpty() }.last().toIntOrNull() ?: (index + 1)
            val formattedName = dto.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            Pokemon(
                name = formattedName,
                number = number
            )
        }
        emit(pokemonList)
    }.flowOn(ioDispatcher)
}
