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
package io.github.kevinah95.pokedex.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Pokemon(
    val name: String,
    val number: Int,
    val imageUrl: String = ""
)

@Serializable
data class PokemonDetail(
    val number: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
    val height: Double, // in decimeters
    val weight: Double, // in hectograms
    val species: String, // genus description
    val evolutionChain: List<EvolutionStage>,
    val stats: List<PokemonStat>
)

@Serializable
data class EvolutionStage(
    val number: Int,
    val name: String,
    val imageUrl: String,
    val evolutionTrigger: String? = null // e.g. "Level 25" or "Trade"
)

@Serializable
data class PokemonStat(
    val name: String, // e.g. "HP", "ATK", "DEF", "SATK", "SDEF", "SPD"
    val value: Int
)
