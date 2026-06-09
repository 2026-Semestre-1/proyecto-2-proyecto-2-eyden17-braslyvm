package Parser;

import CPU.BCP;
import Memoria.Memoria;
import Memoria.Disco;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

public class Parser {
    private int siguienteIdProceso = 1;

    /**
     * Lee el archivo .asm, valida cada línea y carga las instrucciones en memoria.
     * Retorna el BCP creado o null si hubo error.
     */
    public BCP Leer(File archivo, Memoria memoria, Disco disco) {
        int cantidadInstrucciones = 0;

        String nombre = archivo.getName().toLowerCase();
        if (!nombre.endsWith(".asm")) {
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Archivo no compatible.",
                    "Error de archivo",
                    javax.swing.JOptionPane.WARNING_MESSAGE
            );
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            java.util.List<String> instrucciones = new java.util.ArrayList<>();

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                if (!ValidarLinea(linea)) {
                    javax.swing.JOptionPane.showMessageDialog(
                            null,
                            "Línea inválida: " + linea,
                            "Error de archivo",
                            javax.swing.JOptionPane.ERROR_MESSAGE
                    );
                    return null;
                }
                instrucciones.add(linea);
                cantidadInstrucciones++;
            }

            if (cantidadInstrucciones == 0) {
                javax.swing.JOptionPane.showMessageDialog(
                        null,
                        "Archivo vacío.",
                        "Error de archivo",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE
                );
                return null;
            }

            String[] arreglo = instrucciones.toArray(new String[0]);
            if (!disco.guardarPrograma(archivo.getName(), arreglo)) {
                return null;
            }

            String idProceso = generarIdProceso();

            if (!memoria.lleno() && memoria.hayEspacioUsuario(arreglo.length)) {
                int[] resultado = memoria.cargarInstruccionesSiCabe(arreglo);
                int baseFinal = resultado[0];
                int limiteFinal = resultado[1];

                BCP bcp = new BCP(
                        idProceso,
                        archivo.getName(),
                        "preparado",
                        baseFinal,
                        limiteFinal,
                        baseFinal,
                        0
                );

                bcp.setTiempoLlegada(0);
                bcp.setRafagaTotal(cantidadInstrucciones);
                return bcp;
            }

            int baseVirtual = disco.cargarProcesoEnVirtual(archivo.getName(), arreglo);
            if (baseVirtual == -1) {
                return null;
            }

            BCP bcp = new BCP(
                    idProceso,
                    archivo.getName(),
                    "nuevo",
                    -1,
                    arreglo.length,
                    -1,
                    0
            );

            bcp.setTiempoLlegada(0);
            bcp.setRafagaTotal(cantidadInstrucciones);
            return bcp;

        } catch (IOException e) {
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Error al leer el archivo: " + e.getMessage(),
                    "Error de archivo",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
            return null;
        }
    }

    private String generarIdProceso() {
        return "P" + siguienteIdProceso++;
    }

    public boolean ValidarLinea(String linea) {
        String[] partes = linea.trim().split("\\s+", 2);
        String operacion = partes[0].toLowerCase();
        String operandos = partes.length > 1 ? partes[1].trim() : "";

        switch (operacion) {

            case "inc":
            case "dec":
                if (operandos.isEmpty()) return true;
                return ValidarRegistro(operandos);

            // Un registro
            case "load":
            case "store":
            case "add":
            case "sub":
            case "push":
            case "pop":
                return ValidarRegistro(operandos);

            case "mov": {
                String[] ops = operandos.split(",", 2);

                if (ops.length != 2) return false;

                String destino = ops[0].trim();
                String origen = ops[1].trim();

                if (!ValidarRegistro(destino)) return false;

                if (destino.equalsIgnoreCase("dx") || destino.equalsIgnoreCase("al")) {
                    return ValidarRegistro(origen)
                            || ValidarNumero(origen)
                            || ValidarCadena(origen);
                }

                return ValidarRegistro(origen) || ValidarNumero(origen);
            }
            case "swap": {
                String[] ops = operandos.split(",");
                if (ops.length != 2) return false;
                return ValidarRegistro(ops[0].trim()) && ValidarRegistro(ops[1].trim());
            }
            case "cmp": {
                String[] ops = operandos.split(",");
                if (ops.length != 2) return false;
                return ValidarRegistro(ops[0].trim()) && ValidarRegistro(ops[1].trim());
            }

           
            case "jmp":
            case "je":
            case "jne":
                return ValidarNumero(operandos);

            case "param": {
                String[] vals = operandos.split(",");
                if (vals.length < 1 || vals.length > 3) return false;
                for (String v : vals) {
                    if (!ValidarNumero(v.trim())) return false;
                }
                return true;
            }

            case "int": {
                String op = operandos.toUpperCase();
                return op.equals("20H") || op.equals("10H")
                        || op.equals("09H") || op.equals("21H");
            }

            default:
                return false;
        }
    }
    public boolean ValidarCadena(String palabra) {
        if (palabra == null) return false;

        palabra = palabra.trim();

        return palabra.length() >= 2
                && palabra.startsWith("\"")
                && palabra.endsWith("\"");
    }

    /**
     * Valida que la palabra sea un registro válido.
     */
    public boolean ValidarRegistro(String palabra) {
        switch (palabra.toLowerCase()) {
            case "ax":
            case "bx":
            case "cx":
            case "dx":
            case "ac":
            case "al":
            case "ah":
                return true;
            default:
                return false;
        }
    }

    /**
     * Valida que la palabra sea un número en rango -127 a 127.
     */
    public boolean ValidarNumero(String palabra) {
        try {
            palabra = palabra.trim().toUpperCase();

            int numero;

            if (palabra.endsWith("H")) {
                String hex = palabra.substring(0, palabra.length() - 1);
                numero = Integer.parseInt(hex, 16);
            } else {
                numero = Integer.parseInt(palabra);
            }

            return numero >= -127 && numero <= 255;

        } catch (NumberFormatException e) {
            return false;
        }
    }
}