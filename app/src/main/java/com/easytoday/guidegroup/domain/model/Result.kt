// app/src/main/java/com/easytoday/guidegroup/domain/model/Result.kt
package com.easytoday.guidegroup.domain.model

/**
 * Une classe scellée pour encapsuler les résultats des opérations asynchrones.
 * Permet de représenter les états de Succès, Erreur et Chargement.
 */
sealed class Result<out T> {
    object Loading : Result<Nothing>() // Nothing permet de mapper à n'importe quel T
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()
    object Initial : Result<Nothing>() // vérifier que c'est bien 'object Initial'
}





