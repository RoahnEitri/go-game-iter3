package com.roahacha.gogame.Common;

/**
 * Interfejs bazowy dla wzorca projektowego Polecenie (Command).
 * Pozwala na enkapsulację żądania jako obiektu, co umożliwia parametryzację 
 * i wykonywanie operacji w sposób ujednolicony.
 */
public interface Command {
    /**
     * Metoda wykonująca logikę polecenia.
     */
    void execute();
}
