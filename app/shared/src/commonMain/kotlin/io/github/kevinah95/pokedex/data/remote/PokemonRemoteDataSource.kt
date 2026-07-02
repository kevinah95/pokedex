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

import io.github.kevinah95.pokedex.data.remote.dto.PokemonDetailResponse
import io.github.kevinah95.pokedex.data.remote.dto.PokemonSpeciesResponse
import io.github.kevinah95.pokedex.data.remote.dto.EvolutionChainResponse
import io.github.kevinah95.pokedex.data.remote.dto.EvolutionLinkDto
import io.github.kevinah95.pokedex.data.remote.dto.PokemonListResponse
import io.github.kevinah95.pokedex.domain.entity.Pokemon
import io.github.kevinah95.pokedex.domain.entity.PokemonDetail
import io.github.kevinah95.pokedex.domain.entity.EvolutionStage
import io.github.kevinah95.pokedex.domain.entity.PokemonStat
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

    override fun fetchPokemonDetail(number: Int): Flow<PokemonDetail> = flow {
        val detailUrl = "https://pokeapi.co/api/v2/pokemon/$number"
        val detailResp = httpClient.get(detailUrl).body<PokemonDetailResponse>()

        val speciesUrl = "https://pokeapi.co/api/v2/pokemon-species/$number"
        val speciesResp = httpClient.get(speciesUrl).body<PokemonSpeciesResponse>()

        val speciesGenus = speciesResp.genera
            .firstOrNull { it.language.name == "en" }?.genus
            ?: speciesResp.genera.firstOrNull()?.genus
            ?: "Unknown"

        val evoUrl = speciesResp.evolutionChain.url
        val evoResp = httpClient.get(evoUrl).body<EvolutionChainResponse>()

        val evolutionList = mutableListOf<EvolutionStage>()
        parseEvolutionChain(evoResp.chain, evolutionList)

        val domainStats = detailResp.stats.map { statSlot ->
            val formattedName = when (statSlot.stat.name.lowercase()) {
                "hp" -> "HP"
                "attack" -> "ATK"
                "defense" -> "DEF"
                "special-attack" -> "SATK"
                "special-defense" -> "SDEF"
                "speed" -> "SPD"
                else -> statSlot.stat.name.uppercase()
            }
            PokemonStat(
                name = formattedName,
                value = statSlot.baseStat
            )
        }

        val imageUrl = detailResp.sprites.other?.home?.frontDefault ?: ""
        val types = detailResp.types.map { it.type.name }

        val detail = PokemonDetail(
            number = detailResp.id,
            name = detailResp.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            imageUrl = imageUrl,
            types = types,
            height = detailResp.height,
            weight = detailResp.weight,
            species = speciesGenus,
            evolutionChain = evolutionList,
            stats = domainStats
        )

        emit(detail)
    }.flowOn(ioDispatcher)

    private fun parseEvolutionChain(link: EvolutionLinkDto, outList: MutableList<EvolutionStage>) {
        val name = link.species.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val id = link.species.url.split("/").filter { it.isNotEmpty() }.last().toIntOrNull() ?: 1

        val triggerDetails = link.evolutionDetails.firstOrNull()
        val trigger = when {
            triggerDetails == null -> null
            triggerDetails.minLevel != null -> "Level ${triggerDetails.minLevel}"
            triggerDetails.trigger?.name == "trade" -> "Trade"
            triggerDetails.trigger?.name == "use-item" -> "Use Item"
            else -> triggerDetails.trigger?.name?.replaceFirstChar { it.titlecase() } ?: "Evolve"
        }

        val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/$id.png"

        outList.add(
            EvolutionStage(
                number = id,
                name = name,
                imageUrl = imageUrl,
                evolutionTrigger = trigger
            )
        )

        for (nextLink in link.evolvesTo) {
            parseEvolutionChain(nextLink, outList)
        }
    }
}
