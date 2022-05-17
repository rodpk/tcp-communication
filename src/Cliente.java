import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Cliente implements Runnable {

    private final int PORTA = 9999;

    private Socket cliente;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isDone;

    @Override
    public void run() {
        try {
            cliente = new Socket("127.0.0.1", PORTA);

            out = new PrintWriter(cliente.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));

            InputHandler inHandler = new InputHandler();
            Thread thread = new Thread(inHandler);
            thread.start();

            String inMessage;

            while( (inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }

        } catch (Exception ex) {
            shutdown();
        }
    }

    public void shutdown() {
        isDone = true;
        try {
            in.close();
            out.close();
            if(!cliente.isClosed()) {
                cliente.close();
            }
        } catch(Exception ex) {
            // ignore
        }
    }
    class InputHandler implements Runnable {

        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!isDone) {
                    String mensagem=  inReader.readLine();

                    if(mensagem.equals("/sair")) {
                        out.println(mensagem);
                        inReader.close();
                        shutdown();
                    } else {
                        out.println(mensagem);
                    }
                }
            } catch (IOException ex) {
                shutdown();
            }
        }

    }

    public static void main(String[] args) {
        Cliente cliente = new Cliente();
        cliente.run();
    }
}
