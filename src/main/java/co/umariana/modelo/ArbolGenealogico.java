package co.umariana.modelo;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * El árbol genealógico: un SUJETO (posición "1") con sus ASCENDIENTES (padres, abuelos... en Ahnentafel)
 * y sus DESCENDIENTES (hijos, nietos... en d'Aboville). Ver la clase Posicion.
 *
 * Son en realidad dos árboles unidos en la raíz:
 *
 *     ascendientes:  árbol BINARIO. Cada persona tiene como mucho un padre (2n) y una madre (2n + 1).
 *     descendientes: árbol GENERAL. Cada persona tiene una lista de hijos (1.1, 1.2, ...) ordenada por número.
 *
 * El árbol solo guarda a los miembros que EXISTEN: no hay "huecos" ni personas vacías. Puede faltar el padre
 * de alguien, o el hijo 1.1 aunque exista el 1.2.
 *
 * INSTRUCCIONES PARA EL ESTUDIANTE (Etapa 2)
 * ------------------------------------------
 * 1. Implemente el árbol con sus PROPIOS NODOS: una clase interna "Nodo" con la Persona, el nodo del
 *    padre, el nodo de la madre y la lista de hijos. NO se permite usar Map ni Set (las pruebas lo
 *    revisan): para buscar una posición recorra el árbol siguiendo su camino
 *      - Ahnentafel: los bits de n después del primer 1 dicen el camino (0 = padre, 1 = madre).
 *        Ej.: 5 = 101 -> desde el sujeto: padre (0), luego madre (1).
 *      - D'Aboville: cada número de la posición dice a qué hijo bajar. Ej.: 1.2.3 -> hijo 2, luego su hijo 3.
 *    Se permite usar List/ArrayList para la lista de hijos.
 * 2. Todos los errores de las reglas se informan con {@link ArbolInvalidoException} (un mensaje claro).
 *    Los datos inválidos de una Persona los detecta Persona.validar() (IllegalArgumentException).
 * 3. NO modifique las pruebas (src/test).
 *
 * REGLAS DEL ÁRBOL
 *   - La posición "1" (el sujeto) existe siempre y no se puede eliminar.
 *   - Una posición no puede repetirse.
 *   - Un ascendiente n solo se puede agregar si ya existe su hijo n / 2, y debe haber NACIDO ANTES
 *     (año de nacimiento menor) que ese hijo.
 *   - Un descendiente solo se puede agregar si ya existe su progenitor, y debe haber NACIDO DESPUÉS
 *     (año de nacimiento mayor) que su progenitor.
 */
public class ArbolGenealogico {

    // TODO Etapa 2: declare la clase interna Nodo y los atributos necesarios (raíz, cantidad de miembros...).

    private static class Nodo {

        Persona persona;
        Nodo padre;
        Nodo madre;
        List<Nodo> hijos;

        Nodo(Persona persona) {
            this.persona = persona;
            this.hijos = new ArrayList<>();
        }
    }

    private final Nodo raiz;
    private int cantidad;

    /**
     * Crea un árbol que solo tiene al sujeto.
     *
     * @throws IllegalArgumentException si la persona no es válida (Persona.validar)
     * @throws ArbolInvalidoException si su posición no es "1"
     */
    public ArbolGenealogico(Persona sujeto) {
        // TODO Etapa 2
        if (sujeto == null) {
            throw new IllegalArgumentException(
                    "El sujeto no puede ser nulo");
        }

        sujeto.validar();

        if (!Posicion.esSujeto(sujeto.getPosicion())) {
            throw new ArbolInvalidoException(
                    "El sujeto debe tener la posicion 1");
        }

        raiz = new Nodo(sujeto);
        cantidad = 1;
    }

    /** @return el sujeto (la persona de la posición "1") */
    public Persona sujeto() {
        // TODO Etapa 2
        return raiz.persona;
    }

    /** @return cuántos miembros tiene el árbol (el sujeto cuenta; un árbol nuevo tiene 1) */
    public int tamano() {
        // TODO Etapa 2
        return cantidad;
    }

    /**
     * @return la persona de esa posición, o null si no existe en el árbol o si la posición no es válida
     */
    public Persona buscar(String posicion) {
        // TODO Etapa 2
        Nodo nodo = buscarNodo(posicion);

        if (nodo == null) {
            return null;
        }

        return nodo.persona;
    }

    // Método auxiliar para buscar un nodo por su posición.
    private Nodo buscarNodo(String posicion) {

        if (!Posicion.esValida(posicion)) {
            return null;
        }

        if (Posicion.esSujeto(posicion)) {
            return raiz;
        }

        if (Posicion.esAscendiente(posicion)) {

            int numero = Integer.parseInt(posicion);
            String bits = Integer.toBinaryString(numero);

            Nodo actual = raiz;

            for (int i = 1; i < bits.length(); i++) {

                if (actual == null) {
                    return null;
                }

                if (bits.charAt(i) == '0') {
                    actual = actual.padre;
                } else {
                    actual = actual.madre;
                }
            }

            return actual;
        }

        String[] partes = posicion.split("\\.");
        Nodo actual = raiz;

        for (int i = 1; i < partes.length; i++) {

            int ordenBuscado = Integer.parseInt(partes[i]);
            Nodo encontrado = null;

            for (Nodo hijo : actual.hijos) {

                if (Posicion.orden(
                        hijo.persona.getPosicion()) == ordenBuscado) {
                    encontrado = hijo;
                    break;
                }
            }

            if (encontrado == null) {
                return null;
            }

            actual = encontrado;
        }

        return actual;
    }

    /** @return true si hay alguien en esa posición (false también si la posición no es válida) */
    public boolean contiene(String posicion) {
        // TODO Etapa 2
        return buscarNodo(posicion) != null;
    }

    /**
     * Agrega una persona en la posición que ella misma trae (la usa el constructor del árbol a partir del texto).
     * El orden de llamada importa: primero debe existir la persona de la que depende (ver REGLAS DEL ÁRBOL).
     *
     * @return la misma persona
     * @throws IllegalArgumentException si la persona no es válida (Persona.validar)
     * @throws ArbolInvalidoException si la posición ya existe (o es "1"), si falta la persona de la que depende,
     *         o si los años de nacimiento no cumplen las reglas
     */
    public Persona agregar(Persona persona) {
        // TODO Etapa 2
        if (persona == null) {
            throw new IllegalArgumentException(
                    "La persona no puede ser nula");
        }

        persona.validar();

        String posicion = persona.getPosicion();

        if (contiene(posicion)) {
            throw new ArbolInvalidoException(
                    "Ya existe una persona en esa posicion");
        }

        if (Posicion.esAscendiente(posicion)) {

            String posicionHijo =
                    Posicion.posicionDelHijoEnLinea(posicion);

            Nodo hijo = buscarNodo(posicionHijo);

            if (hijo == null) {
                throw new ArbolInvalidoException(
                        "No existe el hijo del ascendiente");
            }

            if (persona.getNacimiento() >=
                    hijo.persona.getNacimiento()) {
                throw new ArbolInvalidoException(
                        "El ascendiente debe nacer antes que su hijo");
            }

            Nodo nuevo = new Nodo(persona);
            int numero = Integer.parseInt(posicion);

            if (numero % 2 == 0) {
                hijo.padre = nuevo;
            } else {
                hijo.madre = nuevo;
            }

            cantidad++;
            return persona;
        }

        if (Posicion.esDescendiente(posicion)) {

            String posicionProgenitor =
                    Posicion.posicionDelProgenitor(posicion);

            Nodo progenitor = buscarNodo(posicionProgenitor);

            if (progenitor == null) {
                throw new ArbolInvalidoException(
                        "No existe el progenitor");
            }

            if (persona.getNacimiento() <=
                    progenitor.persona.getNacimiento()) {
                throw new ArbolInvalidoException(
                        "El descendiente debe nacer despues de su progenitor");
            }

            Nodo nuevo = new Nodo(persona);
            int ordenNuevo = Posicion.orden(posicion);
            int indice = 0;

            while (indice < progenitor.hijos.size()
                    && Posicion.orden(
                            progenitor.hijos.get(indice)
                                    .persona.getPosicion()) < ordenNuevo) {
                indice++;
            }

            progenitor.hijos.add(indice, nuevo);

            cantidad++;
            return persona;
        }

        throw new ArbolInvalidoException(
                "No se puede agregar otro sujeto");
    }

    /**
     * Agrega el PADRE de la persona de esa posición, en la posición 2n (ver Posicion.posicionDelPadre).
     * Solo se pueden agregar padres al sujeto y a sus ascendientes.
     *
     * @return la persona creada
     * @throws ArbolInvalidoException si la posición no existe, si no es el sujeto ni un ascendiente, si ese padre
     *         ya existe o si no cumple las reglas del árbol
     * @throws IllegalArgumentException si los datos de la persona no son válidos
     */
    public Persona agregarPadre(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        // TODO Etapa 2
        if (!(Posicion.esSujeto(posicion)
                || Posicion.esAscendiente(posicion))
                || !contiene(posicion)) {

            throw new ArbolInvalidoException(
                    "La posicion no permite agregar un padre");
        }

        String nuevaPosicion =
                Posicion.posicionDelPadre(posicion);

        if (!Posicion.esAhnentafel(nuevaPosicion)) {
            throw new ArbolInvalidoException(
                    "Se excede el limite de ascendientes");
        }

        Persona persona = new Persona(
                nuevaPosicion, nombre, nacimiento, fallecimiento);

        return agregar(persona);
    }

    /**
     * Igual que agregarPadre, pero agrega la MADRE (posición 2n + 1).
     */
    public Persona agregarMadre(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        // TODO Etapa 2
        if (!(Posicion.esSujeto(posicion)
                || Posicion.esAscendiente(posicion))
                || !contiene(posicion)) {

            throw new ArbolInvalidoException(
                    "La posicion no permite agregar una madre");
        }

        String nuevaPosicion =
                Posicion.posicionDeLaMadre(posicion);

        if (!Posicion.esAhnentafel(nuevaPosicion)) {
            throw new ArbolInvalidoException(
                    "Se excede el limite de ascendientes");
        }

        Persona persona = new Persona(
                nuevaPosicion, nombre, nacimiento, fallecimiento);

        return agregar(persona);
    }

    /**
     * Agrega un HIJO a la persona de esa posición. Solo se pueden agregar hijos al sujeto y a sus descendientes.
     * Su posición es la del progenitor + "." + el número siguiente al MAYOR número de hijo que ya exista
     * (o 1 si todavía no tiene hijos). Ej.: si "1" tiene los hijos 1.1 y 1.3, el nuevo es 1.4.
     *
     * @return la persona creada
     * @throws ArbolInvalidoException si la posición no existe, si no es el sujeto ni un descendiente, o si no cumple
     *         las reglas del árbol
     * @throws IllegalArgumentException si los datos de la persona no son válidos
     */
    public Persona agregarHijo(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        // TODO Etapa 2
        if (!(Posicion.esSujeto(posicion)
                || Posicion.esDescendiente(posicion))
                || !contiene(posicion)) {

            throw new ArbolInvalidoException(
                    "La posicion no permite agregar hijos");
        }

        Nodo progenitor = buscarNodo(posicion);
        int mayor = 0;

        for (Nodo hijo : progenitor.hijos) {

            int numero = Posicion.orden(
                    hijo.persona.getPosicion());

            if (numero > mayor) {
                mayor = numero;
            }
        }

        String nuevaPosicion =
                posicion + "." + (mayor + 1);

        if (!Posicion.esDAboville(nuevaPosicion)) {
            throw new ArbolInvalidoException(
                    "Se excede el limite de descendientes");
        }

        Persona persona = new Persona(
                nuevaPosicion, nombre, nacimiento, fallecimiento);

        return agregar(persona);
    }

    /**
     * Elimina a la persona de esa posición y TODO lo que depende de ella:
     *   - si es un ascendiente: ella y todos sus propios ascendientes (sus padres, abuelos...);
     *   - si es un descendiente: ella y todos sus propios descendientes (hijos, nietos...).
     * Las demás posiciones NO cambian de número.
     *
     * @return cuántas personas se eliminaron (ella incluida)
     * @throws ArbolInvalidoException si la posición es "1" (el sujeto no se puede eliminar) o si no existe
     */
    public int eliminar(String posicion) {
        // TODO Etapa 2
        if (Posicion.esSujeto(posicion)) {
            throw new ArbolInvalidoException(
                    "No se puede eliminar el sujeto");
        }

        Nodo nodo = buscarNodo(posicion);

        if (nodo == null) {
            throw new ArbolInvalidoException(
                    "La persona no existe en el arbol");
        }

        int eliminados;

        if (Posicion.esAscendiente(posicion)) {

            eliminados = contarSubarbolAscendente(nodo);

            String posicionHijo =
                    Posicion.posicionDelHijoEnLinea(posicion);

            Nodo hijo = buscarNodo(posicionHijo);
            int numero = Integer.parseInt(posicion);

            if (numero % 2 == 0) {
                hijo.padre = null;
            } else {
                hijo.madre = null;
            }

        } else {

            eliminados = contarSubarbolDescendente(nodo);

            String posicionProgenitor =
                    Posicion.posicionDelProgenitor(posicion);

            Nodo progenitor = buscarNodo(posicionProgenitor);
            progenitor.hijos.remove(nodo);
        }

        cantidad -= eliminados;

        return eliminados;
    }

    // Método auxiliar para contar ascendientes.
    private int contarSubarbolAscendente(Nodo nodo) {

        if (nodo == null) {
            return 0;
        }

        return 1
                + contarSubarbolAscendente(nodo.padre)
                + contarSubarbolAscendente(nodo.madre);
    }

    // Método auxiliar para contar descendientes.
    private int contarSubarbolDescendente(Nodo nodo) {

        if (nodo == null) {
            return 0;
        }

        int total = 1;

        for (Nodo hijo : nodo.hijos) {
            total += contarSubarbolDescendente(hijo);
        }

        return total;
    }

    /**
     * @return todos los miembros en este orden: el sujeto; luego los ascendientes por número Ahnentafel
     *         ascendente (2, 3, 4, 5...); luego los descendientes en PREORDEN (cada persona y después sus hijos:
     *         1.1, 1.1.1, 1.1.2, 1.2, ...). La lista es una copia nueva: modificarla no cambia el árbol.
     *         Este orden garantiza que cada persona aparece DESPUÉS de la que necesita (útil para guardar y cargar).
     */
    public List<Persona> miembros() {
        // TODO Etapa 2
        List<Persona> lista = new ArrayList<>();

        lista.add(raiz.persona);
        lista.addAll(ascendientes());
        lista.addAll(descendientes());

        return lista;
    }

    /** @return solo los ascendientes, por número Ahnentafel ascendente (sin el sujeto) */
    public List<Persona> ascendientes() {
        // TODO Etapa 2
        List<Persona> lista = new ArrayList<>();

        recorrerAscendientes(raiz, lista);

        lista.sort(Comparator.comparingInt(
                persona -> Integer.parseInt(
                        persona.getPosicion())));

        return lista;
    }

    /** @return solo los descendientes, en preorden (sin el sujeto) */
    public List<Persona> descendientes() {
        // TODO Etapa 2
        List<Persona> lista = new ArrayList<>();

        recorrerDescendientes(raiz, lista);

        return lista;
    }

    // Método auxiliar para recorrer los ascendientes.
    private void recorrerAscendientes(
            Nodo nodo, List<Persona> lista) {

        if (nodo == null) {
            return;
        }

        if (nodo.padre != null) {
            lista.add(nodo.padre.persona);
            recorrerAscendientes(nodo.padre, lista);
        }

        if (nodo.madre != null) {
            lista.add(nodo.madre.persona);
            recorrerAscendientes(nodo.madre, lista);
        }
    }

    // Método auxiliar para recorrer los descendientes.
    private void recorrerDescendientes(
            Nodo nodo, List<Persona> lista) {

        for (Nodo hijo : nodo.hijos) {
            lista.add(hijo.persona);
            recorrerDescendientes(hijo, lista);
        }
    }
}