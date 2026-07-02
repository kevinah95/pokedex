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

import io.github.kevinah95.pokedex.di.getFirestoreEmulatorHost
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
import kotlinx.serialization.Serializable

@Serializable
data class FirestoreListResponse(
    val documents: List<FirestoreDocument>? = null
)

@Serializable
data class FirestoreDocument(
    val name: String,
    val fields: FirestoreFields
)

@Serializable
data class FirestoreFields(
    val number: FirestoreIntegerField? = null,
    val name: FirestoreStringField? = null,
    val imageUrl: FirestoreStringField? = null,
    val types: FirestoreArrayField? = null,
    val height: FirestoreDoubleField? = null,
    val weight: FirestoreDoubleField? = null,
    val species: FirestoreStringField? = null,
    val stats: FirestoreArrayField? = null,
    val evolutionChain: FirestoreArrayField? = null
)

@Serializable
data class FirestoreIntegerField(val integerValue: String)

@Serializable
data class FirestoreStringField(val stringValue: String)

@Serializable
data class FirestoreDoubleField(
    val doubleValue: Double? = null,
    val integerValue: String? = null
)

@Serializable
data class FirestoreArrayField(val arrayValue: FirestoreArrayValue)

@Serializable
data class FirestoreArrayValue(val values: List<FirestoreValue> = emptyList())

@Serializable
data class FirestoreValue(
    val stringValue: String? = null,
    val mapValue: FirestoreMapValue? = null
)

@Serializable
data class FirestoreMapValue(val fields: FirestoreMapFields)

@Serializable
data class FirestoreMapFields(
    val name: FirestoreStringField? = null,
    val value: FirestoreIntegerField? = null,
    val number: FirestoreIntegerField? = null,
    val imageUrl: FirestoreStringField? = null,
    val evolutionTrigger: FirestoreValue? = null
)

class PokemonRemoteDataSource(
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher
) : IPokemonRemoteDataSource {

    override fun fetchPokemonList(): Flow<List<Pokemon>> = flow {
        val host = getFirestoreEmulatorHost()
        val url = "http://$host:8080/v1/projects/pokedex-kevinah95/databases/(default)/documents/pokemons?pageSize=200"
        val response = httpClient.get(url).body<FirestoreListResponse>()
        val pokemonList = response.documents?.map { doc ->
            val number = doc.fields.number?.integerValue?.toIntOrNull() ?: 0
            val name = doc.fields.name?.stringValue ?: ""
            val imageUrl = doc.fields.imageUrl?.stringValue ?: ""
            val types = doc.fields.types?.arrayValue?.values?.mapNotNull { it.stringValue } ?: emptyList()
            Pokemon(name = name, number = number, imageUrl = imageUrl, types = types)
        }?.sortedBy { it.number } ?: emptyList()
        emit(pokemonList)
    }.flowOn(ioDispatcher)

    override fun fetchPokemonDetail(number: Int): Flow<PokemonDetail> = flow {
        val host = getFirestoreEmulatorHost()
        val url = "http://$host:8080/v1/projects/pokedex-kevinah95/databases/(default)/documents/pokemons/$number"
        val doc = httpClient.get(url).body<FirestoreDocument>()
        val fields = doc.fields
        
        val detail = PokemonDetail(
            number = fields.number?.integerValue?.toIntOrNull() ?: number,
            name = fields.name?.stringValue ?: "",
            imageUrl = fields.imageUrl?.stringValue ?: "",
            types = fields.types?.arrayValue?.values?.mapNotNull { it.stringValue } ?: emptyList(),
            height = fields.height?.doubleValue ?: fields.height?.integerValue?.toDoubleOrNull() ?: 0.0,
            weight = fields.weight?.doubleValue ?: fields.weight?.integerValue?.toDoubleOrNull() ?: 0.0,
            species = fields.species?.stringValue ?: "",
            stats = fields.stats?.arrayValue?.values?.mapNotNull { value ->
                val fieldsMap = value.mapValue?.fields
                val statName = fieldsMap?.name?.stringValue
                val statValue = fieldsMap?.value?.integerValue?.toIntOrNull()
                if (statName != null && statValue != null) {
                    PokemonStat(name = statName, value = statValue)
                } else null
            } ?: emptyList(),
            evolutionChain = fields.evolutionChain?.arrayValue?.values?.mapNotNull { value ->
                val fieldsMap = value.mapValue?.fields
                val stageNum = fieldsMap?.number?.integerValue?.toIntOrNull()
                val stageName = fieldsMap?.name?.stringValue ?: fieldsMap?.imageUrl?.stringValue?.split("/")?.lastOrNull()?.replace(".png", "") ?: "Unknown"
                val stageImg = fieldsMap?.imageUrl?.stringValue
                val trigger = fieldsMap?.evolutionTrigger?.stringValue
                if (stageNum != null && stageImg != null) {
                    EvolutionStage(
                        number = stageNum,
                        name = stageName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        imageUrl = stageImg,
                        evolutionTrigger = trigger
                    )
                } else null
            } ?: emptyList()
        )
        emit(detail)
    }.flowOn(ioDispatcher)
}
