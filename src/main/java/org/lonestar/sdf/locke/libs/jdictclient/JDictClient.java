/*
 * Copyright (C) 2016 Robert Gill <locke@sdf.lonestar.org>
 *
 * This file is part of JDictClient.
 *
 * JDictClient is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JDictClient is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with JDictClient.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.lonestar.sdf.locke.libs.jdictclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;

import static org.lonestar.sdf.locke.libs.jdictclient.Command.Type.AUTH;
import static org.lonestar.sdf.locke.libs.jdictclient.Command.Type.CLIENT;
import static org.lonestar.sdf.locke.libs.jdictclient.Command.Type.DEFINE;
import static org.lonestar.sdf.locke.libs.jdictclient.Command.Type.HELP;
import static org.lonestar.sdf.locke.libs.jdictclient.Command.Type.MATCH;
import static org.lonestar.sdf.locke.libs.jdictclient.Command.Type.QUIT;
import static org.lonestar.sdf.locke.libs.jdictclient.Command.Type.SHOW_DATABASES;
import static org.lonestar.sdf.locke.libs.jdictclient.Command.Type.SHOW_INFO;
import static org.lonestar.sdf.locke.libs.jdictclient.Command.Type.SHOW_SERVER;
import static org.lonestar.sdf.locke.libs.jdictclient.Command.Type.SHOW_STRATEGIES;

/**
 * JDictClient: <a href="http://dict.org">DICT</a> dictionary client for Java.
 *
 * @author Robert Gill &lt;locke@sdf.lonestar.org&gt;
 *
 */
public class JDictClient {
    public static final int DEFAULT_PORT = Connection.DEFAULT_PORT;
    public static final int DEFAULT_TIMEOUT = Connection.DEFAULT_TIMEOUT;

    private static String libraryName;
    private static String libraryVendor;
    private static String libraryVersion;

    private String clientString;
    private String host;
    private int port;
    private int timeout;
    private Response resp;

    private Connection connection;
    private PrintWriter out;
    private BufferedReader in;

    private String serverInfo;
    private String capabilities;
    private String connectionId;

