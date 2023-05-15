package backend;

import io.github.cdimascio.dotenv.Dotenv;
import spark.Spark;
import java.sql.*;
import java.util.ArrayList;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Server {

    private static void returnCards() {
        Spark.get("/getCards", (rq, rs) -> {
            Connection conn = connection();
            String sql = "SELECT * FROM custom_pokemons";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet result = stmt.executeQuery();
            ArrayList<JsonObject> cards = new ArrayList<>();
            while (result.next()) {
                JsonObject card = new JsonObject();
                String[] namesData = returnData(result.getString("idPokemon"), result.getString("idAbility")
                                            , result.getString("idMovement1"),result.getString("idMovement2")
                                            , result.getString("idMovement3"), result.getString("idMovement4"));

                card.addProperty("name", namesData[0]);
                card.addProperty("image", namesData[1]);
                card.addProperty("ability", namesData[2]);
                card.addProperty("movement1", namesData[3]);
                card.addProperty("movement2", namesData[4]);
                card.addProperty("movement3", namesData[5]);
                card.addProperty("movement4", namesData[6]);
                cards.add(card);
            }
            return cards;
        });
    }
    
    private static String[] returnData(String idPokemon, String idAbility, String idMovement1
                                    ,String idMovement2, String idMovement3, String idMovement4){

        String[] data = new String[7];
        try {
            Connection conn = connection();
            String sql = "SELECT image, name FROM pokemon WHERE idPokemon = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, idPokemon);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            data[0] = rs.getString("name");
            data[1] = rs.getString("image");

            sql = "SELECT name FROM abilities WHERE idAbility = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, idAbility);
            rs = stmt.executeQuery();
            rs.next();
            data[2] = rs.getString("name");

            sql = "SELECT name FROM movements WHERE idMovement = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, idMovement1);
            rs = stmt.executeQuery();
            rs.next();
            data[3] = rs.getString("name");

            sql = "SELECT name FROM movements WHERE idMovement = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, idMovement2);
            rs = stmt.executeQuery();
            rs.next();
            data[4] = rs.getString("name");

            sql = "SELECT name FROM movements WHERE idMovement = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, idMovement3);
            rs = stmt.executeQuery();
            rs.next();
            data[5] = rs.getString("name");

            sql = "SELECT name FROM movements WHERE idMovement = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, idMovement4);
            rs = stmt.executeQuery();
            rs.next();
            data[6] = rs.getString("name");

        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println(data);
        return data;
    }

    private static String[] returnIds (String name, String ability, String movement1, String movement2, String movement3, String movement4){
        String[] ids = new String[6];
        try {
            Connection conn = connection();

            String sql = "SELECT idPokemon FROM pokemon WHERE name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            ids[0] = rs.getString("idPokemon");

            sql = "SELECT idAbility FROM abilities WHERE name = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, ability);
            rs = stmt.executeQuery();
            rs.next();
            ids[1] = rs.getString("idAbility");

            sql = "SELECT idMovement FROM movements WHERE name = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, movement1);
            rs = stmt.executeQuery();
            rs.next();
            ids[2] = rs.getString("idMovement");

            sql = "SELECT idMovement FROM movements WHERE name = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, movement2);
            rs = stmt.executeQuery();
            rs.next();
            ids[3] = rs.getString("idMovement");

            sql = "SELECT idMovement FROM movements WHERE name = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, movement3);
            rs = stmt.executeQuery();
            rs.next();
            ids[4] = rs.getString("idMovement");

            sql = "SELECT idMovement FROM movements WHERE name = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, movement4);
            rs = stmt.executeQuery();
            rs.next();
            ids[5] = rs.getString("idMovement");

        } catch (Exception e) {
            System.out.println(e);
        }
        return ids;
    }

    private static Connection connection() throws Exception {
        Dotenv dotenv = Dotenv.load();
        String DB_CONNECTION = "jdbc:mysql://";
        String DB_HOST = dotenv.get("DB_HOST");
        String DB_PORT = dotenv.get("DB_PORT");
        String DB_NAME = dotenv.get("DB_NAME");
        String url = DB_CONNECTION + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");
        return DriverManager.getConnection(url, user, password);
    }

    private static void postPokemon() {
        Spark.post("/postPokemonCard", (rq, res) -> {

            String body = rq.body();
            Gson gson = new Gson();
            JsonObject data = gson.fromJson(body, JsonObject.class);
            String[] ids = returnIds(data.get("name").getAsString(),data.get("ability").getAsString()
                                    , data.get("movement1").getAsString(), data.get("movement2").getAsString()
                                    , data.get("movement3").getAsString(), data.get("movement4").getAsString());
            
            String name = ids[0];
            String ability = ids[1];
            String movement1 = ids[2];
            String movement2 = ids[3];
            String movement3 = ids[4];
            String movement4 = ids[5];

            JsonArray evs = data.get("evs").getAsJsonArray();
            JsonArray ivs = data.get("ivs").getAsJsonArray();

            Connection conn = connection();

            String sql = "INSERT INTO custom_pokemons (idPokemon, idAbility, idMovement1, idMovement2, idMovement3, idMovement4, "
                    +
                    "evsAttack, evsDefense, evsSpatk, evsSpdef, evsSpeed, " +
                    "ivsAttack, ivsDefense, ivsSpatk, ivsSpdef, ivsSpeed) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setString(2, ability);
                stmt.setString(3, movement1);
                stmt.setString(4, movement2);
                stmt.setString(5, movement3);
                stmt.setString(6, movement4);
                stmt.setString(7, evs.get(0).getAsString());
                stmt.setString(8, evs.get(1).getAsString());
                stmt.setString(9, evs.get(2).getAsString());
                stmt.setString(10, evs.get(3).getAsString());
                stmt.setString(11, evs.get(4).getAsString());
                stmt.setString(12, ivs.get(0).getAsString());
                stmt.setString(13, ivs.get(1).getAsString());
                stmt.setString(14, ivs.get(2).getAsString());
                stmt.setString(15, ivs.get(3).getAsString());
                stmt.setString(16, ivs.get(4).getAsString());
                stmt.executeUpdate();
            }
            String sql2 = "SELECT MAX(idCustomPokemon) FROM custom_pokemons";
            PreparedStatement stmt2 = conn.prepareStatement(sql2);
            ResultSet rs = stmt2.executeQuery();
            return rs.next() ? rs.getString(1) : "0";
        });
    }

    private static void config() {
        Spark.staticFiles.location("");
        Spark.port(8080);
    }

    private static void redirect() {
        Spark.get("/", (req, res) -> {
            res.redirect("/html/home.html");
            return "";
        });
    }

    private static void attendTutorialRequest() {
        Spark.get("/tutorial/:index", (rq, rs) -> {
            String param = rq.params(":index");
            int index = Integer.parseInt(param);
            return JsonTutorial.get_tutorial(index);
        });
    }

    private static void attendTypesRequest(ConnMysql conn) {
        Spark.get("/getTypes", (req, res) -> {
            return new DamageRelations().build_damage_SQL(conn);
        });
    }

    private static void attendGlossaryRequest(ConnMysql conn) {
        Spark.get("/getTerms", (req, res) -> {
            String query = "SELECT name, description FROM terms";
            ResultSet rs = conn.queryMysql(query);
            Gson gson = new GsonBuilder().create();
            ArrayList<Terms> array = new ArrayList<>();

            while (rs.next()) {
                Terms item = new Terms(rs.getString("name"), rs.getString("description"));
                array.add(item);
            }

            return gson.toJson(array);

        });
    }

    private static void attendPokemonSearcher(ConnMysql conn) {
        Spark.get("/getPokemon/:pokemonName", (rq, res) -> {
            String pokemonName = rq.params(":pokemonName");
            String query = "SELECT idPokemon, pokemon.name, a1.name as ability1, " +
                    "a2.name as ability2, a3.name as ability3, t1.picture as type1, " +
                    "t2.picture as type2, hpBase, attackBase, defenseBase, spatkBase, " +
                    "spdefBase, speedBase, image FROM pokemon JOIN abilities a1 on " +
                    "a1.idAbility = idAbility1 LEFT JOIN abilities a2 on " +
                    "a2.idAbility = idAbility2 LEFT JOIN abilities a3 on " +
                    "a3.idAbility = idAbility3 JOIN types t1 on t1.idType = " +
                    "pokemon.idType1 LEFT JOIN types t2 on t2.idType = pokemon.idType2 " +
                    "where pokemon.name = \"" + pokemonName + "\";";
            ResultSet rs = conn.queryMysql(query);
            rs.next();
            Gson gson = new GsonBuilder().serializeNulls().create();
            ArrayList<Pokemon> array = new ArrayList<>();
            Pokemon pokemon = new Pokemon(rs.getInt("idPokemon"),
                    rs.getString("name"),
                    rs.getString("ability1"),
                    rs.getString("ability2"),
                    rs.getString("ability3"),
                    rs.getString("type1"),
                    rs.getString("type2"),
                    rs.getInt("hpBase"),
                    rs.getInt("attackBase"),
                    rs.getInt("defenseBase"),
                    rs.getInt("spatkBase"),
                    rs.getInt("spdefBase"),
                    rs.getInt("speedBase"),
                    rs.getString("image"));
            pokemon.buildPokemonMoves(conn);
            pokemon.buildStrategies(conn);
            pokemon.buildWeaknesses(conn);
            array.add(pokemon);
            return gson.toJson(array);
        });
    }

    private static void attendPokemonItemSearcher(ConnMysql conn) {
        Spark.get("/getItems", (req, res) -> {

            String query = "SELECT name, description, icon FROM objects";
            ResultSet rs = conn.queryMysql(query);
            Gson gson = new GsonBuilder().create();
            ArrayList<Items> array = new ArrayList<>();

            while (rs.next()) {
                Items item = new Items(rs.getString("name"), rs.getString("description"), rs.getString("icon"));
                array.add(item);
            }

            return gson.toJson(array);

        });
    }

    private static void attendPokemonMovementSearcher(ConnMysql conn) {
        Spark.get("/getMovements", (req, res) -> {
            String query = "SELECT idMovement, movements.name, types.name AS type, movement_class.name AS category, pp, power, accuracy, priority "
                    +
                    "FROM movements " +
                    "LEFT JOIN types ON movements.idType = types.idType " +
                    "LEFT JOIN movement_class ON movements.idClass = movement_class.idClass";
            ResultSet rs = conn.queryMysql(query);
            Gson gson = new GsonBuilder().serializeNulls().create();
            ArrayList<Movements> array = new ArrayList<>();

            while (rs.next()) {
                Movements item = new Movements(rs.getString("name"),
                        rs.getString("type"),
                        rs.getString("category"),
                        rs.getInt("pp"),
                        rs.getInt("power"),
                        rs.getInt("accuracy"),
                        rs.getInt("priority"),
                        rs.getInt("idMovement"),
                        conn);
                array.add(item);
            }
            return gson.toJson(array);
        });
    }

    private static void attendPokemonWhoLearnsMovements(ConnMysql conn) {
        Spark.get("/getPokemonsWhoLearnsMovements/:idMovement", (rq, rs) -> {
            Gson gson = new GsonBuilder().create();
            String idMovement = rq.params(":idMovement");
            String result = gson.toJson(Movements.getPokemonsWhoLearnsMovements(Integer.parseInt(idMovement), conn));
            return result;
        });
    }

    private static void attendPokemonAbilitySearcher(ConnMysql conn) {
        Spark.get("/getAbilities", (req, res) -> {
            Gson gson = new GsonBuilder().create();
            String query = "SELECT idAbility, name, description FROM abilities";
            ResultSet rs = conn.queryMysql(query);
            ArrayList<Abilities> listaHabilidades = new ArrayList<>();
            while (rs.next()) {
                Abilities ability = new Abilities(rs.getInt("idAbility"), rs.getString("name"),
                        rs.getString("description"));
                listaHabilidades.add(ability);
            }
            String result = gson.toJson(listaHabilidades);
            return result;
        });
    }

    private static void getPokemonsName(ConnMysql conn) {
        Spark.get("/getPokemonsName", (req, res) -> {
            Gson gson = new GsonBuilder().create();
            String query = "SELECT name FROM pokemon";
            ResultSet rs = conn.queryMysql(query);
            ArrayList<String> listaPokemons = new ArrayList<>();
            while (rs.next()) {
                listaPokemons.add(rs.getString("name"));
            }
            String result = gson.toJson(listaPokemons);
            return result;
        });
    }

    private static void attendPokemonWhoLearnsAbilities(ConnMysql conn) {
        Spark.get("/getPokemonsWhoLearnsAbilities/:idAbility", (req, res) -> {
            Gson gson = new GsonBuilder().create();
            int idAbility = Integer.parseInt(req.params(":idAbility"));
            String query = "SELECT pokemon.name, pokemon.image, pokemon.idAbility1, pokemon.idAbility2, pokemon.idAbility3 "
                    +
                    "FROM pokemon JOIN abilities on abilities.idAbility = pokemon.idAbility1 " +
                    String.format("WHERE idAbility = %d OR idAbility2 = %d OR idAbility3 = %d;", idAbility, idAbility,
                            idAbility);
            ResultSet rs = conn.queryMysql(query);
            ArrayList<JsonObject> array = new ArrayList<>();
            while (rs.next()) {
                JsonObject jsonobject = new JsonObject();
                jsonobject.addProperty("name", rs.getString("name"));
                jsonobject.addProperty("image", rs.getString("image"));
                int ability1 = rs.getInt("idAbility1");
                int ability2 = rs.getInt("idAbility2");
                int ability3 = rs.getInt("idAbility3");
                String abilityType = "";
                if (ability1 == idAbility) {
                    abilityType = "First Ability";
                } else if (ability2 == idAbility) {
                    abilityType = "Second Ability";
                } else if (ability3 == idAbility) {
                    abilityType = "Hidden Ability";
                }
                jsonobject.addProperty("abilityType", abilityType);
                array.add(jsonobject);
            }
            String result = gson.toJson(array);
            return result;
        });
    }


    private static void sendEmail() {
        Spark.get("/sendEmail", (req, res) -> {
    
            final String username = "pokemonpsapp@gmail.com";
            final String password = "cqys fcif wbxt ynbx"; // replace with your actual password
    
            String to = req.queryParams("to");
            String subject = req.queryParams("subject");
            String body = req.queryParams("body");
    
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
    
            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });
    
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(to));
                message.setSubject(subject);
                message.setText(body);
    
                Transport.send(message);
    
                System.out.println("Done");
    
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
            return "Email sent.";
        });
    }

    private static void getNatures(ConnMysql conn) {
        Spark.get("/getNatures", (req, res) -> {
            Gson gson = new GsonBuilder().create();
            String query = "SELECT name FROM natures";
            ResultSet rs = conn.queryMysql(query);
            ArrayList<String> listaNaturalezas = new ArrayList<>();
            while (rs.next()) {
                String nature = rs.getString("name");
                listaNaturalezas.add(nature);
            }
            String result = gson.toJson(listaNaturalezas);
            return result;
        });
    }
    
    
     // Registro de usuarios
    private static void attendRegisterUserRequest(Connection conn) {
        Spark.post("/register", (req, res) -> {
            // Crear objeto JSON a partir del cuerpo de la petición HTTP POST
            JSONObject requestBody = new JSONObject(req.body());

            String username = requestBody.getString("username");
            String email = requestBody.getString("email");
            String birthdate = requestBody.getString("birthdate");
            String password = requestBody.getString("password");
            // Validar que los datos no estén vacíos
            if (username.isEmpty() || email.isEmpty() || birthdate.isEmpty() || password.isEmpty()) {
                res.status(400); // Código de respuesta para bad request
                return "Faltan datos";
            }

            // Verificar si el email del usuario ya existe en la base de datos
            String query = String.format("SELECT COUNT(*) FROM users WHERE email = '%s'", email);
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count > 0) {
                res.status(409); // Código de respuesta para conflicto
                return "El email del usuario ya existe en la base de datos";
            }

            // Verificar si el nombre de usuario ya existe en la base de datos
            query = String.format("SELECT COUNT(*) FROM users WHERE username = '%s'", username);
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            rs.next();
            count = rs.getInt(1);
            if (count > 0) {
                res.status(409); // Código de respuesta para conflicto
                return "El nombre usuario ya existe en la base de datos";
            }

            // Insertar usuario en la base de datos
            query = String.format("INSERT INTO users (email, username, password, birthdayDate) VALUES ('%s', '%s', '%s', '%s')", email, username, password, birthdate);
            stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
            
            stmt.close();
            conn.close();
            res.status(200);
            return "Usuario registrado correctamente";
        });
    } 

    public static void main(String[] args) throws Exception {
        ConnMysql conn = new ConnMysql();
        Connection conn2 = connection();
        Server.config();
        Server.redirect();
        Server.attendTutorialRequest();
        Server.attendTypesRequest(conn);
        Server.attendGlossaryRequest(conn);
        Server.attendPokemonSearcher(conn);
        Server.attendPokemonItemSearcher(conn);
        Server.attendPokemonMovementSearcher(conn);
        Server.attendPokemonWhoLearnsMovements(conn);
        Server.attendPokemonAbilitySearcher(conn);
        Server.attendPokemonWhoLearnsAbilities(conn);
        Server.getPokemonsName(conn);
        Server.sendEmail();
        Server.getNatures(conn);
        Server.postPokemon();
        Server.returnCards();
        Server.attendRegisterUserRequest(conn2);
    }

}
