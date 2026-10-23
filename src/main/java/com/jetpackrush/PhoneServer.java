package com.jetpackrush;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Petit serveur web du jeu : sert la page manette ({@code assets/phone.html}) au téléphone
 * et transmet ses messages à un {@link PhoneInput}.
 *
 * <p>L'adresse contient un jeton secret : toute requête qui ne le porte pas reçoit une 404.
 *
 * <p>Deux façons de joindre le serveur : par le réseau local (le téléphone doit être sur le même
 * réseau, sans pare-feu ni Wi-Fi isolant), ou par un tunnel ssh vers localhost.run, qui donne une
 * adresse publique en https et marche partout. Le tunnel démarre en arrière-plan : {@link #getUrl()}
 * renvoie l'adresse locale tout de suite, puis celle du tunnel dès qu'elle est connue.
 * On le désactive avec {@code -Dphone.tunnel=false}.
 */
public class PhoneServer {

    static final int PORT = 8080;
    private static final int MAX_BODY = 64;

    private static final Pattern TUNNEL_URL =
        Pattern.compile("https://(?!admin\\.)[a-z0-9-]+\\.(?:lhr\\.life|localhost\\.run)");

    private final HttpServer server;
    private final String token;
    private volatile String url;
    private volatile Process tunnel;

    private PhoneServer(HttpServer server, String token, String url) {
        this.server = server;
        this.token = token;
        this.url = url;
    }

    /**
     * Démarre le serveur.
     *
     * @param input l'entrée qui reçoit les messages du téléphone
     * @return le serveur démarré
     * @throws IOException si aucun port n'a pu être ouvert
     */
    public static PhoneServer start(PhoneInput input) throws IOException {
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
        } catch (IOException busy) {
            // Port 8080 déjà pris : on laisse le système en choisir un autre
            server = HttpServer.create(new InetSocketAddress(0), 0);
        }
        byte[] page = readPage();
        String token = newToken();
        String root = "/" + token + "/";
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.equals(root)) {
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                reply(exchange, 200, page);
            } else if (path.equals(root + "input") && "POST".equals(exchange.getRequestMethod())) {
                try (InputStream body = exchange.getRequestBody()) {
                    input.handle(new String(body.readNBytes(MAX_BODY), StandardCharsets.UTF_8));
                }
                reply(exchange, 204, new byte[0]);
            } else {
                reply(exchange, 404, new byte[0]);
            }
        });
        server.setExecutor(Executors.newFixedThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable, "phone-server");
            thread.setDaemon(true);
            return thread;
        }));
        server.start();
        String url = "http://" + findLanAddress() + ":" + server.getAddress().getPort() + root;
        return new PhoneServer(server, token, url);
    }

    private static String newToken() {
        byte[] bytes = new byte[8];
        new SecureRandom().nextBytes(bytes);
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private static byte[] readPage() throws IOException {
        try (InputStream in = Assets.open("phone.html")) {
            return in.readAllBytes();
        }
    }

    private static void reply(HttpExchange exchange, int status, byte[] body) throws IOException {
        exchange.sendResponseHeaders(status, status == 204 ? -1 : body.length);
        if (status != 204) {
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        }
        exchange.close();
    }

    /**
     * Cherche l'adresse de l'ordinateur sur le réseau local, celle que le téléphone peut joindre.
     *
     * <p>On demande d'abord au système par quelle adresse il sortirait vers Internet (aucun paquet
     * n'est envoyé) : c'est celle du Wi-Fi ou du câble, pas celle d'un pont Docker ou d'une VM.
     * Sinon on prend une adresse privée en écartant ces interfaces virtuelles.
     *
     * @return une adresse IPv4 privée, ou {@code localhost} si aucune n'est trouvée
     */
    static String findLanAddress() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByAddress(new byte[] {8, 8, 8, 8}), 53);
            InetAddress local = socket.getLocalAddress();
            if (local instanceof Inet4Address && local.isSiteLocalAddress()) {
                return local.getHostAddress();
            }
        } catch (IOException e) {
            // pas de route par défaut : on cherche à la main
        }
        try {
            for (NetworkInterface net : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!net.isUp() || net.isLoopback() || isVirtual(net)) {
                    continue;
                }
                for (InetAddress address : Collections.list(net.getInetAddresses())) {
                    if (address instanceof Inet4Address && address.isSiteLocalAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (IOException e) {
            // on retombe sur localhost
        }
        return "localhost";
    }

    private static boolean isVirtual(NetworkInterface net) {
        String name = net.getName();
        return net.isVirtual() || name.startsWith("docker") || name.startsWith("br-")
            || name.startsWith("veth") || name.startsWith("virbr") || name.startsWith("vmnet")
            || name.startsWith("vboxnet");
    }

    /**
     * Lance le tunnel ssh vers localhost.run, sauf avec {@code -Dphone.tunnel=false}.
     * Sans ssh ou sans Internet, rien ne change : l'adresse locale reste utilisée.
     */
    public void startTunnel() {
        if ("false".equals(System.getProperty("phone.tunnel"))) {
            return;
        }
        int port = server.getAddress().getPort();
        try {
            Process process = new ProcessBuilder("ssh", "-T",
                "-o", "StrictHostKeyChecking=accept-new",
                "-o", "ServerAliveInterval=30",
                "-o", "ExitOnForwardFailure=yes",
                "-R", "80:localhost:" + port, "nokey@localhost.run")
                .redirectErrorStream(true).start();
            tunnel = process;
            Thread reader = new Thread(() -> readTunnelOutput(process), "phone-tunnel");
            reader.setDaemon(true);
            reader.start();
        } catch (IOException e) {
            System.err.println("Tunnel indisponible (ssh introuvable ?) : " + e.getMessage());
        }
    }

    private void readTunnelOutput(Process process) {
        try (BufferedReader out = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = out.readLine()) != null) {
                String found = parseTunnelUrl(line);
                if (found != null) {
                    url = found + "/" + token + "/";
                }
            }
        } catch (IOException e) {
            // le tunnel est fermé : on garde la dernière adresse connue
        }
    }

    /**
     * Cherche l'adresse publique dans une ligne affichée par localhost.run.
     *
     * @param line une ligne de la sortie de ssh
     * @return l'adresse {@code https://...} du tunnel, ou {@code null} si la ligne n'en contient pas
     */
    static String parseTunnelUrl(String line) {
        Matcher matcher = TUNNEL_URL.matcher(line);
        return matcher.find() ? matcher.group() : null;
    }

    /**
     * @return l'adresse à ouvrir sur le téléphone : celle du tunnel dès qu'il est prêt,
     *         sinon celle du réseau local
     */
    public String getUrl() {
        return url;
    }

    /** Arrête le serveur et le tunnel. */
    public void stop() {
        Process process = tunnel;
        if (process != null) {
            process.destroy();
        }
        server.stop(0);
    }
}
