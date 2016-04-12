package Estructuras;

import Clases.Cliente;
import Excepciones.ObjetoNoExistenteException;
import Excepciones.ObjetoYaExistenteException;
import Utilidades.ConexionMySQL;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Coleccion de objetos de la clase Cliente.
 *
 * @author Kevin
 */
//TODO - Cambiar ficheros por base de datos
public class ColeccionClientes {

    private static final String PATH = "ficheros/listaClientes.txt";

    private final ArrayList<Cliente> clientes;             //Coleccion de clientes
    private final ConexionMySQL conexionMySQL;

    /**
     * Inicializa la coleccion con un tamanyo determinado.
     */
    public ColeccionClientes() throws IOException, FileNotFoundException, ClassNotFoundException, SQLException {
        clientes = new ArrayList<>();
        conexionMySQL = new ConexionMySQL();
    }

    /**
     * Anayade un cliente a la coleccion.
     *
     * @param dni dni del cliente
     * @param nombre nombre del cliente
     * @param direccion direccion del cliente
     * @param tlf telefono del cliente
     * @param vip si del cliente es vip
     * @throws ObjetoYaExistenteException si el objeto a añadir ya existe.
     */    
    public void anyadirCliente(String dni, String nombre, String direccion, String tlf, boolean vip) throws ObjetoYaExistenteException {
        if (posicionCliente(dni) < 0) {
            Cliente c = new Cliente(dni, nombre, direccion, tlf);
            c.setVip(vip);
            clientes.add(c);
            guardar();
        } else {
            throw new ObjetoYaExistenteException();
        }
    }

    /**
     * Devuelve el cliente identificado por el dni dado.
     *
     * @param dni el dni del cliente a buscar.
     * @return el cliente con el dni dado.
     * @throws ObjetoNoExistenteException si el cliente no existe en la
     * coleccion.
     */
    public Cliente obtenerCliente(String dni) throws ObjetoNoExistenteException {
        for (Cliente cliente : clientes) {
            if (cliente != null && cliente.getDni().equals(dni)) {
                return cliente;
            }
        }
        throw new ObjetoNoExistenteException("El cliente con DNI " + dni + " no existe.");
    }

    /**
     * Modifica el clientes identificado por el DNI dado, asignandole el cliente
     * dado.
     *
     * @param DNI dni del cliente
     * @param nombre nombre del cliente
     * @param direccion direccion del cliente
     * @param tlf telefono del cliente
     * @param vip si del cliente es vip
     * @throws ObjetoNoExistenteException si el vehiculo a modificar no existe.
     */
    public void modificarCliente(String DNI, String nombre, String direccion, String tlf, boolean vip) throws ObjetoNoExistenteException {
        for (Cliente cliente : clientes) {
            if (cliente.getDni().equals(DNI)) {
                Cliente c = new Cliente(DNI, nombre, direccion, tlf);
                c.setVip(vip);
                cliente = c;
                guardar();
                return;
            }
        }
        throw new ObjetoNoExistenteException("El cliente con DNI " + DNI + " no esta registrado.");
    }

    /**
     * Elimina el cliente identificado por el DNI dado.
     *
     * @param dni el DNI del cliente.
     * @throws ObjetoNoExistenteException so no existe el cliente en la
     * coleccion,
     */
    public void eliminarCliente(String dni) throws ObjetoNoExistenteException {
        int index = posicionCliente(dni);
        if (index >= 0) {
            clientes.remove(index);
            guardar();
        } else {
            throw new ObjetoNoExistenteException("El cliente con dni " + dni + " no existe.");
        }
    }
    
    /**
     * Devuelve un iterador para la coleccion de clientes.
     *
     * @return un iterador para la coleccion de clientes.
     */
    public IteradorClientes getIterador() {
        return new IteradorClientes(clientes);
    }

    /**
     * Devuelve un array bidimensional con la informacion de la lista. Cada fila
     * contiene un elemento, y las columnas contienen el dni, nombre, direccion
     * telefono y vip, respepctivamente.
     *
     * @return un array bidimensional con la informacion de la lista.
     */
    public String[][] obtenerDataArray() {
        String[][] ret = new String[clientes.size()][3];
        for (int i = 0; i < clientes.size(); i++) {
            ret[i] = clientes.get(i).dataToArray();
        }
        return ret;
    }
    
    /**
     * Devuelve un array con los DNIs de los clientes de la coleccion.
     *
     * @return un array con los DNIs de los clientes de la coleccion.
     */
    public String[] obtenerArrayDnis() {
        String[] dnis = new String[clientes.size()];
        for (int i = 0; i < clientes.size(); i++) {
            dnis[i] = clientes.get(i).getDni();
        }
        return dnis;
    }

    /**
     * Carga la informacion del fichero en el programa
     */
    public void cargar() {
        File archivo = new File(PATH);
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(archivo));

            //Lee el encabezado del archivo e informa si esta vacio.
            String str = reader.readLine();
            if (str == null) {
                //Archivo en blanco
                return;
            } else {
                //Lee la primera linea del archivo e informa si no contiene datos.
                str = reader.readLine();
                if (str == null || str.equals("")) {
                    //Archivo sin informacion
                    return;
                }
                int linea = 1;
                while (str != null && !str.equals("")) {
                    String[] datos = str.split("\\t\\t");
                    if (datos.length != 5) {
                        //Datos de la linea incorrectos
                    } else {
                        Cliente c = new Cliente(datos[0].trim(), datos[1].trim(), datos[2].trim(), datos[3].trim());
                        if (datos[4].equals("S")) {
                            c.setVip(true);
                        }
                        clientes.add(c);
                    }
                    str = reader.readLine();
                    linea++;
                }
            }
            reader.close();
        } catch (IOException e) {
            //Fallo de lectura
        }
    }

    /**
     * Guarda la informacion almacenada en la lista en un fichero.
     */
    public void guardar() {
        File archivo = new File(PATH);
        try (PrintWriter writer = new PrintWriter(new FileWriter(archivo))) {
            writer.println("NumDNI\t\tNombre\t\tDireccion\t\tTelefono\t\tVIP");

            for (Cliente cliente : clientes) {
                String dni = cliente.getDni();
                String nombre = cliente.getNombre();
                String dir = cliente.getDireccion();
                String tlf = cliente.getTlf();
                String vip = cliente.isVip() ? "S" : "N";
                writer.printf("%s\t\t%s\t\t%s\t\t%s\t\t%s%n", dni, nombre, dir, tlf, vip);
            }
        } catch (IOException e) {
            //Fallo de escritura
        }
    }

    /**
     * Devuelve la posicion del cliente identificado por el dni introducido en
     * la coleccion.
     *
     * @param dni del cliente a buscar.
     * @return la posicion del cliente; -1 si no existe en la coleccion.
     */
    private int posicionCliente(String dni) {
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getDni().equals(dni)) {
                return i;
            }
        }
        return -1;
    }
}
