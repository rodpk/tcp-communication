import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor implements Runnable {
    
    private final int PORTA = 9999;

    private ArrayList<ConnectionHandler> conexoes;
    private ServerSocket servidor;
    private ExecutorService poolConexoes;

    private boolean isDone;

    public Servidor() {
        conexoes = new ArrayList<>();
        isDone = false;
    }

    @Override
    public void run() {

        try {

            servidor = new ServerSocket(PORTA);
            poolConexoes = Executors.newCachedThreadPool();

            System.out.println("Servidor ouvindo a porta " + PORTA);
            while(!isDone) {
                Socket client = servidor.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                conexoes.add(handler);
                poolConexoes.execute(handler);
                 
            }

        } catch(Exception ex) {
            desligar();
        }
    }

    public void transmitir(String mensagem) {
        for (ConnectionHandler ch : conexoes) {
            if (ch != null) {
                ch.enviarMensagem(mensagem);
            }
        }
    }


    public void desligar() {
        try {
            isDone = true;
            poolConexoes.shutdown();
            if (!servidor.isClosed()) {
                servidor.close();
            }

            for (ConnectionHandler ch : conexoes) {
                ch.shutdown();
            }

        } catch(Exception ex) {
           // ignore 
        }
    }

    class ConnectionHandler implements Runnable {

        private Socket cliente;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public ConnectionHandler(Socket client) {
            this.cliente = client;
        }
        
        @Override
        public void run() {
            try {

                out = new PrintWriter(cliente.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                out.println("Entre com um nome de usuario: ");
                nickname = in.readLine();
                
                System.out.println(String.format("[%s] conectado!", nickname));
                transmitir(String.format("[%s] se juntou a conversa!", nickname));
                String mensagem;
                while ( (mensagem = in.readLine()) != null ) {
                    if (mensagem.startsWith("/sair")) {
                        transmitir(String.format("[%s] saiu da conversa!", nickname));
                    System.out.println(String.format("[%s] saiu da conversa!", nickname));

                        shutdown();
                    } else {
                        transmitir(String.format("[%s]: %s", nickname, mensagem));
                    }
                }
            } catch(Exception ex) {
                shutdown();
            }
        }

        public void enviarMensagem(String mensagem) {
            out.println(mensagem);
        }

        public void shutdown() {
            try {

                in.close();
                out.close();

                if(!cliente.isClosed()) {
                    cliente.close();
                }

            } catch (Exception ex) {
                // ignore
            }
        }
    }

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.run();
    }
}
