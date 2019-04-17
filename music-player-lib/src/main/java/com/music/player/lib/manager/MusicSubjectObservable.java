package com.music.player.lib.manager;

import java.util.Observable;

/**
 * TinyHung@Outlook.com
 * 2018/1/18.
 * Observable
 */

public class MusicSubjectObservable extends Observable {

    public MusicSubjectObservable(){

    }
    public void updataSubjectObserivce(Object data){
        setChanged();
        notifyObservers(data);
    }
}
