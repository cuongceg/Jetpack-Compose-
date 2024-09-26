package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_DECREASE
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel(){
    private val _uiState = MutableStateFlow(GameUiState())
    var uiState : StateFlow<GameUiState> = _uiState.asStateFlow()// not to modify the state outside the view model
    var userGuess by mutableStateOf("")
        private set

    private var usedWords: MutableSet<String> = mutableSetOf()
    private lateinit var currentWord: String

    init {
        resetGame()
    }

    private fun pickRandomAndShuffle():String{
        currentWord = allWords.random()

        if(usedWords.contains(currentWord)){
            return pickRandomAndShuffle()
        }else{
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    private fun shuffleCurrentWord(currentWords: String):String{
        val tempWords = currentWords.toCharArray()
        tempWords.shuffle()

        while(tempWords.toString().equals(currentWords, false)){
            tempWords.shuffle()
        }
        return String(tempWords)
    }

    fun updateUserGuess(guessWord : String){
        userGuess = guessWord
    }

    fun checkUserGuess(){
        if(userGuess.equals(currentWord, true)){
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
            _uiState.value = _uiState.value.copy(isGuessWordWrong = false)}
        else{
            _uiState.update { currentState -> currentState.copy(isGuessWordWrong = true)}
        }
        updateUserGuess("")
    }

    fun checkEnoughPoints(): Boolean {
        if(_uiState.value.score < SCORE_DECREASE){
            return false
        }
        _uiState.update { currentState -> currentState.copy(score = currentState.score.minus(
            SCORE_DECREASE))}
        return true
    }

    fun showSolution(): String {
        return currentWord
    }

    fun skipWord(){
        userGuess = ""
        updateGameState(_uiState.value.score)
    }

    private fun updateGameState(updatedScore: Int) {
        if(usedWords.size == MAX_NO_OF_WORDS){
            _uiState.update { currentState ->
                currentState.copy(
                    isGameOver = true,
                    score = updatedScore,
                )
            }
        }else{
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessWordWrong = false,
                    currentScrambledWord = pickRandomAndShuffle(),
                    score = updatedScore,
                    currentWordCount = currentState.currentWordCount.inc(),
                )
            }
        }
    }

    fun resetGame(){
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomAndShuffle())
    }
}