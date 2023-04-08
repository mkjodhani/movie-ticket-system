package com.sequencer;

import com.helper.Config;

/**
 * @author mkjodhani
 * @project
 * @since 04/04/23
 */
public class Main {
    static int port = Config.sequencerPort;
    public static void main(String[] args) {
        Sequencer sequencer = new Sequencer("sequencer" ,port);
        sequencer.run();
    }
}
