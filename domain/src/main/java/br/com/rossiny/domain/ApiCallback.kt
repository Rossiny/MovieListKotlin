package br.com.rossiny.domain

interface ApiCallback<T> {

    fun onSuccess(obj: T)
    fun onError(message: String)
}