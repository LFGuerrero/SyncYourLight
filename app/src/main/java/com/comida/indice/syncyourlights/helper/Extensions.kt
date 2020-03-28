package com.comida.indice.syncyourlights.helper

inline fun <LET> LET?.orElse(block: () -> LET) : LET { return this ?: block()}