    /**
     * Construct a new JDictClient.
     *
     * @param host DICT host
     * @param port port number
     * @param timeout connection timeout
     *
     */
    public JDictClient(String host, int port, int timeout) {
        setClientString(getLibraryName() + " " + getLibraryVersion());

        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    /**
     * Create a new JDictClient object and connect to specified host.
     *
     * @param host DICT host to connect to
     * @throws IOException from associated Connection Socket
     * @return new JDictClient instance
     *
     */
    public static JDictClient connect(String host) throws IOException {
        JDictClient dictClient =
          JDictClient.connect(host, DEFAULT_PORT, DEFAULT_TIMEOUT);
        return dictClient;
    }

    /**
     * Create a new JDictClient object and connect to specified host and port.
     *
     * @param host DICT host to connect to
     * @param port port number to connect to
     * @throws IOException from associated Connection Socket
     * @return new JDictClient instance
     *
     */
    public static JDictClient connect(String host, int port)
          throws IOException  {
        JDictClient dictClient = new JDictClient(host, port, DEFAULT_TIMEOUT);
        dictClient.connect();
        return dictClient;
    }

    /**
     * Create a new JDictClient object and connect to specified host and port.
     *
     * @param host DICT host to connect to
     * @param port port number to connect to
     * @param timeout connection socket timeout
     * @throws IOException from associated Connection Socket
     * @return new JDictClient instance
     *
     */
    public static JDictClient connect(String host, int port, int timeout)
          throws IOException {
        JDictClient dictClient = new JDictClient(host, port, timeout);
        dictClient.connect();
        return dictClient;
    }

    /**
     * Open connection to the DICT server.
     * <p>
     * If this instance is not currently connected, attempt to open a
     * connection to the server previously specified.
     *
     * @throws IOException from associated Connection Socket
     *
     */
    public void connect() throws IOException {
        if (connection == null) {
            connection = new Connection(host, port, timeout);
            connection.connect();
            sendClient();
        }
    }

    /**
     * Close connection to the DICT server.
     *
     * @throws IOException from associated Connection Socket
     * @return true if successful, false if closed by remote
     *
     */
    public boolean close() throws IOException {
        boolean rv = true;

        Response resp = quit();
        if (resp.getStatus() != 221)
          throw new DictException(host, resp.getStatus(), resp.getMessage());

        connection.close();
        in = null;
        out = null;

        return rv;
    }

    /**
     * Get library name.
     *
     * @return library name String
     *
     */
    public static String getLibraryName() {
        if (libraryName == null) {
            String packageName = JDictClient.class.getPackage().getName();
            ResourceBundle rb = ResourceBundle.getBundle(packageName + ".library");
            libraryName = rb.getString("library.name");
        }
        return libraryName;
    }

    /**
     * Get library version.
     *
     * @return library version String
     *
     */
    public static String getLibraryVersion() {
        if (libraryVersion == null) {
            String packageName = JDictClient.class.getPackage().getName();
            ResourceBundle rb = ResourceBundle.getBundle(packageName + ".library");
            libraryVersion = rb.getString("library.version");
        }
        return libraryVersion;
    }

    /**
     * Get library developer/vendor name.
     *
     * @return library vendor String
     *
     */
    public static String getLibraryVendor() {
        if (libraryVendor == null) {
            String packageName = JDictClient.class.getPackage().getName();
            ResourceBundle rb = ResourceBundle.getBundle(packageName + ".library");
            libraryVendor = rb.getString("library.vendor");
        }
        return libraryVendor;
    }

    /**
     * Set the client string sent to the server.
     * <p>
     * Identify the client to the server with the provided string. It is
     * usually the application name and version number. This method must be
     * called before any instances of the JDictClient class are created. If
     * this method is not called, the library name and version will be used.
     *
     * @param clientString client string to send to DICT server
     *
     */
    public void setClientString(String clientString) {
        if (this.clientString == null)
          this.clientString = clientString;
    }

    /**
     * Get the response for the last command sent.
     * <p>
     * It's overwritten by subsequent commands.
     *
     * @return the response for the last command.
     *
     */
    public Response getResponse() {
        return resp;
    }

    /**
     * Send client information to DICT server.
     * <p>
     * This method is called by the connect() method. It's not necessary to use
     * it in normal practice.
     *
     * @throws IOException from associated Connection Socket
     *
     */
    private void sendClient() throws IOException {
        Command command = new Command.Builder(CLIENT)
                                     .setParamString(clientString).build();
        List<Response> responses = command.execute(connection);
        Response resp = responses.get(0);
        if (resp.getStatus() != 250)
          throw new DictException(host, resp.getStatus(), resp.getMessage());
    }

    /**
     * Get the connection banner for the currently connected server.
     *
     * @return Banner or null if not connected
     *
     */
    public Banner getBanner() {
        return connection.getBanner();
    }

    /**
     * Get the server information as written by the database administrator.
     *
     * @throws IOException from associated Connection Socket
     * @return server information string
     *
     */
    public String getServerInfo() throws IOException {
        Command.Builder builder = new Command.Builder(SHOW_SERVER);
        Command command = builder.build();
        List<Response> responses = command.execute(connection);
        return responses.get(0).getRawData();
    }

    /**
     * Get summary of server commands.
     *
     * @throws IOException from associated Connection Socket
     * @return server help string
     *
     */
    public String getHelp() throws IOException {
        Command.Builder builder = new Command.Builder(HELP);
        Command command = builder.build();
        List<Response> responses = command.execute(connection);
        return responses.get(0).getRawData();
    }

    /**
     * Authenticate with the server.
     *
     * @param username authenticate with username
     * @param secret authenticate with password
     * @throws IOException from associated Connection Socket
     * @return true on success, false on failure
     *
     */
    public boolean authenticate(String username, String secret)
          throws IOException {
        boolean rv = false;
        Command command = new Command.Builder(AUTH)
                                     .setUsername(username)
                                     .setPassword(secret)
                                     .build();
        List<Response> responses = command.execute(connection);
        if (responses.get(0).getStatus() == 230)
          rv = true;
        return rv;
    }

    /**
     * Get list of available dictionary databases from the server.
     *
     * FIXME: Allow requesting that dictionary info be set while querying
     * server.
     *
     * @throws IOException from associated Connection Socket
     * @return list of dictionaries
     *
     */
    public List<Dictionary> getDictionaries() throws IOException {
        Command command = new Command.Builder(SHOW_DATABASES).build();
        List<Response> responses = command.execute(connection);
        return (List<Dictionary>) responses.get(0).getData();
    }

    /**
     * Get detailed dictionary info for the specified dictionary.
     *
     * @param dictionary the dictionary for which to get information
     * @throws IOException from associated Connection Socket
     * @return dictionary info string
     *
     */
    public String getDictionaryInfo(String dictionary) throws IOException {
        Command command = new Command.Builder(SHOW_INFO)
                                     .setDatabase(dictionary)
                                     .build();
        List<Response> responses = command.execute(connection);
        return responses.get(0).getRawData();
    }

    /**
     * Get detailed dictionary info for the specified dictionary.
     *
     * @param dictionary the dictionary for which to get information
     * @throws IOException from associated Connection Socket
     * @return dictionary info string
     *
     */
    public String getDictionaryInfo(Dictionary dictionary) throws IOException {
        Command command = new Command.Builder(SHOW_INFO)
                                     .setDatabase(dictionary.getDatabase())
                                     .build();
        List<Response> responses = command.execute(connection);
        return responses.get(0).getRawData();
    }

    /**
     * Get list of available match strategies from the server.
     *
     * @throws IOException from associated Connection Socket
     * @return list of strategies
     *
     */
    public List<Strategy> getStrategies() throws IOException {
        Command command = new Command.Builder(SHOW_STRATEGIES).build();
        List<Response> responses = command.execute(connection);
        return (List<Strategy>) responses.get(0).getData();
    }

    /**
     * Get definition for word from DICT server.
     *
     * @param word the word to define
     * @throws IOException from associated Connection Socket
     * @return a list of definitions for word
     *
     */
    public List<Definition> define(String word) throws IOException {
        Command command = new Command.Builder(DEFINE)
                                     .setParamString(word)
                                     .build();
        List<Response> responses = command.execute(connection);
        return collect_definitions(responses);
    }

    /**
     * Get definition for word from DICT server.
     *
     * @param dictionary the dictionary in which to find the definition
     * @param word the word to define
     * @throws IOException from associated Connection Socket
     * @return a list of definitions for word
     *
     */
    public List<Definition> define(String dictionary, String word)
          throws IOException {
        Command command = new Command.Builder(DEFINE)
                                     .setDatabase(dictionary)
                                     .setParamString(word)
                                     .build();
        List<Response> responses = command.execute(connection);
        return collect_definitions(responses);
    }

    /**
     * Match word using requested strategy.
     *
     * @param strategy the strategy to use for matching
     * @param word the word to match
     * @throws IOException from associated Connection Socket
     * @return a list of matching words and the dictionaries they are found in
     *
     */
    public List<Match> match(String strategy, String word) throws IOException {
        Command command = new Command.Builder(MATCH)
                                     .setStrategy(strategy)
                                     .setParamString(word)
                                     .build();
        List<Response> responses = command.execute(connection);
        return (List<Match>) responses.get(0).getData();
    }

    /**
     * Match word using requested strategy.
     *
     * @param dictionary the dictionary to search
     * @param strategy the strategy to use for matching
     * @param word the word to match
     * @throws IOException from associated Connection Socket
     * @return a list of matching words and the dictionaries they are found in
     *
     */
    public List<Match> match(String dictionary, String strategy, String word)
          throws IOException {
        Command command = new Command.Builder(MATCH)
                                     .setDatabase(dictionary)
                                     .setStrategy(strategy)
                                     .setParamString(word)
                                     .build();
        List<Response> responses = command.execute(connection);
        return (List<Match>) responses.get(0).getData();
    }

    /**
     * Send QUIT command to server.
     *
     * @throws IOException from associated Connection Socket
     *
     */
    private Response quit() throws IOException {
        Command command = new Command.Builder(QUIT).build();
        List<Response> responses = command.execute(connection);
        return responses.get(0);
    }

    private ArrayList<Definition> collect_definitions(List<Response> responses)
    {
        ListIterator<Response> itr = responses.listIterator(1);
        ArrayList<Definition> definitions = new ArrayList<Definition>();
        while (itr.hasNext()) {
            Response response = itr.next();
            if (response.getStatus() == 151)
              definitions.add((Definition) response.getData());
        }
        return definitions;
    }
}
