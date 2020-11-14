package com.wwc2.main.driver.steer;

/**
 * the steer driver interface.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public interface SteerDriverable {
    void enterStudyMode();
    void exitStudyMode();
    void keyPressed(byte keyID);
    void keyStore();
    void keyReset();
    void keyClear();
    void keyStudy();
    void keyInfo();

    void enterStudyMode_Panel();
    void exitStudyMode_Panel();
    void keyPressed_Panel(byte keyID);
    void keyStore_Panel();
    void keyReset_Panel();
    void keyClear_Panel();
    void keyStudy_Panel();
}

