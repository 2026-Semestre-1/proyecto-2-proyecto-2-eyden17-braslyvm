/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CPU;

import CPU.BCP;
import Memoria.Memoria;
import Memoria.Disco;

/**
 *
 * @author braslyvm
 */
public class CPU {
    private Memoria memoria;
    private Disco disco;
    private BCP bcp;
    private boolean interrupcion;
    private boolean procesoFinalizado;
    private String tipoInterrupcion = "";
     

    /**
    *Inicializa la CPU con la memoria y el proceso actual, cargando el pc inicial y reiniciando los registros internos.
    */
    public CPU(Memoria memoria, Disco disco) {
        this.memoria = memoria;
        this.disco = disco;
        this.interrupcion = false;
        this.procesoFinalizado = false;
    }
    /**
    *Ejecuta la intruccion que corresponde del proceso actual
    */
    
    public void CargarBcp (BCP bcp){
        this.bcp = bcp;
        this.interrupcion = false;
        this.procesoFinalizado = false;
        bcp.setEstado("ejecución");
    }
    /**
     * Ejecuta la instrucción actual del proceso, actualizando el PC, el estado del proceso y manejando interrupciones según sea necesario.
     * 
     */
    
    public boolean ejecutar() {
        if (bcp == null || procesoFinalizado) return false;
        if (interrupcion) return false;

        int pc = bcp.getPc();

        if (pc < bcp.getBase() || pc > bcp.getLimite()) {
            bcp.setEstado("finalizado");
            procesoFinalizado = true;
            return false;
        }

        String instruccion = memoria.leerInstruccion(bcp);
        if (instruccion == null || instruccion.trim().isEmpty()) {
            bcp.setEstado("finalizado");
            procesoFinalizado = true;
            return false;
        }


        bcp.setIr(instruccion);
        bcp.setUltimaDireccionEjecutada(pc);
        boolean Salto = EjecutarIntruccion(instruccion);

        if (!Salto) {
            bcp.setPc(pc + 1);
        }
        
        bcp.setTiempoEmpleado(bcp.getTiempoEmpleado() + 1);

        return true;
    }
    
