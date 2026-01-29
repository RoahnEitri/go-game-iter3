package com.roahacha.gogame.Common;

import java.io.*;
import java.net.Socket;

/**
 * Fasada dla gniazda sieciowego (Socket), upraszczająca operacje wejścia/wyjścia.
 * <p>
 * Klasa ta upraszcza strumienie {@link DataInputStream} oraz {@link DataOutputStream}, 
 * oferując proste metody do przesyłania i odbierania danych typu integer bez konieczności 
 * bezpośredniej obsługi wyjątków przy każdym wywołaniu.
 */
public class SocketFacade implements AutoCloseable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    /**
     * Inicjalizuje fasadę gniazda i otwiera strumienie wejścia/wyjścia.
     * @param socket Otwarte gniazdo połączenia sieciowego.
     * @throws IOException Jeśli wystąpi błąd podczas inicjalizacji strumieni.
     */
    public SocketFacade(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    /**
     * Wysyła liczbę całkowitą (int) przez połączenie sieciowe.
     * @param value Wartość do wysłania.
     */
    public void sendInt(int value) {
        try {
            out.writeInt(value);
            out.flush();
        } catch (IOException e) {
            handleConnectionError(e);
        }
    }

    /**
     * Odbiera liczbę całkowitą (int) z połączenia sieciowego.
     * @return Odebrana wartość lub -1 w przypadku błędu.
     */
    public int readInt() {
        try {
            return in.readInt();
        } catch (IOException e) {
            handleConnectionError(e);
            return -1;
        }
    }

    /**
     * Prywatna metoda pomocnicza do logowania błędów połączenia i rzucania wyjątków wykonawczych.
     * @param e Wyjątek wejścia/wyjścia.
     */
    private void handleConnectionError(IOException e) {
        System.err.println("Błąd połączenia: " + e.getMessage());
        throw new RuntimeException("Stracono połączenie", e);
    }

    /**
     * Zamyka gniazdo sieciowe i zwalnia zasoby.
     * @throws Exception Jeśli wystąpi błąd podczas zamykania.
     */
    @Override
    public void close() throws Exception {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
