import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final String API_KEY = "f51d4a249f6c6489f408b7c4";
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean ejecutar = true;

        System.out.println("¡Bienvenido al Conversor de Monedas!");

        while (ejecutar) {
            mostrarMenu();
            try {
                int opcion = Integer.parseInt(scanner.nextLine());

                switch (opcion) {
                    case 1:
                        convertir("USD", "ARS", scanner);
                        break;
                    case 2:
                        convertir("ARS", "USD", scanner);
                        break;
                    case 3:
                        convertir("EUR", "ARS", scanner);
                        break;
                    case 4:
                        convertir("BRL", "USD", scanner);
                        break;
                    case 5:
                        convertir("CLP", "ARS", scanner);
                        break;
                    case 6:
                        convertir("GBP", "ARS", scanner);
                        break;
                    case 0:
                        ejecutar = false;
                        System.out.println("Gracias por usar el Conversor de Monedas. ¡Hasta pronto!");
                        break;
                    default:
                        System.out.println("Opción inválida. Por favor, seleccione una opción válida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Debe ingresar un número.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void mostrarMenu() {
        System.out.println("\n=== MENÚ DE CONVERSIÓN ===");
        System.out.println("1. Dólar a Peso Argentino");
        System.out.println("2. Peso Argentino a Dólar");
        System.out.println("3. Euro a Peso Argentino");
        System.out.println("4. Real Brasileño a Dólar");
        System.out.println("5. Peso Chileno a Peso Argentino");
        System.out.println("6. Libra Esterlina a Peso Argentino");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }

    private static void convertir(String monedaOrigen, String monedaDestino, Scanner scanner) {
        try {
            System.out.print("Ingresa el valor que deseas convertir: ");
            double cantidad = Double.parseDouble(scanner.nextLine());

            double tasaConversion = obtenerTasaConversion(monedaOrigen, monedaDestino);
            double resultado = cantidad * tasaConversion;

            System.out.printf("El valor de %.2f %s corresponde al valor final de %.2f %s%n",
                    cantidad, monedaOrigen, resultado, monedaDestino);

        } catch (NumberFormatException e) {
            System.out.println("Error: Debe ingresar un número válido.");
        } catch (Exception e) {
            System.out.println("Error durante la conversión: " + e.getMessage());
        }
    }

    private static double obtenerTasaConversion(String monedaOrigen, String monedaDestino) throws Exception {

        URI uri = new URI(API_URL + monedaOrigen);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String jsonResponse = response.toString();

            if (jsonResponse.contains("\"result\":\"success\"")) {
                String tasasSection = jsonResponse.substring(jsonResponse.indexOf("\"conversion_rates\":{") + 19);
                tasasSection = tasasSection.substring(0, tasasSection.indexOf("}"));

                Map<String, Double> tasas = parseRates(tasasSection);

                if (tasas.containsKey(monedaDestino)) {
                    return tasas.get(monedaDestino);
                } else {
                    throw new Exception("Moneda destino no encontrada en la respuesta");
                }
            } else {
                throw new Exception("Error en la respuesta de la API");
            }
        } else {
            throw new Exception("Error en la conexión a la API. Código: " + responseCode);
        }
    }

    private static Map<String, Double> parseRates(String tasasText) {
        Map<String, Double> tasas = new HashMap<>();
        String[] pares = tasasText.split(",");

        for (String par : pares) {
            String[] keyValue = par.split(":");
            if (keyValue.length == 2) {
                String moneda = keyValue[0].trim().replace("\"", "");
                double valor = Double.parseDouble(keyValue[1].trim());
                tasas.put(moneda, valor);
            }
        }

        return tasas;
    }
}