    /**
     * Ejecuta la instrucción especificada, lee las lineas y separa por partes 
     * @param Linea
     * @return
     */
    public boolean EjecutarIntruccion (String Linea){
        String[] partes = Linea.split("\\s+", 2);
        String Instruccion = partes[0].toUpperCase();
        String operados = partes.length > 1 ? partes[1].trim() : "";
        
        switch (Instruccion){
            case "LOAD": {
                bcp.setAc(Registros(operados));
                break;
            }
            case "STORE": {
                CambiarRegistros(operados, bcp.getAc());
                break;
            }
            case "MOV": {
                String[] ops = operados.split(",");
                String destino = ops[0].trim();
                String origen = ops[1].trim();
                
                try {
                    CambiarRegistros(destino, parseValor(origen));
                } catch (NumberFormatException e) {
                    CambiarRegistros(destino, Registros(origen));
                }
                break;
            }
            case "ADD": {
                bcp.setAc(bcp.getAc() + Registros(operados));
                break;
            }
            case "SUB": {
                bcp.setAc(bcp.getAc() - Registros(operados));
                break;
            }
            case "INC": {
                 if (operados.isEmpty()) {
                    bcp.setAc(bcp.getAc() + 1);
                } else {
                    CambiarRegistros(operados, Registros(operados) + 1);
                }
                break;
            }
            case "DEC": {
                if (operados.isEmpty()) {
                    bcp.setAc(bcp.getAc() - 1 );
                } else {
                    CambiarRegistros(operados, Registros(operados) - 1);
                }
                break;
            }
            case "SWAP": {
                String[] ops = operados.split(",");
                String x = ops[0].trim();
                String y = ops[1].trim();
                int z = Registros (x);
                CambiarRegistros(x, Registros(y));
                CambiarRegistros(y, z);
                break;
            }
            case "INT": {
                if (!interrupcion) {
                    switch (operados.toUpperCase()) {

                        case "20H": {
                            bcp.setEstado("finalizado");
                            procesoFinalizado = true;
                            bcp.setPc(bcp.getPc() + 1);
                            break;
                        }

                        case "10H": {
                            tipoInterrupcion = "10H";
                            interrupcion = true;
                            bcp.setEstado("en espera");
                            break;
                        }

                        case "09H": {
                            tipoInterrupcion = "09H";
                            interrupcion = true;
                            bcp.setEstado("en espera");
                            break;
                        }

                        case "21H": {
                                int ah = bcp.getAh();

                                switch (ah) {
                                    case 0x3C:
                                        bcp.crearArchivoDesdeDX();
                                        break;

                                    case 0x3D:
                                        bcp.abrirArchivoDesdeDX();
                                        break;

                                    case 0x4D:
                                        bcp.leerArchivoDesdeDX();
                                        break;

                                    case 0x40:
                                        bcp.escribirArchivoDesdeDX();
                                        break;

                                    case 0x41:
                                        bcp.eliminarArchivoDesdeDX();
                                        break;

                                    default:
                                        javax.swing.JOptionPane.showMessageDialog(
                                                null,
                                                "INT 21H: operación AH desconocida: " + String.format("%02XH", ah),
                                                bcp.getIdProceso(),
                                                javax.swing.JOptionPane.ERROR_MESSAGE
                                        );
                                        break;
                                }

                                bcp.setPc(bcp.getPc() + 1);
                                break;
                            }

                        default: {
                            javax.swing.JOptionPane.showMessageDialog(
                                null,
                                "Interrupción desconocida: INT " + operados,
                                bcp.getIdProceso(),
                                javax.swing.JOptionPane.ERROR_MESSAGE
                            );
                            bcp.setPc(bcp.getPc() + 1);
                            break;
                        }
                    }
                } else {
                    interrupcion = false;
                    bcp.setEstado("ejecución");
                    bcp.setPc(bcp.getPc() + 1);
                }
                return true;
            }
            case "JMP": {
                int desplazamiento = Integer.parseInt(operados);
                    bcp.setPc(bcp.getPc() + desplazamiento);
                return true;
            }
            case "CMP": {
                String[] ops = operados.split(",");
                int v1 = Registros(ops[0].trim());
                int v2 = Registros(ops[1].trim());
                bcp.setAc(Integer.compare(v1, v2));

                break;
            }
            case "JE": {
                int desplazamiento = Integer.parseInt(operados);
                if (bcp.getAc() == 0) {
                    int nuevoPc = bcp.getPc() + desplazamiento;
                    // Verificar desbordamiento
                    if (nuevoPc < bcp.getBase() || nuevoPc > bcp.getLimite()) {
                       javax.swing.JOptionPane.showMessageDialog(
                            null,
                            "JE fuera de límites en proceso " ,
                            bcp.getIdProceso(),
                            javax.swing.JOptionPane.ERROR_MESSAGE
                        );
                        bcp.setEstado("finalizado");
                        procesoFinalizado = true;
                    } else {
                        bcp.setPc(nuevoPc);
                    }
                } else {
                    bcp.setPc(bcp.getPc() + 1);
                }
                
                return true;
            }
            case "JNE": {
                int desplazamiento = Integer.parseInt(operados);
                if (bcp.getAc() != 0) {
                    int nuevoPc = bcp.getPc() + desplazamiento;
                    if (nuevoPc < bcp.getBase() || nuevoPc > bcp.getLimite()) {
                        javax.swing.JOptionPane.showMessageDialog(
                            null,
                            "JE fuera de límites en proceso " ,
                            bcp.getIdProceso(),
                            javax.swing.JOptionPane.ERROR_MESSAGE
                        );
                        bcp.setEstado("finalizado");
                        procesoFinalizado = true;
                    } else {
                        bcp.setPc(nuevoPc);
                    }
                } else {
                    bcp.setPc(bcp.getPc() + 1);
                }
                return true;
            }
            case "PARAM": {
                String[] lista = operados.split(",");

                if (lista.length < 1 || lista.length > 3) {
                    javax.swing.JOptionPane.showMessageDialog(
                        null,
                        "PARAM permite de 1 a 3 parámetros",
                        operados,
                        javax.swing.JOptionPane.ERROR_MESSAGE
                    );
                    break;
                }

                for (int i = 0; i < lista.length; i++) {
                    try {
                        int valor = Integer.parseInt(lista[i].trim());

                        if (valor < 0 || valor > 255) {
                            javax.swing.JOptionPane.showMessageDialog(
                                null,
                                "El parámetro debe estar entre 0 y 255",
                                lista[i].trim(),
                                javax.swing.JOptionPane.ERROR_MESSAGE
                            );
                            break;
                        }

                        bcp.AgregarPila(valor);

                    } catch (NumberFormatException e) {
                        javax.swing.JOptionPane.showMessageDialog(
                            null,
                            "El parámetro debe ser numérico",
                            lista[i].trim(),
                            javax.swing.JOptionPane.ERROR_MESSAGE
                        );
                        break;
                    }
                }

                break;
            }
            case "PUSH": {
                try {
                    bcp.AgregarPila(Registros(operados));
                } catch (Exception e) {
                    javax.swing.JOptionPane.showMessageDialog(
                            null,
                            "Error al ingresar a la pila" ,
                            "Pila llena",
                            javax.swing.JOptionPane.ERROR_MESSAGE
                        );
                }
                break;
            }
            case "POP": {
                try {
                    CambiarRegistros(operados, bcp.EliminarPila());
                } catch (Exception e) {
                    javax.swing.JOptionPane.showMessageDialog(
                            null,
                            "Error al hacer POP" ,
                            "Pila vacia",
                            javax.swing.JOptionPane.ERROR_MESSAGE
                        );
                }

                break;
            }
        }
        return false;
    }

