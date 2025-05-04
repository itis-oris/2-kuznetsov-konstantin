package ru.itis.balckjack.exceptions;

public class PlayersLimitException extends Exception{
    public PlayersLimitException() {
        super("Комната уже заполнена");
    }
}
