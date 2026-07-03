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
package io.github.kevinah95.pokedex.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonSpeciesResponse(
    val genera: List<PokemonGenusDto>,
    @SerialName("evolution_chain") val evolutionChain: EvolutionChainUrlDto
)

@Serializable
data class PokemonGenusDto(
    val genus: String,
    val language: LanguageDto
)

@Serializable
data class LanguageDto(
    val name: String
)

@Serializable
data class EvolutionChainUrlDto(
    val url: String
)