    /**
    * Devuelve el valor almacenado en el registro indicado por el código binario recibido.
    */
    private int Registros(String registro) {
        switch (registro.toUpperCase()) {
            case "AX":
                return bcp.getAx();
            case "BX":
                return bcp.getBx();
            case "CX":
                return bcp.getCx();
            case "DX":
                return bcp.getDx();
            case "AC":
                return bcp.getAc();
            case "AL":
                return bcp.getAl();
            case "AH":
                return bcp.getAh();
            default:
                return bcp.getAc();
        }
    }
    /**
    *  Actualiza el valor del registro indicado con el dato recibido como parámetro.
    */
    public void CambiarRegistros(String registro, int valor) {
        switch (registro.toUpperCase()) {
            case "AC":
                bcp.setAc(valor);
                break;
            case "AX":
                bcp.setAx(valor);
                break;
            case "BX":
                bcp.setBx(valor);
                break;
            case "CX":
                bcp.setCx(valor);
                break;
            case "DX":
                bcp.setDx(valor);
                break;
            case "AL":
                bcp.setAl(valor);
                break;
            case "AH":
                bcp.setAh(valor);
                break;
            default:
                bcp.setAc(valor);
                break;
        }
    }
    /**
    * La interfaz llama esto cuando INT 10H termina de mostrar en pantalla.
    */
   public void resolverInterrupcion() {
        interrupcion = false;
        tipoInterrupcion = "";
        bcp.setEstado("ejecución");
        bcp.setPc(bcp.getPc() + 1);
    }

   /**
    * La interfaz llama esto cuando el usuario ingresó un valor por teclado (INT 09H).
    * Solo acepta 0-255.
    */
   public void recibirEntradaTeclado(int valor) {
        if (valor < 0 || valor > 255) {
            javax.swing.JOptionPane.showMessageDialog(
                null,
                "Valor fuera de rango. Debe estar entre 0 y 255.",
                "Error INT 09H",
                javax.swing.JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        bcp.setDx(valor);
        interrupcion = false;
        tipoInterrupcion = "";
        bcp.setEstado("ejecución");
        bcp.setPc(bcp.getPc() + 1);
    }
    /**
     * convierte un valor numerio en Hexadecimal si termina con H, o lo devuelve como decimal si no tiene H.
     * @param valor
     * @return
     */
   private int parseValor(String valor) {
        valor = valor.trim().toUpperCase();

        if (valor.endsWith("H")) {
            String hex = valor.substring(0, valor.length() - 1);
            return Integer.parseInt(hex, 16);
        }

        return Integer.parseInt(valor);
    }

   public boolean isInterrupcion()      { 
       return interrupcion; 
   }
   public boolean isProcesoFinalizado() { 
       return procesoFinalizado; 
   }
   public boolean estaLibre() {
       return bcp == null || procesoFinalizado;
   }
   public void liberarBcp() {
       bcp = null;
       interrupcion = false;
       procesoFinalizado = false;
       tipoInterrupcion = "";
   }
   public BCP getBcp()                  { 
       return bcp; 
   }
   public String getTipoInterrupcion()  { 
       return tipoInterrupcion; 
   }
}