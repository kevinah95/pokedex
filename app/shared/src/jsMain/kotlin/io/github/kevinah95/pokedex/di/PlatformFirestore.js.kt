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
package io.github.kevinah95.pokedex.di

import kotlinx.browser.window

actual fun getFirestoreBaseUrl(): String {
    val hostname = window.location.hostname
    return if (hostname == "localhost" || hostname == "127.0.0.1" || hostname == "0.0.0.0") {
        "http://127.0.0.1:8080/v1/projects/pokedex-kevinah95/databases/(default)/documents"
    } else {
        "https://firestore.googleapis.com/v1/projects/pokedex-kevinah95/databases/(default)/documents"
    }
}

