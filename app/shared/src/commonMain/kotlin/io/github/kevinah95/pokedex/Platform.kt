package io.github.kevinah95.pokedex

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform