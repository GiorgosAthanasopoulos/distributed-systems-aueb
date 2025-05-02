package giorgosathanasopoulos.com.github.distributed_systems_aueb;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.client.ClientConfig;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Filters;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.FilterStoresRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.NetworkUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;

public class Client {
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        Filters filters = getFilters();
        FilterStoresRequest request = new FilterStoresRequest(Message.UserAgent.CLIENT, UID.getNextUID(),
                Request.Action.FILTER_STORES, filters);

        Socket server = connectToServer();
        if (server == null) {
            System.err.println("Failed to connect to server.");
            return;
        }

        if (!NetworkUtils.sendMessage(server, request)) {
            System.err.println("Failed to send request to server.");
            return;
        }

        String jsonResponse = getResponse(server);
        if (jsonResponse == null) {
            System.err.println("Failed to receive response from server.");
            return;
        }

        System.out.println(jsonResponse);
        // TODO: print results and buy products
    }

    private static String getInput(String p_Prompt, Predicate<String> p_Validator) {
        System.out.print(p_Prompt);
        String input = sc.nextLine();

        while (!p_Validator.test(input)) {
            System.out.print("Invalid input. " + p_Prompt);
            input = sc.nextLine();
        }

        return input;
    }

    private static Filters getFilters() {
        String latitudeS = getInput("Enter latitude: ", (p_Input) -> {
            try {
                Double.valueOf(p_Input);
                return true;
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
                return false;
            }
        });
        String longitudeS = getInput("Enter longitude: ", (p_Input) -> {
            try {
                Double.valueOf(p_Input);
                return true;
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
                return false;
            }
        });
        String radiusS = getInput("Enter radius (in km): ", (p_Input) -> {
            try {
                Double.valueOf(p_Input);
                return true;
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
                return false;
            }
        });
        String foodTypesS = getInput(
                "Enter food types (comma separated, without spaces in between values, e.g. pizza,coffee,desert): ",
                (p_Input) -> {
                    String[] foodTypes = p_Input.split(",");
                    for (String foodType : foodTypes) {
                        if (foodType.isEmpty()) {
                            return false;
                        }
                    }
                    return true;
                });
        String starsS = getInput(
                "Enter restaurant # of stars (comma separated, without spaces in between values, e.g. 1,2,3): ",
                (p_Input) -> {
                    String[] stars = p_Input.split(",");
                    for (String star : stars) {
                        try {
                            int starValue = Integer.parseInt(star);
                            if (starValue < 1 || starValue > 5) {
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println(e.getMessage());
                            return false;
                        }
                    }
                    return true;
                });
        String pricesS = getInput(
                "Enter restaurant price range (comma separated, without spaces in between values, e.g. $,$$,$$$): ",
                (p_Input) -> {
                    String[] prices = p_Input.split(",");
                    for (String price : prices) {
                        if (!price.matches("\\$+")) {
                            return false;
                        }
                    }
                    return true;
                });

        double latitude = Double.parseDouble(latitudeS);
        double longitude = Double.parseDouble(longitudeS);
        double radius = Double.parseDouble(radiusS);
        List<String> foodTypes = List.of(foodTypesS.split(","));
        List<Integer> stars = List.of(starsS.split(",")).stream()
                .map(Integer::parseInt)
                .toList();
        List<Integer> prices = List.of(pricesS.split(",")).stream()
                .map(price -> price.length())
                .toList();

        return new Filters(latitude, longitude, radius, foodTypes, stars, prices);
    }

    private static Socket connectToServer() {
        try {
            Socket socket = new Socket(ClientConfig.c_SERVER_HOST, ClientConfig.c_SERVER_PORT);
            return socket;
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            return null;
        }
    }

    private static String getResponse(Socket p_Socket) {
        try (Scanner sc1 = new Scanner(p_Socket.getInputStream())) {
            return sc1.nextLine();
        } catch (IOException e) {
            System.err.println("Error reading response from server: " + e.getMessage());
            return null;
        }
    }
}
